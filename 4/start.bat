@echo off
echo ========================================
echo    医院门诊运营分析平台
echo ========================================
echo.

echo [1/3] 检查Python环境...
python --version
if errorlevel 1 (
    echo 错误: 未找到Python，请先安装Python
    pause
    exit /b 1
)

echo.
echo [2/3] 检查演示数据...
if not exist "data\demo\departments.csv" (
    echo 正在生成演示数据...
    python data/generate_demo_data.py
) else (
    echo 演示数据已存在
)

echo.
echo [3/3] 启动应用...
echo.
echo 应用启动后，请在浏览器中访问显示的地址（通常是 http://localhost:8501）
echo.
echo 按 Ctrl+C 停止应用
echo ========================================
echo.

streamlit run app.py --server.headless false

pause
