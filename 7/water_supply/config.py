from __future__ import annotations

from dataclasses import dataclass, field
from typing import Optional

import json
import os

GRAVITY = 9.81
WATER_DENSITY = 998.2
HW_EXPONENT = 1.852
HW_CONSTANT = 10.67
MIN_PRESSURE_M = 15.0
MAX_VELOCITY_MS = 3.0
DEFAULT_ROUGHNESS = 130.0


@dataclass
class NodeConfig:
    node_id: str
    elevation: float = 0.0
    demand: float = 0.0
    is_source: bool = False
    source_head: Optional[float] = None
    x: float = 0.0
    y: float = 0.0

    def validate(self):
        errors = []
        if self.is_source and self.source_head is None:
            errors.append(f"水源节点 '{self.node_id}' 缺少 source_head 参数")
        if self.demand < 0:
            errors.append(f"节点 '{self.node_id}' 用水量不能为负值: {self.demand}")
        if self.elevation < 0:
            errors.append(f"节点 '{self.node_id}' 标高不能为负值: {self.elevation}")
        return errors


@dataclass
class PipeConfig:
    pipe_id: str
    start_node: str
    end_node: str
    length: float
    diameter: float
    roughness: float = DEFAULT_ROUGHNESS
    status: str = "open"

    def validate(self):
        errors = []
        if self.length <= 0:
            errors.append(f"管段 '{self.pipe_id}' 长度必须为正: {self.length}")
        if self.diameter <= 0:
            errors.append(f"管段 '{self.pipe_id}' 管径必须为正: {self.diameter}")
        if self.roughness <= 0:
            errors.append(f"管段 '{self.pipe_id}' 粗糙系数必须为正: {self.roughness}")
        if self.status not in ("open", "closed"):
            errors.append(f"管段 '{self.pipe_id}' 状态必须为 'open' 或 'closed': {self.status}")
        return errors

    @property
    def resistance(self) -> float:
        return HW_CONSTANT * self.length / (self.roughness ** HW_EXPONENT * self.diameter ** 4.87)


@dataclass
class PumpConfig:
    pump_id: str
    node_id: str
    head_added: float
    max_flow: float

    def validate(self):
        errors = []
        if self.head_added <= 0:
            errors.append(f"泵站 '{self.pump_id}' 扬程必须为正: {self.head_added}")
        if self.max_flow <= 0:
            errors.append(f"泵站 '{self.pump_id}' 最大流量必须为正: {self.max_flow}")
        return errors


@dataclass
class ValveConfig:
    valve_id: str
    pipe_id: str
    status: str = "open"
    opening_ratio: float = 1.0

    def validate(self):
        errors = []
        if self.status not in ("open", "closed", "partial"):
            errors.append(f"阀门 '{self.valve_id}' 状态无效: {self.status}")
        if self.status == "partial" and not (0.0 < self.opening_ratio < 1.0):
            errors.append(
                f"阀门 '{self.valve_id}' 部分开启时 opening_ratio 应在 (0,1): {self.opening_ratio}"
            )
        return errors


