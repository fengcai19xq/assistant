#!/bin/bash

# 文件AI助手启动脚本

echo "🚀 启动文件AI助手..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java环境，请先安装Java 17+"
    exit 1
fi

# 检查Maven环境
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未找到Maven环境，请先安装Maven"
    exit 1
fi

# 进入项目目录
cd "$(dirname "$0")"

echo "📦 编译项目..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi

echo "🔧 打包项目..."
mvn package -DskipTests -q

if [ $? -ne 0 ]; then
    echo "❌ 打包失败"
    exit 1
fi

echo "🌐 启动Web服务..."
cd assistant-web
nohup java -jar target/assistant-web-1.0.0.jar &

echo "✅ 文件AI助手已启动"
echo "🌍 访问地址: http://localhost:8080/assistant"
echo "📱 前端界面: 打开 frontend/index.html"
