# 项目结构说明

## 项目架构

本项目采用前后端分离的架构，包含以下主要组件：

### 后端模块 (Maven多模块项目)

1. **assistant-common** - 公共模块
   - 通用DTO类
   - 异常处理
   - 工具类

2. **assistant-core** - 核心业务模块
   - 文件索引服务
   - 搜索服务
   - 数据库配置

3. **assistant-ai** - AI功能模块
   - 嵌入向量生成
   - 向量搜索
   - AI模型管理

4. **assistant-storage** - 存储模块
   - 向量存储服务
   - 文件索引存储
   - 存储管理器

5. **assistant-web** - Web服务模块
   - REST API接口
   - 监控和健康检查
   - 异常处理

### 前端模块 (Electron + React)

6. **assistant-desktop** - 桌面应用
   - Electron主进程
   - React渲染进程
   - 用户界面组件

## 技术栈

### 后端技术
- Spring Boot 2.7.18
- MyBatis Plus 3.5.4
- SQLite数据库
- RocksDB向量存储
- ONNX Runtime AI推理
- Apache Tika文档解析

### 前端技术
- Electron 28.1.0
- React 18.2.0
- TypeScript 5.3.3
- Ant Design 5.12.8
- Vite 5.0.8

## 启动方式

### 后端服务
```bash
# 构建项目
mvn clean package -DskipTests

# 启动后端服务
java -jar assistant-web/target/assistant-web-1.0.0.jar --spring.profiles.active=v1
```

### 前端服务
```bash
# 进入前端目录
cd assistant-desktop

# 安装依赖
npm install

# 启动开发服务器
npm run dev:react

# 启动Electron应用
npm run electron
```

## 项目特点

1. **模块化设计** - 清晰的模块分离，便于维护和扩展
2. **本地化部署** - 所有组件都在本地运行，无需外部依赖
3. **AI增强搜索** - 支持语义搜索和向量相似度匹配
4. **多格式支持** - 支持文本、PDF、Office文档等多种格式
5. **实时监控** - 提供系统性能监控和告警功能
6. **跨平台支持** - 支持Windows、macOS、Linux系统

## 目录结构

```
assistant/
├── assistant-common/          # 公共模块
├── assistant-core/            # 核心业务模块
├── assistant-ai/             # AI功能模块
├── assistant-storage/         # 存储模块
├── assistant-web/             # Web服务模块
├── assistant-desktop/         # 桌面应用
├── pom.xml                    # Maven根配置
├── README.md                  # 项目说明
├── PROJECT_STRUCTURE.md       # 项目结构说明
└── .gitignore                 # Git忽略文件
```
