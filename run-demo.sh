#!/bin/bash

# 演示版文件AI助手启动脚本

echo "🤖 启动演示版文件AI助手..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java环境"
    exit 1
fi

# 编译Java文件
echo "🔧 编译Java代码..."
javac SimpleFileAssistant.java

if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi

# 运行程序
echo "🚀 启动程序..."
java SimpleFileAssistant

echo "✅ 程序运行完成"
