import os
import sys
import tempfile

import streamlit as st
import pandas as pd

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from config import (
    NetworkConfig,
    NodeConfig,
    PipeConfig,
    PumpConfig,
    ValveConfig,
    load_config_from_json,
    check_missing_params,
    MIN_PRESSURE_M,
)
from network import build_graph, check_connectivity, find_source_nodes
from hydraulic import solve_network
from simulation import (
    simulate_leakage,
    simulate_valve_closure,
    simulate_pump_failure,
    run_multi_leakage_scenarios,
    run_valve_sensitivity,
)
from metrics import (
    compute_leakage_risk,
    identify_supply_shortage,
    rank_critical_nodes,
    compute_network_resilience,
    summarize_results,
)
from charts import (
    plot_pressure_distribution,
    plot_flow_distribution,
    plot_leakage_risk,
    plot_network_graph,
    plot_scenario_comparison,
    plot_critical_nodes,
    plot_leakage_curve,
)
from export import (
    export_node_pressures,
    export_pipe_flows,
    export_leakage_risk,
    export_critical_nodes,
    export_full_report,
)

EXAMPLE_DATA_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), "data", "example_network.json")


def init_session_state():
    if "config" not in st.session_state:
        st.session_state.config = None
    if "result" not in st.session_state:
        st.session_state.result = None
    if "leakage_assessments" not in st.session_state:
        st.session_state.leakage_assessments = None
    if "critical_nodes" not in st.session_state:
        st.session_state.critical_nodes = None
    if "shortage_nodes" not in st.session_state:
        st.session_state.shortage_nodes = None


def render_sidebar():
    st.sidebar.header("管网配置")

    input_mode = st.sidebar.radio("输入方式", ["加载示例数据", "上传 JSON 文件"])

    config = None
    if input_mode == "加载示例数据":
        if os.path.exists(EXAMPLE_DATA_PATH):
            try:
                config = load_config_from_json(EXAMPLE_DATA_PATH)
                st.sidebar.success("示例数据加载成功")
            except Exception as e:
                st.sidebar.error(f"加载失败: {e}")
        else:
            st.sidebar.warning("示例数据文件不存在，请先创建 data/example_network.json")

    elif input_mode == "上传 JSON 文件":
        uploaded = st.sidebar.file_uploader("选择 JSON 文件", type=["json"])
        if uploaded:
            try:
                with tempfile.NamedTemporaryFile(delete=False, suffix=".json") as tmp:
                    tmp.write(uploaded.getvalue())
                    tmp_path = tmp.name
                config = load_config_from_json(tmp_path)
                os.unlink(tmp_path)
                st.sidebar.success("文件加载成功")
            except Exception as e:
                st.sidebar.error(f"加载失败: {e}")

    if config:
        errors = config.validate()
        if errors:
            st.sidebar.error("配置验证失败:")
            for e in errors:
                st.sidebar.error(f"  - {e}")
            return None

        warnings = check_missing_params(config)
        if warnings:
            with st.sidebar.expander("参数警告", expanded=False):
                for w in warnings:
                    st.sidebar.warning(w)

    st.sidebar.markdown("---")
    st.sidebar.header("参数调整")
    min_pressure = st.sidebar.slider("最小供水压力 (m)", 5.0, 30.0, MIN_PRESSURE_M, 0.5, key="min_pressure_slider")
    leak_coeff_options = [1e-7, 5e-7, 1e-6, 5e-6, 1e-5, 5e-5, 1e-4, 5e-4, 1e-3]
    leak_coeff_default_idx = leak_coeff_options.index(1e-5)
    leak_coeff = st.sidebar.select_slider(
        "漏损系数",
        options=leak_coeff_options,
        value=leak_coeff_options[leak_coeff_default_idx],
        format_func=lambda x: f"{x:.1e}",
        key="leak_coeff_slider",
    )
    leak_exp = st.sidebar.slider("漏损压力指数", 0.3, 1.0, 0.5, 0.05, key="leak_exp_slider")

    st.sidebar.markdown("---")
    st.sidebar.markdown("### 当前参数")
    st.sidebar.info(
        f"最小压力: **{min_pressure} m**\n\n"
        f"漏损系数: **{leak_coeff:.1e}**\n\n"
        f"漏损指数: **{leak_exp:.2f}**"
    )

    return config, min_pressure, leak_coeff, leak_exp


