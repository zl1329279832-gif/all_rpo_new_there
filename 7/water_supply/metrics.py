from __future__ import annotations

from dataclasses import dataclass, field
from typing import Optional

import numpy as np
import pandas as pd

from config import NetworkConfig, MIN_PRESSURE_M
from hydraulic import HydraulicResult
from simulation import ScenarioResult


@dataclass
class LeakageRiskAssessment:
    node_id: str
    risk_score: float = 0.0
    pressure_level: str = ""
    velocity_factor: float = 0.0
    age_factor: float = 1.0
    detail: str = ""


@dataclass
class CriticalNodeInfo:
    node_id: str
    criticality_score: float = 0.0
    demand_importance: float = 0.0
    pressure_vulnerability: float = 0.0
    connectivity_factor: float = 0.0
    rank: int = 0


def compute_leakage_risk(
    config: NetworkConfig,
    result: HydraulicResult,
    pipe_age_years: Optional[dict[str, float]] = None,
) -> list[LeakageRiskAssessment]:
    assessments = []
    for node in config.nodes:
        if node.is_source:
            continue

        pressure = result.node_pressures.get(node.node_id, 0)

        if pressure > 60:
            pressure_level = "高压"
            pressure_risk = 0.8
        elif pressure > 40:
            pressure_level = "中高压"
            pressure_risk = 0.5
        elif pressure > MIN_PRESSURE_M:
            pressure_level = "正常"
            pressure_risk = 0.2
        elif pressure > 0:
            pressure_level = "低压"
            pressure_risk = 0.1
        else:
            pressure_level = "负压"
            pressure_risk = 0.9

        connected_pipes = [
            p for p in config.pipes
            if p.start_node == node.node_id or p.end_node == node.node_id
        ]

        max_velocity = 0.0
        for pipe in connected_pipes:
            vel = result.pipe_velocities.get(pipe.pipe_id, 0)
            max_velocity = max(max_velocity, vel)

        if max_velocity > 2.5:
            velocity_factor = 0.8
        elif max_velocity > 1.5:
            velocity_factor = 0.4
        else:
            velocity_factor = 0.1

        age_factor = 1.0
        if pipe_age_years:
            ages = []
            for pipe in connected_pipes:
                age = pipe_age_years.get(pipe.pipe_id, 0)
                ages.append(age)
            if ages:
                avg_age = np.mean(ages)
                if avg_age > 30:
                    age_factor = 1.5
                elif avg_age > 20:
                    age_factor = 1.2
                elif avg_age > 10:
                    age_factor = 1.0
                else:
                    age_factor = 0.8

        risk_score = (pressure_risk * 0.4 + velocity_factor * 0.3 + (age_factor - 0.8) / 0.7 * 0.3)
        risk_score = np.clip(risk_score, 0, 1)

        detail = (
            f"压力等级={pressure_level}({pressure:.1f}m), "
            f"最大流速={max_velocity:.2f}m/s, "
            f"管龄系数={age_factor:.1f}"
        )

        assessments.append(
            LeakageRiskAssessment(
                node_id=node.node_id,
                risk_score=round(float(risk_score), 4),
                pressure_level=pressure_level,
                velocity_factor=round(float(velocity_factor), 4),
                age_factor=round(float(age_factor), 4),
                detail=detail,
            )
        )

    assessments.sort(key=lambda a: a.risk_score, reverse=True)
    return assessments


def identify_supply_shortage(
    config: NetworkConfig,
    result: HydraulicResult,
    min_pressure: float = MIN_PRESSURE_M,
) -> list[dict]:
    shortage_nodes = []
    for node in config.nodes:
        if node.is_source:
            continue
        pressure = result.node_pressures.get(node.node_id, 0)
        deficit = min_pressure - pressure
        if deficit > 0:
            shortage_nodes.append({
                "node_id": node.node_id,
                "pressure": round(pressure, 4),
                "min_required": min_pressure,
                "deficit": round(deficit, 4),
                "demand": node.demand,
                "severity": "严重" if deficit > 10 else "中度" if deficit > 5 else "轻微",
            })

    shortage_nodes.sort(key=lambda x: x["deficit"], reverse=True)
    return shortage_nodes


