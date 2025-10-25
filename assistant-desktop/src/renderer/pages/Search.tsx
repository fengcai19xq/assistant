import React, { useState } from 'react'
import { 
  Input, 
  Button, 
  Card, 
  List, 
  Typography, 
  Space, 
  Switch, 
  Tag, 
  Spin,
  Alert,
  Empty
} from 'antd'
import { 
  SearchOutlined, 
  FileOutlined, 
  ClockCircleOutlined,
  RobotOutlined 
} from '@ant-design/icons'
import { useApiStore } from '../stores/apiStore'

const { Title, Text } = Typography
const { Search: SearchInput } = Input

const Search: React.FC = () => {
  const { searchFiles } = useApiStore()
  const [query, setQuery] = useState('')
  const [semantic, setSemantic] = useState(false)
  const [loading, setLoading] = useState(false)
  const [results, setResults] = useState<any[]>([])
  const [error, setError] = useState<string | null>(null)

  const handleSearch = async () => {
    if (!query.trim()) return

    setLoading(true)
    setError(null)

    try {
      const response = await searchFiles(query, semantic)
      if (response.success) {
        setResults(response.data || [])
      } else {
        setError(response.message || '搜索失败')
      }
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
    return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB'
  }

  const getFileTypeColor = (type: string) => {
    switch (type) {
      case 'code': return 'blue'
      case 'web': return 'green'
      case 'text': return 'orange'
      case 'document': return 'purple'
      default: return 'default'
    }
  }

  return (
    <div className="fade-in">
      <Title level={2}>智能搜索</Title>
      
      <Card className="search-card" style={{ marginBottom: 24 }}>
        <Space.Compact style={{ width: '100%' }}>
          <SearchInput
            placeholder="输入搜索关键词..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onSearch={handleSearch}
            enterButton={
              <Button type="primary" icon={<SearchOutlined />} loading={loading}>
                搜索
              </Button>
            }
            size="large"
          />
        </Space.Compact>
        
        <div style={{ marginTop: 16, display: 'flex', alignItems: 'center', gap: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <RobotOutlined style={{ color: semantic ? '#1890ff' : '#d9d9d9' }} />
            <Text>语义搜索</Text>
            <Switch
              checked={semantic}
              onChange={setSemantic}
              size="small"
            />
          </div>
          
          {semantic && (
            <Tag color="blue" icon={<RobotOutlined />}>
              AI增强搜索已启用
            </Tag>
          )}
        </div>
      </Card>

      {error && (
        <Alert
          message="搜索失败"
          description={error}
          type="error"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      {loading && (
        <div style={{ textAlign: 'center', padding: '50px' }}>
          <Spin size="large" />
          <div style={{ marginTop: 16 }}>搜索中...</div>
        </div>
      )}

      {!loading && results.length === 0 && query && (
        <Empty
          description="未找到匹配的文件"
          image={Empty.PRESENTED_IMAGE_SIMPLE}
        />
      )}

      {!loading && results.length > 0 && (
        <Card title={`搜索结果 (${results.length} 个文件)`} className="search-card">
          <List
            dataSource={results}
            renderItem={(item: any) => (
              <List.Item>
                <List.Item.Meta
                  avatar={<FileOutlined style={{ fontSize: 24, color: '#1890ff' }} />}
                  title={
                    <div>
                      <Text strong>{item.fileName}</Text>
                      <Tag 
                        color={getFileTypeColor(item.fileType)} 
                        style={{ marginLeft: 8 }}
                      >
                        {item.fileType}
                      </Tag>
                    </div>
                  }
                  description={
                    <div>
                      <Text type="secondary" style={{ display: 'block', marginBottom: 4 }}>
                        {item.filePath}
                      </Text>
                      <Space size="small">
                        <Text type="secondary">
                          <ClockCircleOutlined /> {new Date(item.lastModified).toLocaleString()}
                        </Text>
                        <Text type="secondary">
                          大小: {formatFileSize(item.fileSize)}
                        </Text>
                        {item.relevanceScore && (
                          <Text type="secondary">
                            相关度: {(item.relevanceScore * 100).toFixed(1)}%
                          </Text>
                        )}
                      </Space>
                      {item.content && (
                        <div style={{ marginTop: 8, padding: 8, background: '#f5f5f5', borderRadius: 4 }}>
                          <Text type="secondary" style={{ fontSize: 12 }}>
                            {item.content.substring(0, 200)}...
                          </Text>
                        </div>
                      )}
                    </div>
                  }
                />
              </List.Item>
            )}
          />
        </Card>
      )}
    </div>
  )
}

export default Search
