# 文物藏品管理系统

基于 Python、PySide6 和 SQLite 开发的专业文物藏品管理桌面应用。

## 功能特性

### 核心功能
- **藏品管理**: 支持藏品编号、名称、年代、类别、来源、保存状态、入库时间、存放位置、估值、描述等信息管理
- **条件筛选**: 按编号、名称、类别、年代、来源、保存状态等多维度筛选藏品
- **详情编辑**: 完整的藏品信息编辑界面，支持新建、修改、删除操作
- **修复记录**: 完整的修复流程管理，支持修复状态流转
- **图片附件**: 支持图片和文档附件上传、预览和管理
- **展出历史**: 记录藏品的展出历史信息
- **统计概览**: 多维度统计展示，包括按类别、年代、状态、来源等统计
- **报告导出**: 支持导出 CSV、JSON 格式数据，生成统计报告和详情报告
- **数据备份**: 支持数据库备份和完整备份（含附件），支持备份恢复

### 业务规则
- **藏品编号重复检测**: 自动检测并阻止重复编号录入
- **修复状态流转**: 规范的修复状态流转机制（待修复 → 修复中 → 已完成/已取消）
- **附件丢失提示**: 启动时自动检测并提示丢失的附件文件
- **修改留痕**: 所有数据变更记录审计日志
- **删除确认**: 重要操作提供二次确认机制
- **数据库初始化异常处理**: 完善的错误处理和用户提示

## 项目结构

```
.
├── ui/                      # UI界面模块
│   ├── __init__.py
│   ├── main_window.py      # 主窗口
│   ├── collection_list.py  # 藏品列表组件
│   ├── collection_detail.py # 藏品详情组件
│   ├── statistics_panel.py # 统计面板
│   ├── backup_panel.py     # 备份管理面板
│   ├── exhibition_dialog.py # 展出记录对话框
│   └── dialogs.py         # 通用对话框工具
├── database/               # 数据库模块
│   ├── __init__.py
│   ├── connection.py      # 数据库连接管理
│   ├── collection_repo.py # 藏品仓库
│   ├── repair_repo.py     # 修复记录仓库
│   ├── attachment_repo.py # 附件仓库
│   └── exhibition_repo.py # 展出记录仓库
├── models/                 # 数据模型
│   ├── __init__.py
│   ├── collection.py      # 藏品模型
│   ├── repair_record.py   # 修复记录模型
│   ├── attachment.py      # 附件模型
│   ├── exhibition.py      # 展出历史模型
│   └── audit_log.py       # 审计日志模型
├── services/               # 业务服务层
│   ├── __init__.py
│   ├── collection_service.py # 藏品服务
│   ├── repair_service.py  # 修复服务
│   └── exhibition_service.py # 展出服务
├── attachments/            # 附件管理模块
│   ├── __init__.py
│   └── manager.py         # 附件管理器
├── reports/                # 报告导出模块
│   ├── __init__.py
│   └── generator.py       # 报告生成器
├── backup/                 # 数据备份模块
│   ├── __init__.py
│   └── manager.py         # 备份管理器
├── config/                 # 配置模块
│   ├── __init__.py
│   └── settings.py        # 应用配置
├── tests/                  # 测试模块
│   ├── __init__.py
│   ├── test_models.py     # 模型测试
│   └── test_database.py   # 数据库测试
├── data/                   # 数据目录
│   ├── attachments/       # 附件存储
│   ├── backups/           # 备份文件
│   └── reports/           # 导出报告
├── logs/                   # 日志目录
├── demo_data.py           # 演示数据生成器
├── main.py                # 主程序入口
├── requirements.txt       # 依赖列表
└── README.md             # 项目说明
```

## 安装与运行

### 环境要求
- Python 3.8+
- PySide6 6.5+

### 安装依赖

```bash
pip install -r requirements.txt
```

### 运行应用

```bash
python main.py
```

首次运行时，系统会提示是否加载演示数据，选择"是"将自动生成20条示例藏品记录及相关数据。

## 打包方式

### 使用 PyInstaller 打包

#### 1. 安装 PyInstaller

```bash
pip install pyinstaller
```

#### 2. 单文件打包

```bash
pyinstaller --onefile --windowed --name "文物藏品管理系统" main.py
```

#### 3. 带目录打包（推荐，启动更快）

```bash
pyinstaller --windowed --name "文物藏品管理系统" main.py
```

#### 4. 带图标打包

```bash
pyinstaller --onefile --windowed --icon=app.ico --name "文物藏品管理系统" main.py
```

#### 打包参数说明
- `--onefile`: 打包成单个可执行文件
- `--windowed`: 不显示控制台窗口（GUI应用）
- `--name`: 指定生成的可执行文件名
- `--icon`: 指定应用图标

### 使用 spec 文件自定义打包

创建 `collections.spec` 文件：

```python
# -*- mode: python ; coding: utf-8 -*-

block_cipher = None

a = Analysis(
    ['main.py'],
    pathex=[],
    binaries=[],
    datas=[],
    hiddenimports=[],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=[],
    win_no_prefer_redirects=False,
    win_private_assemblies=False,
    cipher=block_cipher,
    noarchive=False,
)

pyz = PYZ(a.pure, a.zipped_data, cipher=block_cipher)

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.zipfiles,
    a.datas,
    [],
    name='文物藏品管理系统',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=False,
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
    icon='app.ico' if os.path.exists('app.ico') else None,
)
```

然后运行：

```bash
pyinstaller collections.spec
```

## 运行测试

```bash
# 运行所有测试
python -m pytest tests/ -v

# 运行模型测试
python -m pytest tests/test_models.py -v

# 运行数据库测试
python -m pytest tests/test_database.py -v
```

## 配置说明

主要配置项位于 `config/settings.py`：

- `DATABASE_PATH`: 数据库文件路径
- `ATTACHMENTS_DIR`: 附件存储目录
- `BACKUPS_DIR`: 备份文件目录
- `REPORTS_DIR`: 报告导出目录
- `MAX_ATTACHMENT_SIZE`: 最大附件大小（默认50MB）
- `CATEGORIES`: 藏品类别列表
- `ERAS`: 年代列表
- `SOURCES`: 来源列表
- `CONSERVATION_STATUSES`: 保存状态列表
- `REPAIR_STATUSES`: 修复状态列表

## 快捷键

- `Ctrl + N`: 新建藏品
- `Ctrl + Q`: 退出应用

## 注意事项

1. 首次运行请确保有足够的磁盘空间
2. 建议定期进行数据备份
3. 删除操作不可恢复，请谨慎操作
4. 附件文件请勿手动删除，应通过应用程序管理
5. 大型附件建议压缩后上传

## 技术栈

- **前端框架**: PySide6 (Qt6)
- **数据库**: SQLite3
- **开发语言**: Python 3
- **架构模式**: 分层架构 (UI → Service → Repository → Database)

## 许可证

本项目仅供学习和研究使用。
