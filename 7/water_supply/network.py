from __future__ import annotations

from typing import Optional

import networkx as nx

from config import NetworkConfig, PipeConfig, ValveConfig


def build_graph(config: NetworkConfig) -> nx.DiGraph:
    G = nx.DiGraph()
    for node in config.nodes:
        G.add_node(
            node.node_id,
            elevation=node.elevation,
            demand=node.demand,
            is_source=node.is_source,
            source_head=node.source_head,
            x=node.x,
            y=node.y,
        )

    closed_pipes = _get_closed_pipe_ids(config)

    for pipe in config.pipes:
        if pipe.pipe_id in closed_pipes:
            continue
        effective_resistance = _compute_effective_resistance(pipe, config.valves)
        G.add_edge(
            pipe.start_node,
            pipe.end_node,
            pipe_id=pipe.pipe_id,
            length=pipe.length,
            diameter=pipe.diameter,
            roughness=pipe.roughness,
            resistance=effective_resistance,
        )
        G.add_edge(
            pipe.end_node,
            pipe.start_node,
            pipe_id=pipe.pipe_id + "_rev",
            length=pipe.length,
            diameter=pipe.diameter,
            roughness=pipe.roughness,
            resistance=effective_resistance,
        )

    for pump in config.pumps:
        for predecessor in G.predecessors(pump.node_id):
            edge_data = G[predecessor][pump.node_id]
            if "pump_head" not in edge_data:
                edge_data["pump_head"] = 0.0
            edge_data["pump_head"] += pump.head_added

    return G


def _get_closed_pipe_ids(config: NetworkConfig) -> set[str]:
    closed = set()
    for pipe in config.pipes:
        if pipe.status == "closed":
            closed.add(pipe.pipe_id)
    for valve in config.valves:
        if valve.status == "closed":
            closed.add(valve.pipe_id)
    return closed


def _compute_effective_resistance(pipe: PipeConfig, valves: list[ValveConfig]) -> float:
    base_resistance = pipe.resistance
    for valve in valves:
        if valve.pipe_id == pipe.pipe_id and valve.status == "partial":
            if valve.opening_ratio > 0:
                base_resistance /= valve.opening_ratio ** 2
            else:
                base_resistance *= 1e12
    return base_resistance


def check_connectivity(G: nx.DiGraph) -> dict:
    source_nodes = [n for n, d in G.nodes(data=True) if d.get("is_source")]
    all_nodes = set(G.nodes)
    reachable = set()
    for src in source_nodes:
        reachable.update(nx.descendants(G, src))
        reachable.add(src)

    unreachable = all_nodes - reachable
    components = list(nx.weakly_connected_components(G))

    return {
        "is_connected": len(unreachable) == 0,
        "unreachable_nodes": list(unreachable),
        "num_components": len(components),
        "components": [list(c) for c in components],
        "source_nodes": source_nodes,
    }


def find_source_nodes(G: nx.DiGraph) -> list[str]:
    return [n for n, d in G.nodes(data=True) if d.get("is_source")]


def get_supply_paths(G: nx.DiGraph, source: str, target: str) -> list[list[str]]:
    if source not in G or target not in G:
        return []
    try:
        return list(nx.all_simple_paths(G, source, target, cutoff=10))
    except nx.NetworkXError:
        return []


def get_upstream_sources(G: nx.DiGraph, node_id: str) -> list[str]:
    sources = find_source_nodes(G)
    connected_sources = []
    for src in sources:
        if nx.has_path(G, src, node_id):
            connected_sources.append(src)
    return connected_sources


def identify_isolated_nodes(G: nx.DiGraph) -> list[str]:
    isolated = list(nx.isolates(G))
    return isolated


def compute_adjacency(G: nx.DiGraph) -> dict[str, list[str]]:
    adj = {}
    for node in G.nodes:
        neighbors = list(G.successors(node))
        predecessors = list(G.predecessors(node))
        adj[node] = list(set(neighbors + predecessors))
    return adj


def get_pipe_between(G: nx.DiGraph, node_a: str, node_b: str) -> Optional[dict]:
    if G.has_edge(node_a, node_b):
        return dict(G[node_a][node_b])
    if G.has_edge(node_b, node_a):
        return dict(G[node_b][node_a])
    return None


def build_undirected_supply_graph(config: NetworkConfig) -> nx.Graph:
    G = nx.Graph()
    for node in config.nodes:
        G.add_node(
            node.node_id,
            elevation=node.elevation,
            demand=node.demand,
            is_source=node.is_source,
            source_head=node.source_head,
            x=node.x,
            y=node.y,
        )

    closed_pipes = _get_closed_pipe_ids(config)
    for pipe in config.pipes:
        if pipe.pipe_id in closed_pipes:
            continue
        effective_resistance = _compute_effective_resistance(pipe, config.valves)
        G.add_edge(
            pipe.start_node,
            pipe.end_node,
            pipe_id=pipe.pipe_id,
            length=pipe.length,
            diameter=pipe.diameter,
            roughness=pipe.roughness,
            resistance=effective_resistance,
        )
    return G