@dataclass
class NetworkConfig:
    nodes: list[NodeConfig] = field(default_factory=list)
    pipes: list[PipeConfig] = field(default_factory=list)
    pumps: list[PumpConfig] = field(default_factory=list)
    valves: list[ValveConfig] = field(default_factory=list)

    def validate(self) -> list[str]:
        errors = []
        node_ids = set()
        for n in self.nodes:
            node_ids.add(n.node_id)
            errors.extend(n.validate())

        pipe_ids = set()
        for p in self.pipes:
            if p.pipe_id in pipe_ids:
                errors.append(f"管段 ID 重复: {p.pipe_id}")
            pipe_ids.add(p.pipe_id)
            if p.start_node not in node_ids:
                errors.append(f"管段 '{p.pipe_id}' 起始节点 '{p.start_node}' 不存在")
            if p.end_node not in node_ids:
                errors.append(f"管段 '{p.pipe_id}' 终止节点 '{p.end_node}' 不存在")
            errors.extend(p.validate())

        pump_ids = set()
        for pk in self.pumps:
            if pk.pump_id in pump_ids:
                errors.append(f"泵站 ID 重复: {pk.pump_id}")
            pump_ids.add(pk.pump_id)
            if pk.node_id not in node_ids:
                errors.append(f"泵站 '{pk.pump_id}' 所在节点 '{pk.node_id}' 不存在")
            errors.extend(pk.validate())

        valve_ids = set()
        for v in self.valves:
            if v.valve_id in valve_ids:
                errors.append(f"阀门 ID 重复: {v.valve_id}")
            valve_ids.add(v.valve_id)
            if v.pipe_id not in pipe_ids:
                errors.append(f"阀门 '{v.valve_id}' 所在管段 '{v.pipe_id}' 不存在")
            errors.extend(v.validate())

        source_count = sum(1 for n in self.nodes if n.is_source)
        if source_count == 0:
            errors.append("管网至少需要一个水源节点")
        if len(self.nodes) < 2:
            errors.append("管网至少需要两个节点")

        return errors

    def get_node(self, node_id: str) -> Optional[NodeConfig]:
        for n in self.nodes:
            if n.node_id == node_id:
                return n
        return None

    def get_pipe(self, pipe_id: str) -> Optional[PipeConfig]:
        for p in self.pipes:
            if p.pipe_id == pipe_id:
                return p
        return None


def load_config_from_json(filepath: str) -> NetworkConfig:
    if not os.path.exists(filepath):
        raise FileNotFoundError(f"配置文件不存在: {filepath}")
    with open(filepath, "r", encoding="utf-8") as f:
        data = json.load(f)

    nodes = [
        NodeConfig(
            node_id=n["node_id"],
            elevation=n.get("elevation", 0.0),
            demand=n.get("demand", 0.0),
            is_source=n.get("is_source", False),
            source_head=n.get("source_head"),
            x=n.get("x", 0.0),
            y=n.get("y", 0.0),
        )
        for n in data.get("nodes", [])
    ]

    pipes = [
        PipeConfig(
            pipe_id=p["pipe_id"],
            start_node=p["start_node"],
            end_node=p["end_node"],
            length=p["length"],
            diameter=p["diameter"],
            roughness=p.get("roughness", DEFAULT_ROUGHNESS),
            status=p.get("status", "open"),
        )
        for p in data.get("pipes", [])
    ]

    pumps = [
        PumpConfig(
            pump_id=pk["pump_id"],
            node_id=pk["node_id"],
            head_added=pk["head_added"],
            max_flow=pk["max_flow"],
        )
        for pk in data.get("pumps", [])
    ]

    valves = [
        ValveConfig(
            valve_id=v["valve_id"],
            pipe_id=v["pipe_id"],
            status=v.get("status", "open"),
            opening_ratio=v.get("opening_ratio", 1.0),
        )
        for v in data.get("valves", [])
    ]

    return NetworkConfig(nodes=nodes, pipes=pipes, pumps=pumps, valves=valves)


def check_missing_params(config: NetworkConfig) -> list[str]:
    warnings = []
    for n in config.nodes:
        if not n.is_source and n.demand == 0.0:
            warnings.append(f"节点 '{n.node_id}' 用水量为 0，请确认是否正确")
        if n.is_source and n.source_head is not None and n.source_head < MIN_PRESSURE_M:
            warnings.append(
                f"水源节点 '{n.node_id}' 水头 {n.source_head}m 低于最小供水压力 {MIN_PRESSURE_M}m"
            )
    for p in config.pipes:
        velocity_limit = MAX_VELOCITY_MS
        area = 3.14159265 * (p.diameter / 2) ** 2
        max_q = velocity_limit * area
        if max_q < 0.001:
            warnings.append(
                f"管段 '{p.pipe_id}' 管径 {p.diameter}m 过小，最大可通过流量仅为 {max_q:.4f} m³/s"
            )
    return warnings
