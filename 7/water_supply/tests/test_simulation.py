import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

import pytest
from copy import deepcopy

from config import NodeConfig, PipeConfig, PumpConfig, ValveConfig, NetworkConfig
from hydraulic import solve_network
from simulation import (
    simulate_leakage,
    simulate_valve_closure,
    simulate_pump_failure,
    run_multi_leakage_scenarios,
    run_valve_sensitivity,
)


def _make_config_with_pump():
    return NetworkConfig(
        nodes=[
            NodeConfig(node_id="S1", is_source=True, source_head=80, elevation=50),
            NodeConfig(node_id="N1", demand=0.01, elevation=48),
            NodeConfig(node_id="N2", demand=0.01, elevation=45),
            NodeConfig(node_id="N3", demand=0.01, elevation=43),
        ],
        pipes=[
            PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
            PipeConfig(pipe_id="P2", start_node="N1", end_node="N2", length=400, diameter=0.25),
            PipeConfig(pipe_id="P3", start_node="N2", end_node="N3", length=300, diameter=0.25),
            PipeConfig(pipe_id="P4", start_node="N1", end_node="N3", length=600, diameter=0.20),
        ],
        pumps=[
            PumpConfig(pump_id="PU1", node_id="N1", head_added=10, max_flow=0.05),
        ],
        valves=[
            ValveConfig(valve_id="V1", pipe_id="P4", status="open"),
        ],
    )


class TestSimulateLeakage:
    def test_basic_leakage(self):
        cfg = _make_config_with_pump()
        sr = simulate_leakage(cfg, leak_nodes=["N1", "N2"], leak_coefficient=1e-5)
        assert sr.scenario_name == "漏损模拟"
        assert sr.pressure_changes != {}

    def test_leakage_with_baseline(self):
        cfg = _make_config_with_pump()
        baseline = solve_network(cfg)
        sr = simulate_leakage(cfg, baseline=baseline, leak_nodes=["N1"])
        assert sr.baseline is baseline

    def test_invalid_leak_node(self):
        cfg = _make_config_with_pump()
        sr = simulate_leakage(cfg, leak_nodes=["INVALID"])
        assert "不存在" in sr.description

    def test_leakage_reduces_pressure(self):
        cfg = _make_config_with_pump()
        sr = simulate_leakage(cfg, leak_coefficient=1e-3)
        if sr.baseline.converged and sr.scenario.converged:
            for nid in sr.pressure_changes:
                assert sr.pressure_changes[nid] <= 0.01


class TestSimulateValveClosure:
    def test_close_one_pipe(self):
        cfg = _make_config_with_pump()
        sr = simulate_valve_closure(cfg, closed_pipe_ids=["P4"])
        assert sr.scenario_name == "阀门关闭模拟"

    def test_close_invalid_pipe(self):
        cfg = _make_config_with_pump()
        sr = simulate_valve_closure(cfg, closed_pipe_ids=["INVALID"])
        assert "不存在" in sr.description

    def test_close_critical_pipe_disconnects(self):
        cfg = NetworkConfig(
            nodes=[
                NodeConfig(node_id="S1", is_source=True, source_head=80),
                NodeConfig(node_id="N1", demand=0.01),
            ],
            pipes=[
                PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
            ],
        )
        sr = simulate_valve_closure(cfg, closed_pipe_ids=["P1"])
        assert len(sr.scenario.errors) > 0 or len(sr.scenario.warnings) > 0


class TestSimulatePumpFailure:
    def test_pump_failure(self):
        cfg = _make_config_with_pump()
        sr = simulate_pump_failure(cfg, failed_pump_ids=["PU1"])
        assert sr.scenario_name == "泵站故障模拟"

    def test_invalid_pump(self):
        cfg = _make_config_with_pump()
        sr = simulate_pump_failure(cfg, failed_pump_ids=["INVALID"])
        assert "不存在" in sr.description


class TestMultiLeakageScenarios:
    def test_run_multiple(self):
        cfg = _make_config_with_pump()
        results = run_multi_leakage_scenarios(cfg, leak_coefficients=[1e-6, 1e-5])
        assert len(results) == 2


class TestValveSensitivity:
    def test_sensitivity(self):
        cfg = _make_config_with_pump()
        results = run_valve_sensitivity(cfg)
        assert len(results) == len([p for p in cfg.pipes if p.status == "open"])
