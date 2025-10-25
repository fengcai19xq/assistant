#!/bin/bash

# API测试脚本
echo "🧪 个人电脑本地AI助手 API测试"
echo "=================================="

BASE_URL="http://localhost:8080/assistant"

# 测试1: 系统状态
echo "📋 测试1: 系统状态"
curl -s "$BASE_URL/api/v1/status" | head -1
echo ""

# 测试2: 健康检查
echo "🏥 测试2: 健康检查"
curl -s "$BASE_URL/actuator/health"
echo ""

# 测试3: 搜索功能
echo "🔍 测试3: 搜索功能"
curl -s -X POST -H "Content-Type: application/json" \
  -d '{"query":"测试","pageSize":3}' \
  "$BASE_URL/api/v1/search" | head -1
echo ""

# 测试4: 添加文件夹
echo "📁 测试4: 添加文件夹"
curl -s -X POST -H "Content-Type: application/json" \
  -d '{"path":"/Users/qianxu/Documents","recursive":true}' \
  "$BASE_URL/api/v1/folders"
echo ""

# 测试5: 获取文件夹列表
echo "📂 测试5: 获取文件夹列表"
curl -s "$BASE_URL/api/v1/folders"
echo ""

# 测试6: 文件统计
echo "📊 测试6: 文件统计"
curl -s "$BASE_URL/api/v1/stats"
echo ""

echo "✅ API测试完成"
