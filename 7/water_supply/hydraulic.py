from __future__ import annotations

from dataclasses import dataclass, field
from typing import Optional

import numpy as np
from scipy.optimize import fsolve
import networkx as nx

from config import NetworkConfig, HW_EXPONENT, MIN_PRESSURE_M
from network import build_graph, check_connectivity


@dataclass
class HydraulicResult:
    node_heads: dict[str, float] = field(default_factory=dict)
    node_pressures: dict[str, float] = field(default_factory=dict)
    pipe_flows: dict[str, float] = field(default_factory=dict)
    pipe_velocities: dict[str, float] = field(default_factory=dict)
    pipe_head_losses: dict[str, float] = field(default_factory=dict)
    converged: bool = False
    iterations: int = 0
    errors: list[str] = field(default_factory=list)
    warnings: list[str] = field(default_factory=list)


def hazen_williams_resistance(length: float, diameter: float, roughness: float) -> float:
    if length <= 0 or diameter <= 0 or roughness <= 0:
        raise ValueError(
            f"Hazen-Williams 参数必须为正: length={length}, diameter={diameter}, roughness={roughness}"
        )
    from config import HW_CONSTANT
    return HW_CONSTANT * length / (roughness ** HW_EXPONENT * diameter ** 4.87)


def compute_head_loss(flow: float, resistance: float) -> float:
    if resistance <= 0:
        return 0.0
    return resistance * np.sign(flow) * np.abs(flow) ** HW_EXPONENT


def compute_flow_from_head_diff(head_diff: float, resistance: float) -> float:
    if resistance <= 0:
        return 0.0
    eps = 1e-10
    abs_hd = max(np.abs(head_diff), eps)
    q = np.sign(head_diff) * (abs_hd / resistance) ** (1.0 / HW_EXPONENT)
    return q


