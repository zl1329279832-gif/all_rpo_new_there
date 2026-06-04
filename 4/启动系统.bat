@echo off
chcp 65001 >nul
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
echo [2/3] 生成演示数据...
python -c "import sys; sys.path.insert(0, '.'); exec(open('data/generate_demo_data.py', encoding='utf-8').read()); save_all_data('data/demo'); print('数据生成成功！')"

echo.
echo [3/3] 启动应用...
echo.
echo 应用启动后，请在浏览器中访问显示的地址（通常是 http://localhost:8501）
echo.
echo 按 Ctrl+C 停止应用
echo ========================================
echo.

streamlit run app.py

pause
