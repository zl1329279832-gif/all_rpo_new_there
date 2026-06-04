from __future__ import annotations

from copy import deepcopy
from dataclasses import dataclass, field
from typing import Optional

from config import NetworkConfig, PipeConfig, ValveConfig
from hydraulic import HydraulicResult, solve_network


@dataclass
class LeakageScenario:
    leak_nodes: list[str]
    leak_coefficient: float = 1e-5
    pressure_exponent: float = 0.5
    description: str = ""


@dataclass
class ValveClosureScenario:
    closed_pipe_ids: list[str]
    description: str = ""


@dataclass
class PumpFailureScenario:
    failed_pump_ids: list[str]
    description: str = ""


@dataclass
class ScenarioResult:
    scenario_name: str
    baseline: HydraulicResult = field(default_factory=HydraulicResult)
    scenario: HydraulicResult = field(default_factory=HydraulicResult)
    pressure_changes: dict[str, float] = field(default_factory=dict)
    flow_changes: dict[str, float] = field(default_factory=dict)
    new_shortage_nodes: list[str] = field(default_factory=list)
    description: str = ""


def simulate_leakage(
    config: NetworkConfig,
    baseline: Optional[HydraulicResult] = None,
    leak_nodes: Optional[list[str]] = None,
    leak_coefficient: float = 1e-5,
    pressure_exponent: float = 0.5,
    description: str = "",
) -> ScenarioResult:
    if baseline is None:
        baseline = solve_network(config)
        if baseline.errors:
            return ScenarioResult(
                scenario_name="漏损模拟",
                baseline=baseline,
                description="基准工况求解失败",
            )

    if leak_nodes is None:
        leak_nodes = [
            n.node_id for n in config.nodes if not n.is_source
        ]

    node_ids = {n.node_id for n in config.nodes}
    invalid_nodes = [nid for nid in leak_nodes if nid not in node_ids]
    if invalid_nodes:
        return ScenarioResult(
            scenario_name="漏损模拟",
            baseline=baseline,
            description=f"漏损节点不存在: {invalid_nodes}",
        )

    extra_demands = {}
    for nid in leak_nodes:
        pressure = baseline.node_pressures.get(nid, 0)
        if pressure > 0:
            leak_flow = leak_coefficient * pressure ** pressure_exponent
            extra_demands[nid] = leak_flow
        else:
            extra_demands[nid] = 0.0

    scenario_result = solve_network(config, extra_demands=extra_demands)

    pressure_changes = {}
    for nid in baseline.node_pressures:
        if nid in scenario_result.node_pressures:
            pressure_changes[nid] = (
                scenario_result.node_pressures[nid] - baseline.node_pressures[nid]
            )

    flow_changes = {}
    for pid in baseline.pipe_flows:
        if pid in scenario_result.pipe_flows:
            flow_changes[pid] = scenario_result.pipe_flows[pid] - baseline.pipe_flows[pid]

    new_shortage = []
    for nid in scenario_result.node_pressures:
        if scenario_result.node_pressures[nid] < 0:
            new_shortage.append(nid)

    return ScenarioResult(
        scenario_name="漏损模拟",
        baseline=baseline,
        scenario=scenario_result,
        pressure_changes=pressure_changes,
        flow_changes=flow_changes,
        new_shortage_nodes=new_shortage,
        description=description or f"漏损系数={leak_coefficient}, 指数={pressure_exponent}",
    )


