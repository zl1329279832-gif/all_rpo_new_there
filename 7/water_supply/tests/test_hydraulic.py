import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

import pytest
import numpy as np

from config import NodeConfig, PipeConfig, PumpConfig, NetworkConfig, HW_EXPONENT
from hydraulic import (
    hazen_williams_resistance,
    compute_head_loss,
    compute_flow_from_head_diff,
    solve_network,
    estimate_node_pressure,
)


def _make_simple_config():
    return NetworkConfig(
        nodes=[
            NodeConfig(node_id="S1", is_source=True, source_head=80, elevation=50),
            NodeConfig(node_id="N1", demand=0.01, elevation=48),
            NodeConfig(node_id="N2", demand=0.01, elevation=45),
        ],
        pipes=[
            PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3, roughness=130),
            PipeConfig(pipe_id="P2", start_node="N1", end_node="N2", length=400, diameter=0.25, roughness=130),
        ],
    )


class TestHazenWilliams:
    def test_resistance_positive(self):
        r = hazen_williams_resistance(500, 0.3, 130)
        assert r > 0

    def test_resistance_invalid(self):
        with pytest.raises(ValueError):
            hazen_williams_resistance(0, 0.3, 130)
        with pytest.raises(ValueError):
            hazen_williams_resistance(500, 0, 130)

    def test_head_loss_positive_flow(self):
        r = hazen_williams_resistance(500, 0.3, 130)
        hl = compute_head_loss(0.01, r)
        assert hl > 0

    def test_head_loss_zero_flow(self):
        hl = compute_head_loss(0.0, 100)
        assert hl == 0.0

    def test_head_loss_negative_flow(self):
        r = hazen_williams_resistance(500, 0.3, 130)
        hl = compute_head_loss(-0.01, r)
        assert hl < 0

    def test_flow_from_head_diff(self):
        r = hazen_williams_resistance(500, 0.3, 130)
        hd = 5.0
        q = compute_flow_from_head_diff(hd, r)
        assert q > 0
        hl_back = compute_head_loss(q, r)
        assert abs(hl_back - hd) < 0.1

    def test_flow_negative_head_diff(self):
        r = hazen_williams_resistance(500, 0.3, 130)
        q = compute_flow_from_head_diff(-5.0, r)
        assert q < 0


class TestSolveNetwork:
    def test_simple_solve(self):
        cfg = _make_simple_config()
        result = solve_network(cfg)
        assert result.converged
        assert len(result.node_heads) == 3
        assert len(result.node_pressures) == 3
        assert len(result.pipe_flows) >= 1

    def test_source_head_preserved(self):
        cfg = _make_simple_config()
        result = solve_network(cfg)
        assert abs(result.node_heads["S1"] - 80.0) < 1e-6

    def test_head_decreases_from_source(self):
        cfg = _make_simple_config()
        result = solve_network(cfg)
        h_s1 = result.node_heads["S1"]
        h_n1 = result.node_heads["N1"]
        assert h_n1 <= h_s1 + 1e-3

    def test_disconnected_network_error(self):
        cfg = NetworkConfig(
            nodes=[
                NodeConfig(node_id="S1", is_source=True, source_head=80),
                NodeConfig(node_id="N1", demand=0.01),
                NodeConfig(node_id="N2", demand=0.01),
            ],
            pipes=[
                PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
            ],
        )
        result = solve_network(cfg)
        assert len(result.errors) > 0
        assert any("不连通" in e for e in result.errors)

    def test_no_source_error(self):
        cfg = NetworkConfig(
            nodes=[
                NodeConfig(node_id="N1", demand=0.01),
                NodeConfig(node_id="N2", demand=0.01),
            ],
            pipes=[
                PipeConfig(pipe_id="P1", start_node="N1", end_node="N2", length=500, diameter=0.3),
            ],
        )
        result = solve_network(cfg)
        assert len(result.errors) > 0

    def test_extra_demands(self):
        cfg = _make_simple_config()
        result_base = solve_network(cfg)
        result_extra = solve_network(cfg, extra_demands={"N1": 0.005})
        if result_base.converged and result_extra.converged:
            assert result_extra.node_pressures["N1"] <= result_base.node_pressures["N1"] + 1e-3


class TestEstimateNodePressure:
    def test_simple_estimation(self):
        cfg = _make_simple_config()
        p = estimate_node_pressure(cfg, "S1", "N1")
        assert p is not None
        assert isinstance(p, float)

    def test_unreachable_returns_none(self):
        cfg = NetworkConfig(
            nodes=[
                NodeConfig(node_id="S1", is_source=True, source_head=80),
                NodeConfig(node_id="N2", demand=0.01),
            ],
            pipes=[],
        )
        p = estimate_node_pressure(cfg, "S1", "N2")
        assert p is None
