import os
import sys
import shutil
from pathlib import Path


def build_exe():
    print("=" * 50)
    print("实验室样品检测系统 - 打包脚本")
    print("=" * 50)

    try:
        import PyInstaller
    except ImportError:
        print("正在安装 PyInstaller...")
        os.system("pip install pyinstaller")

    cmd = [
        "pyinstaller",
        "--onefile",
        "--windowed",
        "--name", "实验室样品检测系统",
        "--clean",
        "--noconfirm",
        "main.py"
    ]

    print("\n执行打包命令...")
    print(" ".join(cmd))
    print()

    result = os.system(" ".join(cmd))

    if result == 0:
        print("\n" + "=" * 50)
        print("打包成功！")
        print(f"可执行文件位置: {Path.cwd() / 'dist' / '实验室样品检测系统.exe'}")
        print("=" * 50)
    else:
        print("\n打包失败，请检查错误信息。")
        sys.exit(1)


if __name__ == "__main__":
    build_exe()
