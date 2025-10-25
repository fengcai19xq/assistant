# 文件AI助手桌面应用

基于Electron + React + TypeScript构建的现代化桌面应用，提供智能文件搜索和管理功能。

## 功能特性

- 🚀 **现代化界面**: 基于React 18 + Ant Design 5.x的现代化UI
- 🔍 **智能搜索**: 支持关键词搜索和AI语义搜索
- 📁 **文件夹管理**: 可视化文件夹监控和管理
- 📊 **系统监控**: 实时系统状态和性能监控
- ⚙️ **系统集成**: 系统托盘、开机自启动、文件关联
- 🎨 **主题支持**: 支持浅色/深色主题切换
- 📱 **响应式设计**: 适配不同屏幕尺寸

## 技术栈

- **前端框架**: React 18 + TypeScript 5.x
- **UI组件库**: Ant Design 5.x
- **状态管理**: Zustand
- **构建工具**: Vite
- **桌面框架**: Electron
- **HTTP客户端**: Axios

## 开发环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0
- 后端服务运行在 http://localhost:8080

## 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

### 3. 构建应用

```bash
npm run build
```

### 4. 打包分发

```bash
npm run dist
```

## 项目结构

```
assistant-desktop/
├── src/
│   ├── main/           # Electron主进程
│   ├── preload/        # 预加载脚本
│   └── renderer/       # React渲染进程
│       ├── components/ # 通用组件
│       ├── pages/      # 页面组件
│       ├── stores/     # 状态管理
│       └── main.tsx    # 应用入口
├── resources/          # 应用资源
├── package.json        # 项目配置
├── vite.config.ts      # Vite配置
└── tsconfig.json       # TypeScript配置
```

## 主要功能

### 1. 仪表板
- 系统状态概览
- 基础统计信息
- 实时监控数据

### 2. 智能搜索
- 关键词搜索
- AI语义搜索
- 搜索结果展示
- AI分析总结

### 3. 文件夹管理
- 添加监控文件夹
- 递归监控设置
- 文件夹状态管理
- 重新索引功能

### 4. 系统监控
- 内存使用情况
- 磁盘使用情况
- 线程和GC信息
- 性能统计
- 告警管理

### 5. 设置
- 应用配置
- 连接设置
- 主题设置
- 应用信息

## 开发指南

### 添加新页面

1. 在 `src/renderer/pages/` 下创建新页面组件
2. 在 `src/renderer/App.tsx` 中添加路由
3. 在 `src/renderer/components/Sidebar.tsx` 中添加菜单项

### 添加新API

1. 在 `src/renderer/stores/apiStore.ts` 中添加API方法
2. 在页面组件中使用 `useApiStore` hook调用

### 自定义主题

修改 `src/renderer/App.tsx` 中的 `ConfigProvider` 配置：

```tsx
<ConfigProvider
  theme={{
    token: {
      colorPrimary: '#1890ff',
      borderRadius: 8,
    },
  }}
>
```

## 构建和分发

### 开发构建

```bash
npm run build
```

### 生产构建

```bash
npm run dist
```

构建产物将输出到 `release/` 目录。

## 故障排除

### 常见问题

1. **后端连接失败**
   - 确保后端服务正在运行
   - 检查端口8080是否被占用
   - 验证API地址配置

2. **依赖安装失败**
   - 清除npm缓存: `npm cache clean --force`
   - 删除node_modules重新安装
   - 检查网络连接

3. **构建失败**
   - 检查TypeScript类型错误
   - 验证所有依赖是否正确安装
   - 查看构建日志中的具体错误

### 调试模式

启动调试模式：

```bash
npm run dev
```

应用将自动打开开发者工具。

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 许可证

MIT License
