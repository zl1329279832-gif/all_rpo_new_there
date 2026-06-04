import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

import pytest

from config import NodeConfig, PipeConfig, NetworkConfig, MIN_PRESSURE_M
from hydraulic import solve_network
from metrics import (
    compute_leakage_risk,
    identify_supply_shortage,
    rank_critical_nodes,
    compute_network_resilience,
    summarize_results,
)


def _make_config():
    return NetworkConfig(
        nodes=[
            NodeConfig(node_id="S1", is_source=True, source_head=80, elevation=50),
            NodeConfig(node_id="N1", demand=0.02, elevation=48),
            NodeConfig(node_id="N2", demand=0.015, elevation=45),
            NodeConfig(node_id="N3", demand=0.01, elevation=43),
        ],
        pipes=[
            PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
            PipeConfig(pipe_id="P2", start_node="N1", end_node="N2", length=400, diameter=0.25),
            PipeConfig(pipe_id="P3", start_node="N2", end_node="N3", length=300, diameter=0.25),
            PipeConfig(pipe_id="P4", start_node="N1", end_node="N3", length=600, diameter=0.20),
        ],
    )


class TestLeakageRisk:
    def test_basic_risk(self):
        cfg = _make_config()
        result = solve_network(cfg)
        assessments = compute_leakage_risk(cfg, result)
        assert len(assessments) > 0
        for a in assessments:
            assert 0 <= a.risk_score <= 1

    def test_risk_sorted(self):
        cfg = _make_config()
        result = solve_network(cfg)
        assessments = compute_leakage_risk(cfg, result)
        scores = [a.risk_score for a in assessments]
        assert scores == sorted(scores, reverse=True)

    def test_with_pipe_age(self):
        cfg = _make_config()
        result = solve_network(cfg)
        pipe_ages = {"P1": 35, "P2": 5, "P3": 20, "P4": 40}
        assessments = compute_leakage_risk(cfg, result, pipe_age_years=pipe_ages)
        assert len(assessments) > 0


class TestSupplyShortage:
    def test_no_shortage(self):
        cfg = _make_config()
        result = solve_network(cfg)
        if result.converged:
            shortage = identify_supply_shortage(cfg, result)
            assert isinstance(shortage, list)

    def test_shortage_with_high_min_pressure(self):
        cfg = _make_config()
        result = solve_network(cfg)
        if result.converged:
            shortage = identify_supply_shortage(cfg, result, min_pressure=100)
            assert len(shortage) > 0

    def test_shortage_sorted(self):
        cfg = _make_config()
        result = solve_network(cfg)
        shortage = identify_supply_shortage(cfg, result, min_pressure=100)
        if len(shortage) > 1:
            deficits = [s["deficit"] for s in shortage]
            assert deficits == sorted(deficits, reverse=True)


class TestCriticalNodes:
    def test_ranking(self):
        cfg = _make_config()
        result = solve_network(cfg)
        critical = rank_critical_nodes(cfg, result, top_n=5)
        assert len(critical) > 0
        for cn in critical:
            assert cn.rank > 0

    def test_top_n_limit(self):
        cfg = _make_config()
        result = solve_network(cfg)
        critical = rank_critical_nodes(cfg, result, top_n=2)
        assert len(critical) <= 2


class TestResilience:
    def test_basic_resilience(self):
        cfg = _make_config()
        result = solve_network(cfg)
        r = compute_network_resilience(cfg, result)
        assert "resilience_index" in r
        assert "level" in r
        assert 0 <= r["resilience_index"] <= 1

    def test_resilience_level(self):
        cfg = _make_config()
        result = solve_network(cfg)
        r = compute_network_resilience(cfg, result)
        assert r["level"] in ("高韧性", "中等韧性", "低韧性", "脆弱")


class TestSummarizeResults:
    def test_summary_dataframe(self):
        import pandas as pd
        cfg = _make_config()
        result = solve_network(cfg)
        df = summarize_results(cfg, result)
        assert isinstance(df, pd.DataFrame)
        assert len(df) == len(cfg.nodes)
        assert "node_id" in df.columns
        assert "pressure_m" in df.columns
