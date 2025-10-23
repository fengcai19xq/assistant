#!/bin/bash

# 简化版文件AI助手启动脚本

echo "🤖 启动简化版文件AI助手..."

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java环境"
    exit 1
fi

# 检查SQLite JDBC驱动
if [ ! -f "sqlite-jdbc.jar" ]; then
    echo "📦 下载SQLite JDBC驱动..."
    curl -L -o sqlite-jdbc.jar "https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.44.1.0/sqlite-jdbc-3.44.1.0.jar"
fi

# 编译Java文件
echo "🔧 编译Java代码..."
javac -cp sqlite-jdbc.jar SimpleAssistant.java

if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi

# 运行程序
echo "🚀 启动程序..."
java -cp .:sqlite-jdbc.jar SimpleAssistant

echo "✅ 程序运行完成"
