from __future__ import annotations

from typing import Optional

import numpy as np
import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
import networkx as nx

from config import NetworkConfig
from hydraulic import HydraulicResult
from metrics import LeakageRiskAssessment, CriticalNodeInfo
from simulation import ScenarioResult

plt.rcParams["font.sans-serif"] = ["SimHei", "Microsoft YaHei", "DejaVu Sans"]
plt.rcParams["axes.unicode_minus"] = False


def plot_pressure_distribution(result: HydraulicResult, config: Optional[NetworkConfig] = None) -> plt.Figure:
    fig, ax = plt.subplots(figsize=(10, 6))
    nodes = sorted(result.node_pressures.keys())
    pressures = [result.node_pressures[n] for n in nodes]

    colors = []
    for p in pressures:
        if p < 0:
            colors.append("#d32f2f")
        elif p < 15:
            colors.append("#f57c00")
        elif p < 30:
            colors.append("#fbc02d")
        else:
            colors.append("#388e3c")

    bars = ax.bar(nodes, pressures, color=colors, edgecolor="white", linewidth=0.5)
    ax.axhline(y=15, color="red", linestyle="--", linewidth=1, label="最小压力 (15m)")
    ax.set_xlabel("节点 ID")
    ax.set_ylabel("压力 (m)")
    ax.set_title("节点压力分布")
    ax.legend()
    ax.tick_params(axis="x", rotation=45)
    plt.tight_layout()
    return fig


def plot_flow_distribution(result: HydraulicResult) -> plt.Figure:
    fig, ax = plt.subplots(figsize=(10, 6))
    pipes = sorted(result.pipe_flows.keys())
    flows = [result.pipe_flows[p] for p in pipes]

    colors = ["#1565c0" if f >= 0 else "#c62828" for f in flows]
    ax.barh(pipes, flows, color=colors, edgecolor="white", linewidth=0.5)
    ax.set_xlabel("流量 (m³/s)")
    ax.set_ylabel("管段 ID")
    ax.set_title("管段流量分布")
    ax.axvline(x=0, color="gray", linewidth=0.5)
    plt.tight_layout()
    return fig


def plot_leakage_risk(assessments: list[LeakageRiskAssessment]) -> plt.Figure:
    fig, ax = plt.subplots(figsize=(10, 6))
    if not assessments:
        ax.text(0.5, 0.5, "无漏损风险数据", ha="center", va="center", fontsize=14)
        return fig

    nodes = [a.node_id for a in assessments]
    scores = [a.risk_score for a in assessments]

    colors = []
    for s in scores:
        if s > 0.7:
            colors.append("#d32f2f")
        elif s > 0.4:
            colors.append("#f57c00")
        else:
            colors.append("#388e3c")

    ax.barh(nodes, scores, color=colors, edgecolor="white", linewidth=0.5)
    ax.set_xlabel("漏损风险评分")
    ax.set_ylabel("节点 ID")
    ax.set_title("节点漏损风险排行")
    ax.set_xlim(0, 1)
    plt.tight_layout()
    return fig


def plot_network_graph(
    config: NetworkConfig, result: Optional[HydraulicResult] = None
) -> plt.Figure:
    from network import build_undirected_supply_graph

    G = build_undirected_supply_graph(config)
    fig, ax = plt.subplots(figsize=(12, 9))

    pos = {}
    for node in config.nodes:
        pos[node.node_id] = (node.x, node.y)

    if not any(p != (0, 0) for p in pos.values()):
        pos = nx.spring_layout(G, seed=42)

    source_nodes = [n.node_id for n in config.nodes if n.is_source]

    node_colors = []
    node_sizes = []
    if result:
        for n in G.nodes:
            p = result.node_pressures.get(n, 0)
            if n in source_nodes:
                node_colors.append("#1565c0")
                node_sizes.append(500)
            elif p < 0:
                node_colors.append("#d32f2f")
                node_sizes.append(400)
            elif p < 15:
                node_colors.append("#f57c00")
                node_sizes.append(350)
            else:
                node_colors.append("#388e3c")
                node_sizes.append(300)
    else:
        for n in G.nodes:
            node_colors.append("#1565c0" if n in source_nodes else "#66bb6a")
            node_sizes.append(400 if n in source_nodes else 300)

    nx.draw_networkx_nodes(G, pos, node_color=node_colors, node_size=node_sizes, ax=ax)
    nx.draw_networkx_labels(G, pos, font_size=8, font_weight="bold", ax=ax)

    edge_widths = []
    edge_colors = []
    for u, v, data in G.edges(data=True):
        diameter = data.get("diameter", 0.3)
        edge_widths.append(diameter * 5)
        if result:
            pipe_id = data.get("pipe_id", "")
            flow = abs(result.pipe_flows.get(pipe_id, 0))
            if flow > 0.1:
                edge_colors.append("#d32f2f")
            elif flow > 0.05:
                edge_colors.append("#f57c00")
            else:
                edge_colors.append("#42a5f5")
        else:
            edge_colors.append("#90a4ae")

    nx.draw_networkx_edges(G, pos, width=edge_widths, edge_color=edge_colors, alpha=0.7, ax=ax)

    ax.set_title("供水管网拓扑")
    ax.axis("off")
    plt.tight_layout()
    return fig