def solve_network(config: NetworkConfig, extra_demands: Optional[dict[str, float]] = None) -> HydraulicResult:
    result = HydraulicResult()
    errors = config.validate()
    if errors:
        result.errors = errors
        return result

    G = build_graph(config)
    conn_info = check_connectivity(G)
    if not conn_info["is_connected"]:
        unreachable = conn_info["unreachable_nodes"]
        result.errors.append(f"管网不连通，以下节点无法从水源到达: {unreachable}")
        return result

    source_nodes = [n for n, d in G.nodes(data=True) if d.get("is_source")]
    demand_nodes = [n for n in G.nodes if n not in source_nodes]

    if not demand_nodes:
        result.warnings.append("管网中无需求节点")
        for n in source_nodes:
            result.node_heads[n] = G.nodes[n].get("source_head", 0.0)
            result.node_pressures[n] = result.node_heads[n] - G.nodes[n].get("elevation", 0.0)
        result.converged = True
        return result

    demands = {}
    for node_id in demand_nodes:
        base_demand = G.nodes[node_id].get("demand", 0.0)
        extra = 0.0
        if extra_demands and node_id in extra_demands:
            extra = extra_demands[node_id]
        demands[node_id] = base_demand + extra

    source_heads = {}
    for src in source_nodes:
        sh = G.nodes[src].get("source_head")
        if sh is None:
            result.errors.append(f"水源节点 '{src}' 缺少 source_head")
            return result
        source_heads[src] = sh

    avg_source_head = np.mean(list(source_heads.values()))
    initial_heads = {}
    for node_id in demand_nodes:
        initial_heads[node_id] = avg_source_head * 0.9

    demand_idx = {nid: i for i, nid in enumerate(demand_nodes)}
    node_idx = {nid: i for i, nid in enumerate(G.nodes)}
    all_nodes_list = list(G.nodes)

    def flow_balance(heads_vec):
        heads = dict(zip(demand_nodes, heads_vec))
        full_heads = {}
        for n in all_nodes_list:
            if n in source_nodes:
                full_heads[n] = source_heads[n]
            else:
                full_heads[n] = heads[n]

        residuals = np.zeros(len(demand_nodes))
        for node_id in demand_nodes:
            net_inflow = 0.0
            for pred in G.predecessors(node_id):
                edge = G[pred][node_id]
                resistance = edge.get("resistance", 1e10)
                pump_head = edge.get("pump_head", 0.0)
                hd = full_heads[pred] + pump_head - full_heads[node_id]
                q = compute_flow_from_head_diff(hd, resistance)
                net_inflow += q

            for succ in G.successors(node_id):
                edge = G[node_id][succ]
                resistance = edge.get("resistance", 1e10)
                pump_head = edge.get("pump_head", 0.0)
                hd = full_heads[node_id] - (full_heads[succ] + pump_head)
                q = compute_flow_from_head_diff(hd, resistance)
                net_inflow -= q

            residuals[demand_idx[node_id]] = net_inflow - demands[node_id]
        return residuals

    x0 = np.array([initial_heads[n] for n in demand_nodes])

    try:
        solution, info, ier, mesg = fsolve(
            flow_balance, x0, full_output=True, maxfev=5000
        )
        if ier == 1:
            result.converged = True
        else:
            result.warnings.append(f"求解器未完全收敛: {mesg}")
            residual_norm = np.linalg.norm(info["fvec"])
            if residual_norm < 1e-3:
                result.converged = True
                result.warnings.append(f"残差范数 {residual_norm:.6f} 较小，结果仍可参考")
    except Exception as e:
        result.errors.append(f"水力求解失败: {e}")
        return result

    for i, node_id in enumerate(demand_nodes):
        result.node_heads[node_id] = solution[i]
    for src in source_nodes:
        result.node_heads[src] = source_heads[src]

    for node_id in all_nodes_list:
        elevation = G.nodes[node_id].get("elevation", 0.0)
        result.node_pressures[node_id] = result.node_heads[node_id] - elevation

    for u, v, data in G.edges(data=True):
        pipe_id = data.get("pipe_id", "")
        if pipe_id.endswith("_rev"):
            continue
        if G.has_edge(u, v):
            resistance = G[u][v].get("resistance", 1e10)
            pump_head = G[u][v].get("pump_head", 0.0)
            hd = result.node_heads[u] + pump_head - result.node_heads[v]
            q = compute_flow_from_head_diff(hd, resistance)
            result.pipe_flows[pipe_id] = q

            diameter = data.get("diameter", 0.3)
            area = np.pi * (diameter / 2) ** 2
            result.pipe_velocities[pipe_id] = abs(q) / area if area > 0 else 0.0
            result.pipe_head_losses[pipe_id] = compute_head_loss(q, resistance)

    for node_id in demand_nodes:
        pressure = result.node_pressures.get(node_id, 0)
        if pressure < MIN_PRESSURE_M:
            result.warnings.append(
                f"节点 '{node_id}' 压力 {pressure:.2f}m 低于最小供水压力 {MIN_PRESSURE_M}m"
            )

    for pipe_id, flow in result.pipe_flows.items():
        if abs(flow) > 1.0:
            result.warnings.append(
                f"管段 '{pipe_id}' 流量 {flow:.4f} m³/s 异常偏大，请检查管径和需求配置"
            )

    return result


def estimate_node_pressure(
    config: NetworkConfig,
    source_node: str,
    target_node: str,
) -> Optional[float]:
    G = build_graph(config)
    try:
        path = nx.shortest_path(G, source_node, target_node, weight="resistance")
    except nx.NetworkXNoPath:
        return None

    source_head = G.nodes[source_node].get("source_head", 0.0)
    current_head = source_head
    for i in range(len(path) - 1):
        u, v = path[i], path[i + 1]
        edge = G[u][v]
        resistance = edge.get("resistance", 0)
        demand = G.nodes[v].get("demand", 0)
        estimated_flow = abs(demand) + 0.01
        hl = compute_head_loss(estimated_flow, resistance)
        pump_head = edge.get("pump_head", 0.0)
        current_head = current_head + pump_head - hl

    elevation = G.nodes[target_node].get("elevation", 0.0)
    return current_head - elevation
