import sys
import os

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

import pytest
import networkx as nx

from config import NodeConfig, PipeConfig, PumpConfig, ValveConfig, NetworkConfig
from network import (
    build_graph,
    check_connectivity,
    find_source_nodes,
    get_supply_paths,
    get_upstream_sources,
    identify_isolated_nodes,
    compute_adjacency,
    build_undirected_supply_graph,
)


def _make_simple_config():
    return NetworkConfig(
        nodes=[
            NodeConfig(node_id="S1", is_source=True, source_head=80, x=0, y=1),
            NodeConfig(node_id="N1", demand=0.02, x=1, y=1),
            NodeConfig(node_id="N2", demand=0.015, x=2, y=1),
        ],
        pipes=[
            PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
            PipeConfig(pipe_id="P2", start_node="N1", end_node="N2", length=400, diameter=0.25),
        ],
    )


def _make_disconnected_config():
    return NetworkConfig(
        nodes=[
            NodeConfig(node_id="S1", is_source=True, source_head=80),
            NodeConfig(node_id="N1", demand=0.02),
            NodeConfig(node_id="N2", demand=0.015),
        ],
        pipes=[
            PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
        ],
    )


class TestBuildGraph:
    def test_simple_graph(self):
        cfg = _make_simple_config()
        G = build_graph(cfg)
        assert len(G.nodes) == 3
        assert G.has_edge("S1", "N1")
        assert G.has_edge("N1", "S1")

    def test_closed_pipe_excluded(self):
        cfg = NetworkConfig(
            nodes=[
                NodeConfig(node_id="S1", is_source=True, source_head=80),
                NodeConfig(node_id="N1", demand=0.02),
            ],
            pipes=[
                PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3, status="closed"),
            ],
        )
        G = build_graph(cfg)
        assert not G.has_edge("S1", "N1")

    def test_valve_closed_excluded(self):
        cfg = NetworkConfig(
            nodes=[
                NodeConfig(node_id="S1", is_source=True, source_head=80),
                NodeConfig(node_id="N1", demand=0.02),
            ],
            pipes=[
                PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
            ],
            valves=[
                ValveConfig(valve_id="V1", pipe_id="P1", status="closed"),
            ],
        )
        G = build_graph(cfg)
        assert not G.has_edge("S1", "N1")

    def test_partial_valve_resistance(self):
        cfg = NetworkConfig(
            nodes=[
                NodeConfig(node_id="S1", is_source=True, source_head=80),
                NodeConfig(node_id="N1", demand=0.02),
            ],
            pipes=[
                PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
            ],
            valves=[
                ValveConfig(valve_id="V1", pipe_id="P1", status="partial", opening_ratio=0.5),
            ],
        )
        G = build_graph(cfg)
        assert G.has_edge("S1", "N1")
        edge_resistance = G["S1"]["N1"]["resistance"]
        base_resistance = cfg.pipes[0].resistance
        assert edge_resistance > base_resistance

    def test_pump_head_added(self):
        cfg = NetworkConfig(
            nodes=[
                NodeConfig(node_id="S1", is_source=True, source_head=80),
                NodeConfig(node_id="N1", demand=0.02),
            ],
            pipes=[
                PipeConfig(pipe_id="P1", start_node="S1", end_node="N1", length=500, diameter=0.3),
            ],
            pumps=[
                PumpConfig(pump_id="PU1", node_id="N1", head_added=15, max_flow=0.1),
            ],
        )
        G = build_graph(cfg)
        assert G.has_edge("S1", "N1")
        assert G["S1"]["N1"].get("pump_head", 0) == 15


class TestCheckConnectivity:
    def test_connected(self):
        cfg = _make_simple_config()
        G = build_graph(cfg)
        info = check_connectivity(G)
        assert info["is_connected"]

    def test_disconnected(self):
        cfg = _make_disconnected_config()
        G = build_graph(cfg)
        info = check_connectivity(G)
        assert not info["is_connected"]
        assert "N2" in info["unreachable_nodes"]


class TestFindSourceNodes:
    def test_find_sources(self):
        cfg = _make_simple_config()
        G = build_graph(cfg)
        sources = find_source_nodes(G)
        assert sources == ["S1"]


class TestSupplyPaths:
    def test_existing_path(self):
        cfg = _make_simple_config()
        G = build_graph(cfg)
        paths = get_supply_paths(G, "S1", "N2")
        assert len(paths) > 0

    def test_no_path(self):
        cfg = _make_disconnected_config()
        G = build_graph(cfg)
        paths = get_supply_paths(G, "S1", "N2")
        assert paths == []


class TestUpstreamSources:
    def test_connected_source(self):
        cfg = _make_simple_config()
        G = build_graph(cfg)
        sources = get_upstream_sources(G, "N2")
        assert "S1" in sources


class TestIsolatedNodes:
    def test_no_isolated(self):
        cfg = _make_simple_config()
        G = build_graph(cfg)
        assert identify_isolated_nodes(G) == []


class TestAdjacency:
    def test_adjacency(self):
        cfg = _make_simple_config()
        G = build_graph(cfg)
        adj = compute_adjacency(G)
        assert "S1" in adj
        assert "N1" in adj["S1"]


class TestUndirectedGraph:
    def test_undirected_build(self):
        cfg = _make_simple_config()
        G = build_undirected_supply_graph(cfg)
        assert isinstance(G, nx.Graph)
        assert G.has_edge("S1", "N1")
