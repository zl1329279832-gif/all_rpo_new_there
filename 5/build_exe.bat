@echo off
echo ========================================
echo 实验室样品管理系统 - 打包脚本
echo ========================================
echo.

echo [1/3] 清理旧的打包文件...
if exist build rmdir /s /q build
if exist dist rmdir /s /q dist
if exist "实验室样品管理系统.spec" del /q "实验室样品管理系统.spec"

echo [2/3] 开始打包...
pyinstaller --onefile --windowed --name "实验室样品管理系统" --noupx --clean main.py

if %errorlevel% neq 0 (
    echo.
    echo [错误] 打包失败！
    pause
    exit /b 1
)

echo [3/3] 打包完成！
echo.
echo ========================================
echo EXE文件位置: dist\实验室样品管理系统.exe
echo ========================================
echo.
echo 提示: 将EXE文件放到单独的文件夹中运行
echo 首次运行会自动创建 data、attachments、reports 等目录
echo.
pause