def render_overview(config, result, min_pressure):
    st.header("管网概览")

    col1, col2, col3, col4 = st.columns(4)
    with col1:
        st.metric("节点数", len(config.nodes))
    with col2:
        st.metric("管段数", len(config.pipes))
    with col3:
        st.metric("水源数", sum(1 for n in config.nodes if n.is_source))
    with col4:
        st.metric("泵站数", len(config.pumps))

    G = build_graph(config)
    conn_info = check_connectivity(G)
    if conn_info["is_connected"]:
        st.success("管网连通性检查: 通过")
    else:
        st.error(f"管网不连通! 不可达节点: {conn_info['unreachable_nodes']}")

    tab_net, tab_data = st.tabs(["管网拓扑", "节点数据"])
    with tab_net:
        fig = plot_network_graph(config, result, min_pressure)
        st.pyplot(fig)
    with tab_data:
        df = summarize_results(config, result)
        st.dataframe(df, use_container_width=True)


def render_hydraulic(result, min_pressure):
    st.header("水力计算结果")

    col1, col2 = st.columns(2)
    with col1:
        if result.converged:
            st.success("求解状态: 已收敛")
        else:
            st.warning("求解状态: 未完全收敛")

        if result.warnings:
            with st.expander("计算警告"):
                for w in result.warnings:
                    st.warning(w)

        if result.errors:
            with st.expander("计算错误"):
                for e in result.errors:
                    st.error(e)

    with col2:
        pressures = list(result.node_pressures.values())
        if pressures:
            avg_p = sum(pressures) / len(pressures)
            min_p = min(pressures)
            max_p = max(pressures)
            st.metric("平均压力", f"{avg_p:.2f} m")
            st.metric("最低压力", f"{min_p:.2f} m")
            st.metric("最高压力", f"{max_p:.2f} m")

    tab_p, tab_f = st.tabs(["压力分布", "流量分布"])
    with tab_p:
        fig = plot_pressure_distribution(result, min_pressure=min_pressure)
        st.pyplot(fig)
    with tab_f:
        fig = plot_flow_distribution(result)
        st.pyplot(fig)


