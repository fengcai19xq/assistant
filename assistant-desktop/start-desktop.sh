#!/bin/bash

# 文件AI助手桌面应用启动脚本

echo "🚀 启动文件AI助手桌面应用..."

# 检查Node.js是否安装
if ! command -v node &> /dev/null; then
    echo "❌ 错误: 未找到Node.js，请先安装Node.js"
    echo "   下载地址: https://nodejs.org/"
    exit 1
fi

# 检查npm是否安装
if ! command -v npm &> /dev/null; then
    echo "❌ 错误: 未找到npm，请先安装npm"
    exit 1
fi

# 进入桌面应用目录
cd assistant-desktop

# 检查package.json是否存在
if [ ! -f "package.json" ]; then
    echo "❌ 错误: 未找到package.json文件"
    exit 1
fi

# 检查node_modules是否存在，如果不存在则安装依赖
if [ ! -d "node_modules" ]; then
    echo "📦 安装依赖包..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ 错误: 依赖安装失败"
        exit 1
    fi
fi

# 检查后端服务是否运行
echo "🔍 检查后端服务状态..."
if curl -s http://localhost:8080/assistant/api/v1/status > /dev/null; then
    echo "✅ 后端服务运行正常"
else
    echo "⚠️  警告: 后端服务未运行，桌面应用可能无法正常工作"
    echo "   请先启动后端服务: ./start-v2-simple.sh"
    echo "   继续启动桌面应用..."
fi

# 启动桌面应用
echo "🎯 启动桌面应用..."
npm run dev

echo "👋 桌面应用已退出"