def rank_critical_nodes(
    config: NetworkConfig,
    result: HydraulicResult,
    scenario_results: Optional[list[ScenarioResult]] = None,
    top_n: int = 10,
) -> list[CriticalNodeInfo]:
    import networkx as nx
    from network import build_graph

    G = build_graph(config)

    total_demand = sum(n.demand for n in config.nodes if not n.is_source)
    if total_demand == 0:
        total_demand = 1.0

    avg_source_head = np.mean(
        [G.nodes[s].get("source_head", 50) for s in G.nodes if G.nodes[s].get("is_source")]
    ) if any(G.nodes[s].get("is_source") for s in G.nodes) else 50.0

    betweenness = nx.betweenness_centrality(G.to_undirected())

    nodes_info = []
    for node in config.nodes:
        if node.is_source:
            continue

        demand_importance = node.demand / total_demand

        pressure = result.node_pressures.get(node.node_id, 0)
        pressure_vulnerability = 1.0 - min(pressure / max(avg_source_head, 1), 1.0)

        connectivity_factor = betweenness.get(node.node_id, 0)

        criticality_score = (
            demand_importance * 0.35
            + pressure_vulnerability * 0.35
            + connectivity_factor * 0.30
        )

        if scenario_results:
            pressure_drops = []
            for sr in scenario_results:
                drop = sr.pressure_changes.get(node.node_id, 0)
                pressure_drops.append(abs(drop))
            if pressure_drops:
                avg_drop = np.mean(pressure_drops)
                max_drop = np.max(pressure_drops)
                scenario_vulnerability = (avg_drop + max_drop) / (2 * max(avg_source_head, 1))
                criticality_score = criticality_score * 0.7 + scenario_vulnerability * 0.3

        nodes_info.append(
            CriticalNodeInfo(
                node_id=node.node_id,
                criticality_score=round(float(criticality_score), 6),
                demand_importance=round(float(demand_importance), 6),
                pressure_vulnerability=round(float(pressure_vulnerability), 6),
                connectivity_factor=round(float(connectivity_factor), 6),
            )
        )

    nodes_info.sort(key=lambda x: x.criticality_score, reverse=True)
    for i, info in enumerate(nodes_info[:top_n]):
        info.rank = i + 1

    return nodes_info[:top_n]


def compute_network_resilience(
    config: NetworkConfig,
    result: HydraulicResult,
    scenario_results: Optional[list[ScenarioResult]] = None,
) -> dict:
    demand_nodes = [n for n in config.nodes if not n.is_source]
    if not demand_nodes:
        return {"resilience_index": 1.0, "description": "无需求节点"}

    adequate_nodes = sum(
        1 for n in demand_nodes
        if result.node_pressures.get(n.node_id, 0) >= MIN_PRESSURE_M
    )
    pressure_adequacy = adequate_nodes / len(demand_nodes)

    pressures = [result.node_pressures.get(n.node_id, 0) for n in demand_nodes]
    avg_pressure = np.mean(pressures) if pressures else 0
    pressure_surplus = max(avg_pressure - MIN_PRESSURE_M, 0) / max(avg_pressure, 1)

    redundancy = 0.0
    if scenario_results:
        worst_case_adequacy = 1.0
        for sr in scenario_results:
            adequate_in_scenario = sum(
                1 for n in demand_nodes
                if sr.scenario.node_pressures.get(n.node_id, 0) >= MIN_PRESSURE_M
            )
            ratio = adequate_in_scenario / len(demand_nodes)
            worst_case_adequacy = min(worst_case_adequacy, ratio)
        redundancy = worst_case_adequacy
    else:
        redundancy = 0.5

    resilience_index = (
        pressure_adequacy * 0.4 + pressure_surplus * 0.3 + redundancy * 0.3
    )

    if resilience_index >= 0.8:
        level = "高韧性"
    elif resilience_index >= 0.6:
        level = "中等韧性"
    elif resilience_index >= 0.4:
        level = "低韧性"
    else:
        level = "脆弱"

    return {
        "resilience_index": round(float(resilience_index), 4),
        "pressure_adequacy": round(float(pressure_adequacy), 4),
        "pressure_surplus_ratio": round(float(pressure_surplus), 4),
        "redundancy": round(float(redundancy), 4),
        "level": level,
        "avg_pressure": round(float(avg_pressure), 2),
        "adequate_node_ratio": f"{adequate_nodes}/{len(demand_nodes)}",
    }


def summarize_results(
    config: NetworkConfig,
    result: HydraulicResult,
) -> pd.DataFrame:
    rows = []
    for node in config.nodes:
        rows.append({
            "node_id": node.node_id,
            "type": "水源" if node.is_source else "需求",
            "elevation_m": node.elevation,
            "demand_Ls": node.demand * 1000,
            "head_m": round(result.node_heads.get(node.node_id, 0), 4),
            "pressure_m": round(result.node_pressures.get(node.node_id, 0), 4),
        })
    return pd.DataFrame(rows)
