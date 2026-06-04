import sys
import os
import tempfile

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

import pytest
import pandas as pd

from config import NodeConfig, PipeConfig, NetworkConfig
from hydraulic import solve_network
from metrics import compute_leakage_risk, identify_supply_shortage, rank_critical_nodes
from export import (
    export_node_pressures,
    export_pipe_flows,
    export_leakage_risk,
    export_critical_nodes,
    export_full_report,
)


def _make_config():
    return NetworkConfig(
        nodes=[
            NodeConfig(node_id="S1", is_source=True, source_head=80, elevation=50),
            NodeConfig(node_id="N1", demand=0.02, elevation=48),
            NodeConfig(node_id="N2", demand=0.015, elevation=45),
        ],
        pipes=[
            PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
            PipeConfig(pipe_id="P2", start_node="N1", end_node="N2", length=400, diameter=0.25),
        ],
    )


class TestExportNodePressures:
    def test_export(self):
        cfg = _make_config()
        result = solve_network(cfg)
        with tempfile.NamedTemporaryFile(suffix=".csv", delete=False) as f:
            path = f.name
        try:
            export_node_pressures(result, path)
            df = pd.read_csv(path)
            assert len(df) > 0
            assert "node_id" in df.columns
            assert "pressure_m" in df.columns
        finally:
            os.unlink(path)


class TestExportPipeFlows:
    def test_export(self):
        cfg = _make_config()
        result = solve_network(cfg)
        with tempfile.NamedTemporaryFile(suffix=".csv", delete=False) as f:
            path = f.name
        try:
            export_pipe_flows(result, path)
            df = pd.read_csv(path)
            assert len(df) > 0
            assert "pipe_id" in df.columns
            assert "flow_m3s" in df.columns
        finally:
            os.unlink(path)


class TestExportLeakageRisk:
    def test_export(self):
        cfg = _make_config()
        result = solve_network(cfg)
        assessments = compute_leakage_risk(cfg, result)
        with tempfile.NamedTemporaryFile(suffix=".csv", delete=False) as f:
            path = f.name
        try:
            export_leakage_risk(assessments, path)
            df = pd.read_csv(path)
            assert len(df) > 0
            assert "risk_score" in df.columns
        finally:
            os.unlink(path)


class TestExportCriticalNodes:
    def test_export(self):
        cfg = _make_config()
        result = solve_network(cfg)
        critical = rank_critical_nodes(cfg, result)
        with tempfile.NamedTemporaryFile(suffix=".csv", delete=False) as f:
            path = f.name
        try:
            export_critical_nodes(critical, path)
            df = pd.read_csv(path)
            assert len(df) > 0
            assert "rank" in df.columns
        finally:
            os.unlink(path)


class TestExportFullReport:
    def test_export(self):
        cfg = _make_config()
        result = solve_network(cfg)
        with tempfile.NamedTemporaryFile(suffix=".csv", delete=False) as f:
            path = f.name
        try:
            export_full_report(cfg, result, filepath=path)
            df = pd.read_csv(path)
            assert len(df) == len(cfg.nodes)
            assert "pressure_m" in df.columns
        finally:
            os.unlink(path)
