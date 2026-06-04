import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

import pytest
from config import (
    NodeConfig,
    PipeConfig,
    PumpConfig,
    ValveConfig,
    NetworkConfig,
    load_config_from_json,
    check_missing_params,
    HW_CONSTANT,
    HW_EXPONENT,
    MIN_PRESSURE_M,
)


class TestNodeConfig:
    def test_valid_source_node(self):
        n = NodeConfig(node_id="S1", elevation=50, is_source=True, source_head=80)
        assert n.validate() == []

    def test_source_missing_head(self):
        n = NodeConfig(node_id="S1", is_source=True, source_head=None)
        errors = n.validate()
        assert len(errors) == 1
        assert "source_head" in errors[0]

    def test_negative_demand(self):
        n = NodeConfig(node_id="N1", demand=-0.01)
        errors = n.validate()
        assert len(errors) == 1
        assert "负值" in errors[0]

    def test_negative_elevation(self):
        n = NodeConfig(node_id="N1", elevation=-5)
        errors = n.validate()
        assert any("负值" in e for e in errors)

    def test_demand_node_no_source_head_ok(self):
        n = NodeConfig(node_id="N1", demand=0.02, is_source=False)
        assert n.validate() == []


class TestPipeConfig:
    def test_valid_pipe(self):
        p = PipeConfig(pipe_id="P1", start_node="A", end_node="B", length=500, diameter=0.3)
        assert p.validate() == []

    def test_zero_length(self):
        p = PipeConfig(pipe_id="P1", start_node="A", end_node="B", length=0, diameter=0.3)
        errors = p.validate()
        assert any("长度" in e for e in errors)

    def test_negative_diameter(self):
        p = PipeConfig(pipe_id="P1", start_node="A", end_node="B", length=500, diameter=-0.3)
        errors = p.validate()
        assert any("管径" in e for e in errors)

    def test_invalid_status(self):
        p = PipeConfig(pipe_id="P1", start_node="A", end_node="B", length=500, diameter=0.3, status="broken")
        errors = p.validate()
        assert any("状态" in e for e in errors)

    def test_resistance_calculation(self):
        p = PipeConfig(pipe_id="P1", start_node="A", end_node="B", length=1000, diameter=0.3, roughness=130)
        r = p.resistance
        assert r > 0
        expected = HW_CONSTANT * 1000 / (130 ** HW_EXPONENT * 0.3 ** 4.87)
        assert abs(r - expected) < 1e-10


class TestNetworkConfig:
    def _make_valid_config(self):
        return NetworkConfig(
            nodes=[
                NodeConfig(node_id="S1", is_source=True, source_head=80),
                NodeConfig(node_id="N1", demand=0.02),
            ],
            pipes=[
                PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
            ],
        )

    def test_valid_config(self):
        cfg = self._make_valid_config()
        assert cfg.validate() == []

    def test_no_source(self):
        cfg = NetworkConfig(
            nodes=[NodeConfig(node_id="N1", demand=0.02)],
            pipes=[],
        )
        errors = cfg.validate()
        assert any("水源" in e for e in errors)

    def test_duplicate_pipe_id(self):
        cfg = NetworkConfig(
            nodes=[
                NodeConfig(node_id="S1", is_source=True, source_head=80),
                NodeConfig(node_id="N1", demand=0.02),
            ],
            pipes=[
                PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
                PipeConfig(pipe_id="P1", start_node="N1", end_node="S1", length=500, diameter=0.3),
            ],
        )
        errors = cfg.validate()
        assert any("重复" in e for e in errors)

    def test_pipe_nonexistent_node(self):
        cfg = NetworkConfig(
            nodes=[NodeConfig(node_id="S1", is_source=True, source_head=80)],
            pipes=[
                PipeConfig(pipe_id="P1", start_node="S1", end_node="X1", length=500, diameter=0.3),
            ],
        )
        errors = cfg.validate()
        assert any("不存在" in e for e in errors)

    def test_too_few_nodes(self):
        cfg = NetworkConfig(
            nodes=[NodeConfig(node_id="S1", is_source=True, source_head=80)],
            pipes=[],
        )
        errors = cfg.validate()
        assert any("两个" in e for e in errors)


class TestLoadConfig:
    def test_load_example(self):
        path = os.path.join(os.path.dirname(__file__), "..", "data", "example_network.json")
        if os.path.exists(path):
            cfg = load_config_from_json(path)
            assert len(cfg.nodes) > 0
            assert len(cfg.pipes) > 0
            assert cfg.validate() == []

    def test_file_not_found(self):
        with pytest.raises(FileNotFoundError):
            load_config_from_json("/nonexistent/path.json")


class TestCheckMissingParams:
    def test_zero_demand_warning(self):
        cfg = NetworkConfig(
            nodes=[
                NodeConfig(node_id="S1", is_source=True, source_head=80),
                NodeConfig(node_id="N1", demand=0.0),
            ],
            pipes=[
                PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
            ],
        )
        warnings = check_missing_params(cfg)
        assert any("用水量为 0" in w for w in warnings)

    def test_low_source_head_warning(self):
        cfg = NetworkConfig(
            nodes=[
                NodeConfig(node_id="S1", is_source=True, source_head=10),
                NodeConfig(node_id="N1", demand=0.02),
            ],
            pipes=[
                PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
            ],
        )
        warnings = check_missing_params(cfg)
        assert any("低于最小供水压力" in w for w in warnings)