def simulate_valve_closure(
    config: NetworkConfig,
    baseline: Optional[HydraulicResult] = None,
    closed_pipe_ids: Optional[list[str]] = None,
    description: str = "",
) -> ScenarioResult:
    if baseline is None:
        baseline = solve_network(config)
        if baseline.errors:
            return ScenarioResult(
                scenario_name="阀门关闭模拟",
                baseline=baseline,
                description="基准工况求解失败",
            )

    if closed_pipe_ids is None:
        closed_pipe_ids = []

    pipe_ids = {p.pipe_id for p in config.pipes}
    invalid_pipes = [pid for pid in closed_pipe_ids if pid not in pipe_ids]
    if invalid_pipes:
        return ScenarioResult(
            scenario_name="阀门关闭模拟",
            baseline=baseline,
            description=f"管段不存在: {invalid_pipes}",
        )

    modified_config = deepcopy(config)
    for pipe in modified_config.pipes:
        if pipe.pipe_id in closed_pipe_ids:
            pipe.status = "closed"

    scenario_result = solve_network(modified_config)

    if scenario_result.errors:
        for err in scenario_result.errors:
            if "不连通" in err:
                scenario_result.warnings.append(
                    f"关闭管段 {closed_pipe_ids} 导致管网不连通，部分区域将断水"
                )

    pressure_changes = {}
    for nid in baseline.node_pressures:
        if nid in scenario_result.node_pressures:
            pressure_changes[nid] = (
                scenario_result.node_pressures[nid] - baseline.node_pressures[nid]
            )

    flow_changes = {}
    for pid in baseline.pipe_flows:
        if pid in scenario_result.pipe_flows:
            flow_changes[pid] = scenario_result.pipe_flows[pid] - baseline.pipe_flows[pid]

    new_shortage = []
    for nid in scenario_result.node_pressures:
        if scenario_result.node_pressures[nid] < 0:
            new_shortage.append(nid)

    return ScenarioResult(
        scenario_name="阀门关闭模拟",
        baseline=baseline,
        scenario=scenario_result,
        pressure_changes=pressure_changes,
        flow_changes=flow_changes,
        new_shortage_nodes=new_shortage,
        description=description or f"关闭管段: {closed_pipe_ids}",
    )


def simulate_pump_failure(
    config: NetworkConfig,
    baseline: Optional[HydraulicResult] = None,
    failed_pump_ids: Optional[list[str]] = None,
    description: str = "",
) -> ScenarioResult:
    if baseline is None:
        baseline = solve_network(config)
        if baseline.errors:
            return ScenarioResult(
                scenario_name="泵站故障模拟",
                baseline=baseline,
                description="基准工况求解失败",
            )

    if failed_pump_ids is None:
        failed_pump_ids = []

    pump_ids = {p.pump_id for p in config.pumps}
    invalid_pumps = [pid for pid in failed_pump_ids if pid not in pump_ids]
    if invalid_pumps:
        return ScenarioResult(
            scenario_name="泵站故障模拟",
            baseline=baseline,
            description=f"泵站不存在: {invalid_pumps}",
        )

    modified_config = deepcopy(config)
    modified_config.pumps = [
        p for p in modified_config.pumps if p.pump_id not in failed_pump_ids
    ]

    scenario_result = solve_network(modified_config)

    pressure_changes = {}
    for nid in baseline.node_pressures:
        if nid in scenario_result.node_pressures:
            pressure_changes[nid] = (
                scenario_result.node_pressures[nid] - baseline.node_pressures[nid]
            )

    flow_changes = {}
    for pid in baseline.pipe_flows:
        if pid in scenario_result.pipe_flows:
            flow_changes[pid] = scenario_result.pipe_flows[pid] - baseline.pipe_flows[pid]

    new_shortage = []
    for nid in scenario_result.node_pressures:
        if scenario_result.node_pressures[nid] < 0:
            new_shortage.append(nid)

    return ScenarioResult(
        scenario_name="泵站故障模拟",
        baseline=baseline,
        scenario=scenario_result,
        pressure_changes=pressure_changes,
        flow_changes=flow_changes,
        new_shortage_nodes=new_shortage,
        description=description or f"故障泵站: {failed_pump_ids}",
    )


def run_multi_leakage_scenarios(
    config: NetworkConfig,
    leak_coefficients: Optional[list[float]] = None,
    pressure_exponent: float = 0.5,
) -> list[ScenarioResult]:
    if leak_coefficients is None:
        leak_coefficients = [1e-6, 1e-5, 5e-5, 1e-4, 5e-4]

    baseline = solve_network(config)
    results = []
    for coeff in leak_coefficients:
        sr = simulate_leakage(
            config,
            baseline=baseline,
            leak_coefficient=coeff,
            pressure_exponent=pressure_exponent,
            description=f"漏损系数={coeff:.1e}",
        )
        results.append(sr)
    return results


def run_valve_sensitivity(
    config: NetworkConfig,
) -> list[ScenarioResult]:
    baseline = solve_network(config)
    results = []
    for pipe in config.pipes:
        if pipe.status == "open":
            sr = simulate_valve_closure(
                config,
                baseline=baseline,
                closed_pipe_ids=[pipe.pipe_id],
                description=f"关闭管段: {pipe.pipe_id}",
            )
            results.append(sr)
    return results