def render_simulation(config, result, leak_coeff, leak_exp, min_pressure):
    st.header("场景模拟")

    tab_leak, tab_valve, tab_pump = st.tabs(["漏损模拟", "阀门关闭模拟", "泵站故障模拟"])

    with tab_leak:
        st.subheader("漏损场景模拟")
        auto_preview = st.checkbox("实时预览漏损效果", value=True, key="leak_auto_preview")
        leak_nodes = st.multiselect(
            "选择漏损节点",
            [n.node_id for n in config.nodes if not n.is_source],
            default=[n.node_id for n in config.nodes if not n.is_source][:3],
            key="leak_nodes",
        )

        if auto_preview and leak_nodes:
            with st.spinner("正在计算漏损场景..."):
                sr = simulate_leakage(
                    config,
                    baseline=result,
                    leak_nodes=leak_nodes,
                    leak_coefficient=leak_coeff,
                    pressure_exponent=leak_exp,
                )
                _display_scenario_result(sr, min_pressure)
        elif st.button("运行漏损模拟", key="btn_leak"):
            with st.spinner("正在计算..."):
                sr = simulate_leakage(
                    config,
                    baseline=result,
                    leak_nodes=leak_nodes,
                    leak_coefficient=leak_coeff,
                    pressure_exponent=leak_exp,
                )
                _display_scenario_result(sr, min_pressure)

        st.subheader("多漏损系数对比")
        if st.button("运行漏损系数敏感性分析", key="btn_leak_multi"):
            with st.spinner("正在计算..."):
                scenarios = run_multi_leakage_scenarios(config)
                fig = plot_scenario_comparison(scenarios, min_pressure)
                st.pyplot(fig)

    with tab_valve:
        st.subheader("阀门关闭影响分析")
        open_pipes = [p.pipe_id for p in config.pipes if p.status == "open"]
        auto_valve_preview = st.checkbox("实时预览阀门关闭效果", value=True, key="valve_auto_preview")
        closed_pipes = st.multiselect(
            "选择关闭的管段", open_pipes, key="closed_pipes"
        )
        if auto_valve_preview and closed_pipes:
            with st.spinner("正在计算阀门关闭场景..."):
                sr = simulate_valve_closure(
                    config, baseline=result, closed_pipe_ids=closed_pipes
                )
                _display_scenario_result(sr, min_pressure)
        elif st.button("运行阀门关闭模拟", key="btn_valve"):
            if not closed_pipes:
                st.warning("请至少选择一个管段关闭")
            else:
                with st.spinner("正在计算..."):
                    sr = simulate_valve_closure(
                        config, baseline=result, closed_pipe_ids=closed_pipes
                    )
                    _display_scenario_result(sr, min_pressure)

    with tab_pump:
        st.subheader("泵站故障模拟")
        pump_ids = [p.pump_id for p in config.pumps]
        if pump_ids:
            auto_pump_preview = st.checkbox("实时预览泵站故障效果", value=True, key="pump_auto_preview")
            failed_pumps = st.multiselect(
                "选择故障泵站", pump_ids, key="failed_pumps"
            )
            if auto_pump_preview and failed_pumps:
                with st.spinner("正在计算泵站故障场景..."):
                    sr = simulate_pump_failure(
                        config, baseline=result, failed_pump_ids=failed_pumps
                    )
                    _display_scenario_result(sr, min_pressure)
            elif st.button("运行泵站故障模拟", key="btn_pump"):
                if not failed_pumps:
                    st.warning("请至少选择一个泵站")
                else:
                    with st.spinner("正在计算..."):
                        sr = simulate_pump_failure(
                            config, baseline=result, failed_pump_ids=failed_pumps
                        )
                        _display_scenario_result(sr, min_pressure)
        else:
            st.info("当前管网无泵站配置")


def render_metrics(config, result, min_pressure):
    st.header("分析指标")

    leakage_asmts = compute_leakage_risk(config, result)
    shortage = identify_supply_shortage(config, result, min_pressure)
    critical = rank_critical_nodes(config, result, top_n=10)

    tab_leak, tab_short, tab_crit, tab_resil = st.tabs(
        ["漏损风险", "供水不足", "关键节点", "韧性评估"]
    )

    with tab_leak:
        fig = plot_leakage_risk(leakage_asmts)
        st.pyplot(fig)
        if leakage_asmts:
            df = pd.DataFrame([{
                "节点": a.node_id,
                "风险评分": a.risk_score,
                "压力等级": a.pressure_level,
            } for a in leakage_asmts])
            st.dataframe(df, use_container_width=True)

    with tab_short:
        if shortage:
            st.error(f"发现 {len(shortage)} 个供水不足节点!")
            df = pd.DataFrame(shortage)
            st.dataframe(df, use_container_width=True)
        else:
            st.success("所有节点压力满足最小供水要求")

    with tab_crit:
        fig = plot_critical_nodes(critical)
        st.pyplot(fig)
        if critical:
            df = pd.DataFrame([{
                "排名": cn.rank,
                "节点": cn.node_id,
                "综合评分": cn.criticality_score,
                "需求重要性": cn.demand_importance,
                "压力脆弱性": cn.pressure_vulnerability,
                "连接度": cn.connectivity_factor,
            } for cn in critical])
            st.dataframe(df, use_container_width=True)

    with tab_resil:
        resilience = compute_network_resilience(config, result)
        st.metric("韧性指数", f"{resilience['resilience_index']:.4f}")
        st.metric("韧性等级", resilience["level"])
        st.metric("压力达标率", resilience["adequate_node_ratio"])
        st.metric("平均压力", f"{resilience['avg_pressure']:.2f} m")

    return leakage_asmts, shortage, critical


