# 实验室样品登记与检测系统

基于 Python + PySide6 + SQLite 开发的桌面应用，用于实验室样品登记、检测记录管理和报告生成。

## 功能特性

- **样品管理**: 样品登记、编辑、删除、查询
- **检测记录**: 检测项目录入、结果记录、状态跟踪
- **状态流转**: 待检测 → 检测中 → 检测完成 → 报告已生成 → 已归档
- **附件管理**: 支持上传、下载、删除附件，文件丢失检测
- **报告导出**: 支持 Excel 和 PDF 格式检测报告
- **数据备份**: 数据库备份与恢复，支持包含附件
- **操作审计**: 所有修改操作留痕，可追溯
- **统计概览**: 样品数量统计、按状态分布

## 技术栈

- **前端界面**: PySide6 (Qt for Python)
- **数据库**: SQLite3
- **报告生成**: openpyxl (Excel), reportlab (PDF)
- **打包工具**: PyInstaller

## 项目结构

```
laboratory-system/
├── main.py                 # 程序入口
├── requirements.txt        # 依赖列表
├── .gitignore             # Git 忽略配置
├── config/                # 配置模块
│   ├── __init__.py
│   └── settings.py        # 系统配置
├── database/              # 数据库模块
│   ├── __init__.py
│   ├── connection.py      # 数据库连接
│   └── schema.py          # 数据表结构
├── models/                # 数据模型
│   ├── __init__.py
│   ├── sample.py          # 样品模型
│   ├── test_record.py     # 检测记录模型
│   ├── attachment.py      # 附件模型
│   └── audit_log.py       # 审计日志模型
├── services/              # 业务服务
│   ├── __init__.py
│   ├── database_service.py # 数据库服务
│   ├── sample_service.py  # 样品服务
│   ├── test_record_service.py # 检测记录服务
│   ├── attachment_service.py # 附件服务
│   └── audit_service.py   # 审计服务
├── ui/                    # 界面模块
│   ├── __init__.py
│   └── main_window.py     # 主窗口
├── reports/               # 报告模块
│   ├── __init__.py
│   └── report_generator.py # 报告生成器
├── backup/                # 备份模块
│   ├── __init__.py
│   └── backup_manager.py  # 备份管理器
├── tests/                 # 测试模块
│   ├── __init__.py
│   ├── test_database.py   # 数据库测试
│   ├── test_sample_service.py # 样品服务测试
│   └── run_tests.py       # 测试运行脚本
├── data/                  # 数据库文件目录
├── attachments/           # 附件存储目录
├── reports/               # 报告导出目录
├── backups/               # 备份文件目录
└── logs/                  # 日志目录
```

## 安装与运行

### 1. 创建虚拟环境

```bash
python -m venv venv
```

### 2. 激活虚拟环境

Windows:
```bash
venv\Scripts\activate
```

Linux/Mac:
```bash
source venv/bin/activate
```

### 3. 安装依赖

```bash
pip install -r requirements.txt
```

### 4. 运行程序

```bash
python main.py
```

## 运行测试

```bash
python tests/run_tests.py
```

## 打包为 EXE

### 1. 安装 PyInstaller

```bash
pip install pyinstaller
```

### 2. 打包命令

```bash
pyinstaller --onefile --windowed --name "实验室样品检测系统" --icon=icon.ico main.py
```

或者使用打包脚本（见下文）：

```bash
python build.py
```

### 3. 打包产物

打包完成后，可执行文件位于 `dist/` 目录下。

## 业务规则

1. **样品编号唯一性**: 系统会校验样品编号，重复编号禁止录入
2. **报告生成限制**: 所有检测项目完成后才能生成报告
3. **修改留痕**: 所有样品信息修改都会记录到审计日志
4. **删除确认**: 删除样品前会弹出确认对话框
5. **附件检测**: 打开附件管理时自动检测文件是否丢失
6. **状态约束**: 状态流转按照预设流程进行

## 演示数据

首次运行程序时，系统会自动插入 5 条演示样品数据和对应的检测记录，便于测试功能。

## 数据目录说明

- `data/laboratory.db`: SQLite 数据库文件
- `attachments/`: 存储上传的附件文件
- `reports/`: 存储导出的检测报告
- `backups/`: 存储数据库备份
- `logs/`: 存储运行日志

## 注意事项

1. 打包后的 EXE 文件首次运行可能被杀毒软件误报，添加信任即可
2. 建议定期进行数据备份
3. 附件文件请勿手动删除，否则会显示"文件丢失"
4. 数据库文件建议定期备份到外部存储

## 许可证

MIT License