def plot_scenario_comparison(scenarios: list[ScenarioResult]) -> plt.Figure:
    fig, axes = plt.subplots(1, 2, figsize=(16, 7))

    ax1 = axes[0]
    for sr in scenarios:
        nodes = sorted(sr.scenario.node_pressures.keys())
        pressures = [sr.scenario.node_pressures.get(n, 0) for n in nodes]
        ax1.plot(nodes, pressures, marker="o", markersize=3, label=sr.scenario_name)

    ax1.axhline(y=15, color="red", linestyle="--", linewidth=1)
    ax1.set_xlabel("节点 ID")
    ax1.set_ylabel("压力 (m)")
    ax1.set_title("场景压力对比")
    ax1.legend(fontsize=8)
    ax1.tick_params(axis="x", rotation=45)

    ax2 = axes[1]
    for sr in scenarios:
        nodes = sorted(sr.pressure_changes.keys())
        changes = [sr.pressure_changes.get(n, 0) for n in nodes]
        ax2.plot(nodes, changes, marker="s", markersize=3, label=sr.scenario_name)

    ax2.axhline(y=0, color="gray", linewidth=0.5)
    ax2.set_xlabel("节点 ID")
    ax2.set_ylabel("压力变化 (m)")
    ax2.set_title("场景压力变化量")
    ax2.legend(fontsize=8)
    ax2.tick_params(axis="x", rotation=45)

    plt.tight_layout()
    return fig


def plot_critical_nodes(critical_nodes: list[CriticalNodeInfo]) -> plt.Figure:
    fig, ax = plt.subplots(figsize=(10, 6))
    if not critical_nodes:
        ax.text(0.5, 0.5, "无关键节点数据", ha="center", va="center", fontsize=14)
        return fig

    nodes = [f"{cn.rank}. {cn.node_id}" for cn in critical_nodes]
    scores = [cn.criticality_score for cn in critical_nodes]
    demand_scores = [cn.demand_importance for cn in critical_nodes]
    vuln_scores = [cn.pressure_vulnerability for cn in critical_nodes]
    conn_scores = [cn.connectivity_factor for cn in critical_nodes]

    y_pos = np.arange(len(nodes))
    bar_height = 0.2

    ax.barh(y_pos - 1.5 * bar_height, demand_scores, bar_height, label="需求重要性", color="#1565c0")
    ax.barh(y_pos - 0.5 * bar_height, vuln_scores, bar_height, label="压力脆弱性", color="#f57c00")
    ax.barh(y_pos + 0.5 * bar_height, conn_scores, bar_height, label="连接度", color="#388e3c")
    ax.barh(y_pos + 1.5 * bar_height, scores, bar_height, label="综合评分", color="#7b1fa2")

    ax.set_yticks(y_pos)
    ax.set_yticklabels(nodes)
    ax.set_xlabel("评分")
    ax.set_title("关键节点排行")
    ax.legend(fontsize=8)
    plt.tight_layout()
    return fig


def plot_leakage_curve(
    leak_coefficients: list[float], pressure_drops: list[float], node_id: str = ""
) -> plt.Figure:
    fig, ax = plt.subplots(figsize=(10, 6))
    ax.plot(leak_coefficients, pressure_drops, "b-o", markersize=5, linewidth=2)
    ax.set_xscale("log")
    ax.set_xlabel("漏损系数")
    ax.set_ylabel("压力下降 (m)")
    title = "漏损系数-压力下降曲线"
    if node_id:
        title += f" (节点: {node_id})"
    ax.set_title(title)
    ax.grid(True, alpha=0.3)
    plt.tight_layout()
    return fig


def save_figure(fig: plt.Figure, filepath: str, dpi: int = 150) -> None:
    fig.savefig(filepath, dpi=dpi, bbox_inches="tight")
    plt.close(fig)