def render_export(config, result, leakage_asmts, critical, shortage):
    st.header("结果导出")

    col1, col2, col3 = st.columns(3)

    with col1:
        if st.button("导出节点压力 CSV", key="exp_pressure"):
            path = export_node_pressures(result, "simulation_results/node_pressures.csv")
            st.success(f"已导出: {path}")

        if st.button("导出管段流量 CSV", key="exp_flow"):
            path = export_pipe_flows(result, "simulation_results/pipe_flows.csv")
            st.success(f"已导出: {path}")

    with col2:
        if st.button("导出漏损风险 CSV", key="exp_leak"):
            if leakage_asmts:
                path = export_leakage_risk(leakage_asmts, "simulation_results/leakage_risk.csv")
                st.success(f"已导出: {path}")
            else:
                st.warning("请先运行漏损风险分析")

        if st.button("导出关键节点 CSV", key="exp_crit"):
            if critical:
                path = export_critical_nodes(critical, "simulation_results/critical_nodes.csv")
                st.success(f"已导出: {path}")
            else:
                st.warning("请先运行关键节点分析")

    with col3:
        if st.button("导出完整报告 CSV", key="exp_full"):
            path = export_full_report(
                config, result,
                leakage_assessments=leakage_asmts,
                critical_nodes=critical,
                shortage_nodes=shortage,
                filepath="simulation_results/full_report.csv",
            )
            st.success(f"已导出: {path}")


def _display_scenario_result(sr, min_pressure):
    st.markdown(f"**场景**: {sr.scenario_name}")
    st.markdown(f"**描述**: {sr.description}")

    if sr.scenario.errors:
        for e in sr.scenario.errors:
            st.error(e)
        return

    col1, col2 = st.columns(2)
    with col1:
        st.markdown("**场景压力**")
        fig = plot_pressure_distribution(sr.scenario, min_pressure=min_pressure)
        st.pyplot(fig)
    with col2:
        st.markdown("**压力变化**")
        if sr.pressure_changes:
            nodes = sorted(sr.pressure_changes.keys())
            changes = [sr.pressure_changes[n] for n in nodes]
            import matplotlib.pyplot as plt
            fig, ax = plt.subplots(figsize=(10, 6))
            colors = ["#d32f2f" if c < -1 else "#f57c00" if c < 0 else "#388e3c" for c in changes]
            ax.bar(nodes, changes, color=colors)
            ax.axhline(y=0, color="gray", linewidth=0.5)
            ax.set_xlabel("节点 ID")
            ax.set_ylabel("压力变化 (m)")
            ax.set_title(f"{sr.scenario_name} - 压力变化")
            plt.xticks(rotation=45)
            plt.tight_layout()
            st.pyplot(fig)

    if sr.new_shortage_nodes:
        st.error(f"新增供水不足节点: {sr.new_shortage_nodes}")
    else:
        st.success("未新增供水不足节点")


def main():
    st.set_page_config(page_title="城市供水管网运行分析", layout="wide")
    st.title("城市供水管网运行分析系统")

    init_session_state()
    sidebar_result = render_sidebar()

    if sidebar_result is None:
        st.info("请在左侧加载管网配置数据")
        return

    config, min_pressure, leak_coeff, leak_exp = sidebar_result

    if config is None:
        st.info("请在左侧加载管网配置数据")
        return

    if st.session_state.result is None or st.button("运行水力计算", type="primary"):
        with st.spinner("正在求解管网水力方程..."):
            result = solve_network(config)
            st.session_state.result = result
            st.session_state.config = config

    result = st.session_state.result
    if result.errors:
        st.error("水力计算存在错误，请检查配置:")
        for e in result.errors:
            st.error(f"  - {e}")
        return

    if not result.converged:
        st.warning("水力求解未完全收敛，结果仅供参考")

    render_overview(config, result, min_pressure)
    st.divider()
    render_hydraulic(result, min_pressure)
    st.divider()
    render_simulation(config, result, leak_coeff, leak_exp, min_pressure)
    st.divider()
    leakage_asmts, shortage, critical = render_metrics(config, result, min_pressure)
    st.divider()
    render_export(config, result, leakage_asmts, critical, shortage)


if __name__ == "__main__":
    main()
