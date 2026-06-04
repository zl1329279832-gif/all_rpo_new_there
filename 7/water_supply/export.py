from __future__ import annotations

import os
from typing import Optional

import pandas as pd

from config import NetworkConfig
from hydraulic import HydraulicResult
from metrics import LeakageRiskAssessment, CriticalNodeInfo


def export_node_pressures(result: HydraulicResult, filepath: str) -> str:
    rows = []
    for node_id, pressure in sorted(result.node_pressures.items()):
        head = result.node_heads.get(node_id, 0)
        rows.append({
            "node_id": node_id,
            "head_m": round(head, 4),
            "pressure_m": round(pressure, 4),
        })
    df = pd.DataFrame(rows)
    _ensure_dir(filepath)
    df.to_csv(filepath, index=False, encoding="utf-8-sig")
    return filepath


def export_pipe_flows(result: HydraulicResult, filepath: str) -> str:
    rows = []
    for pipe_id, flow in sorted(result.pipe_flows.items()):
        velocity = result.pipe_velocities.get(pipe_id, 0)
        head_loss = result.pipe_head_losses.get(pipe_id, 0)
        rows.append({
            "pipe_id": pipe_id,
            "flow_m3s": round(flow, 6),
            "velocity_ms": round(velocity, 4),
            "head_loss_m": round(head_loss, 4),
        })
    df = pd.DataFrame(rows)
    _ensure_dir(filepath)
    df.to_csv(filepath, index=False, encoding="utf-8-sig")
    return filepath


def export_leakage_risk(assessments: list[LeakageRiskAssessment], filepath: str) -> str:
    rows = []
    for a in assessments:
        rows.append({
            "node_id": a.node_id,
            "risk_score": a.risk_score,
            "pressure_level": a.pressure_level,
            "velocity_factor": a.velocity_factor,
            "age_factor": a.age_factor,
            "detail": a.detail,
        })
    df = pd.DataFrame(rows)
    _ensure_dir(filepath)
    df.to_csv(filepath, index=False, encoding="utf-8-sig")
    return filepath


def export_critical_nodes(critical_nodes: list[CriticalNodeInfo], filepath: str) -> str:
    rows = []
    for cn in critical_nodes:
        rows.append({
            "rank": cn.rank,
            "node_id": cn.node_id,
            "criticality_score": cn.criticality_score,
            "demand_importance": cn.demand_importance,
            "pressure_vulnerability": cn.pressure_vulnerability,
            "connectivity_factor": cn.connectivity_factor,
        })
    df = pd.DataFrame(rows)
    _ensure_dir(filepath)
    df.to_csv(filepath, index=False, encoding="utf-8-sig")
    return filepath


def export_full_report(
    config: NetworkConfig,
    result: HydraulicResult,
    leakage_assessments: Optional[list[LeakageRiskAssessment]] = None,
    critical_nodes: Optional[list[CriticalNodeInfo]] = None,
    shortage_nodes: Optional[list[dict]] = None,
    filepath: str = "simulation_results/full_report.csv",
) -> str:
    rows = []

    for node in config.nodes:
        row = {
            "node_id": node.node_id,
            "type": "水源" if node.is_source else "需求",
            "elevation_m": node.elevation,
            "demand_Ls": round(node.demand * 1000, 4),
            "head_m": round(result.node_heads.get(node.node_id, 0), 4),
            "pressure_m": round(result.node_pressures.get(node.node_id, 0), 4),
        }

        if leakage_assessments:
            for la in leakage_assessments:
                if la.node_id == node.node_id:
                    row["leakage_risk_score"] = la.risk_score
                    row["pressure_level"] = la.pressure_level
                    break

        if critical_nodes:
            for cn in critical_nodes:
                if cn.node_id == node.node_id:
                    row["criticality_rank"] = cn.rank
                    row["criticality_score"] = cn.criticality_score
                    break

        if shortage_nodes:
            for sn in shortage_nodes:
                if sn["node_id"] == node.node_id:
                    row["shortage_deficit_m"] = sn["deficit"]
                    row["shortage_severity"] = sn["severity"]
                    break

        rows.append(row)

    df = pd.DataFrame(rows)
    _ensure_dir(filepath)
    df.to_csv(filepath, index=False, encoding="utf-8-sig")
    return filepath


def export_scenario_comparison(
    scenario_results: list, filepath: str
) -> str:
    rows = []
    for sr in scenario_results:
        for node_id in sr.scenario.node_pressures:
            baseline_p = sr.baseline.node_pressures.get(node_id, 0)
            scenario_p = sr.scenario.node_pressures.get(node_id, 0)
            change = sr.pressure_changes.get(node_id, 0)
            rows.append({
                "scenario": sr.scenario_name,
                "node_id": node_id,
                "baseline_pressure_m": round(baseline_p, 4),
                "scenario_pressure_m": round(scenario_p, 4),
                "pressure_change_m": round(change, 4),
            })
    df = pd.DataFrame(rows)
    _ensure_dir(filepath)
    df.to_csv(filepath, index=False, encoding="utf-8-sig")
    return filepath


def _ensure_dir(filepath: str) -> None:
    dirpath = os.path.dirname(filepath)
    if dirpath and not os.path.exists(dirpath):
        os.makedirs(dirpath, exist_ok=True)
