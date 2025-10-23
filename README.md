# 🤖 文件AI助手

个人电脑本地AI助手技术方案 - MVP版本

## 📋 项目简介

这是一个基于Spring Boot + SQLite的本地文件智能搜索系统，支持多种文件格式的索引和语义搜索。

## ✨ 主要功能

- 📁 **智能文件索引**: 自动扫描和建立个人文件的知识图谱
- 🔍 **自然语言搜索**: 支持模糊查询、语义理解、多轮对话
- 📄 **跨格式支持**: 文本、PDF、Word、Excel、PPT、图片OCR、代码文件等
- 🔒 **隐私保护**: 所有数据处理均在本地完成
- 🚀 **轻量级AI**: 使用all-MiniLM-L6-v2模型，仅需80MB

## 🏗️ 技术架构

### 后端技术栈
- **核心框架**: Spring Boot 3.2.x
- **数据持久层**: MyBatis Plus 3.5.x + SQLite
- **AI推理引擎**: ONNX Runtime Java API
- **文档解析**: Apache Tika + Apache POI + PDFBox
- **构建工具**: Maven

### 前端技术栈
- **界面**: 原生HTML + CSS + JavaScript
- **样式**: 现代化响应式设计
- **交互**: RESTful API调用

## 📦 项目结构

```
file-ai-assistant/
├── assistant-common/          # 公共模块
│   ├── dto/                   # 数据传输对象
│   ├── util/                  # 工具类
│   ├── exception/             # 异常处理
│   └── constants/             # 常量定义
├── assistant-core/            # 核心业务模块
│   ├── entity/                # 数据库实体
│   ├── mapper/                # MyBatis映射器
│   └── service/                # 业务服务层
├── assistant-web/             # Web接口模块
│   ├── controller/            # REST控制器
│   └── AssistantWebApplication.java
├── frontend/                   # 前端界面
│   └── index.html
├── pom.xml                    # 父POM配置
├── start.sh                   # 启动脚本
└── README.md                  # 项目文档
```

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- 至少2GB可用内存
- 至少1GB可用磁盘空间

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd file-ai-assistant
   ```

2. **启动应用**
   ```bash
   ./start.sh
   ```

3. **访问界面**
   - 后端API: http://localhost:8080/assistant
   - 前端界面: 打开 `frontend/index.html`

### 手动启动

如果自动启动脚本失败，可以手动执行：

```bash
# 编译项目
mvn clean compile

# 打包项目
mvn package -DskipTests

# 启动Web服务
cd assistant-web
java -jar target/assistant-web-1.0.0.jar
```

## 📖 使用指南

### 1. 添加监控文件夹

1. 打开前端界面
2. 在"文件夹管理"区域输入要监控的文件夹路径
3. 选择是否递归监控子文件夹
4. 点击"添加文件夹"按钮

### 2. 搜索文件

1. 在搜索框中输入关键词
2. 选择是否启用语义搜索
3. 点击"搜索"按钮或按回车键

### 3. 管理索引

- **重新索引**: 点击"重新索引"按钮重新扫描所有文件夹
- **查看状态**: 在系统状态区域查看索引统计信息

## 🔧 API接口

### 文件夹管理

- `POST /api/v1/folders` - 添加监控文件夹
- `GET /api/v1/folders` - 获取所有监控文件夹
- `DELETE /api/v1/folders/{id}` - 删除监控文件夹
- `POST /api/v1/folders/reindex` - 重新索引所有文件夹

### 搜索功能

- `POST /api/v1/search` - 搜索文件
- `GET /api/v1/search/history` - 获取搜索历史
- `DELETE /api/v1/search/history` - 清空搜索历史

### 系统状态

- `GET /api/v1/status` - 获取系统状态
- `GET /api/v1/files` - 获取所有文件列表

## ⚙️ 配置说明

### 应用配置

配置文件位置: `assistant-web/src/main/resources/application.yml`

主要配置项：
- `assistant.storage.data-dir`: 数据存储目录
- `assistant.storage.index-dir`: 索引存储目录
- `assistant.ai.embedding-model`: AI模型路径
- `assistant.index.max-file-size`: 最大文件大小限制

### 数据库配置

- 数据库类型: SQLite
- 数据库文件: `~/.file-assistant/data/assistant.db`
- 自动创建表结构

## 🐛 故障排除

### 常见问题

1. **Java版本问题**
   ```
   错误: 需要Java 17+
   解决: 安装Java 17或更高版本
   ```

2. **端口占用**
   ```
   错误: Port 8080 already in use
   解决: 修改application.yml中的server.port配置
   ```

3. **模型下载失败**
   ```
   错误: 模型下载失败
   解决: 检查网络连接，或手动下载模型文件
   ```

4. **权限问题**
   ```
   错误: Permission denied
   解决: 确保对目标文件夹有读取权限
   ```

### 日志查看

日志文件位置: `~/.file-assistant/logs/assistant.log`

## 🔮 未来规划

### 短期目标
- [ ] 添加文件类型过滤
- [ ] 支持更多文件格式
- [ ] 优化搜索性能
- [ ] 添加搜索建议

### 长期目标
- [ ] 集成更多AI模型
- [ ] 支持多用户
- [ ] 添加文件预览功能
- [ ] 支持云端同步

## 📄 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 🤝 贡献指南

欢迎提交Issue和Pull Request来帮助改进项目。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 提交Issue
- 发送邮件
- 在线讨论

---

**注意**: 这是MVP版本，功能相对简单。完整版本将包含更多高级功能。
