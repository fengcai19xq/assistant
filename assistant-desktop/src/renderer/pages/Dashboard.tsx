import React, { useEffect, useState } from 'react'
import { Card, Row, Col, Statistic, Typography, Spin, Alert, Button } from 'antd'
import { 
  FolderOutlined, 
  FileOutlined, 
  SearchOutlined, 
  ClockCircleOutlined,
  ReloadOutlined 
} from '@ant-design/icons'
import { useApiStore } from '../stores/apiStore'

const { Title } = Typography

const Dashboard: React.FC = () => {
  const { getSystemStatus, getWatchFolders } = useApiStore()
  const [loading, setLoading] = useState(true)
  const [systemData, setSystemData] = useState<any>(null)
  const [foldersData, setFoldersData] = useState<any>(null)
  const [error, setError] = useState<string | null>(null)

  const loadData = async () => {
    setLoading(true)
    setError(null)

    try {
      const [statusResponse, foldersResponse] = await Promise.all([
        getSystemStatus(),
        getWatchFolders()
      ])

      if (statusResponse.success) {
        setSystemData(statusResponse.data)
      } else {
        setError(statusResponse.message || '获取系统状态失败')
      }

      if (foldersResponse.success) {
        setFoldersData(foldersResponse.data)
      }
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Spin size="large" />
        <div style={{ marginTop: 16 }}>加载中...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Alert
          message="加载失败"
          description={error}
          type="error"
          showIcon
          action={
            <Button size="small" onClick={loadData}>
              重试
            </Button>
          }
        />
      </div>
    )
  }

  return (
    <div className="fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={2}>系统概览</Title>
        <Button
          icon={<ReloadOutlined />}
          onClick={loadData}
          loading={loading}
        >
          刷新数据
        </Button>
      </div>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card className="search-card">
            <Statistic
              title="监控文件夹"
              value={foldersData?.folders?.length || 0}
              suffix="个"
              prefix={<FolderOutlined style={{ color: '#1890ff' }} />}
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="search-card">
            <Statistic
              title="已索引文件"
              value={systemData?.indexedFileCount || 0}
              suffix="个"
              prefix={<FileOutlined style={{ color: '#52c41a' }} />}
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="search-card">
            <Statistic
              title="总文件数"
              value={systemData?.totalFileCount || 0}
              suffix="个"
              prefix={<FileOutlined style={{ color: '#fa8c16' }} />}
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="search-card">
            <Statistic
              title="最后更新"
              value={systemData?.lastUpdateTime ? new Date(systemData.lastUpdateTime).toLocaleString() : '未知'}
              prefix={<ClockCircleOutlined style={{ color: '#722ed1' }} />}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
        <Col xs={24} lg={12}>
          <Card title="系统状态" className="search-card">
            <div style={{ padding: '16px 0' }}>
              <p><strong>后端服务:</strong> {systemData?.backendStatus || '未知'}</p>
              <p><strong>数据库:</strong> {systemData?.databaseStatus || '未知'}</p>
              <p><strong>AI模型:</strong> {systemData?.aiModelStatus || '未知'}</p>
              <p><strong>存储状态:</strong> {systemData?.storageStatus || '未知'}</p>
            </div>
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card title="快速操作" className="search-card">
            <div style={{ padding: '16px 0' }}>
              <Button type="primary" icon={<SearchOutlined />} style={{ marginRight: 8, marginBottom: 8 }}>
                智能搜索
              </Button>
              <Button icon={<FolderOutlined />} style={{ marginRight: 8, marginBottom: 8 }}>
                管理文件夹
              </Button>
              <Button icon={<ReloadOutlined />} style={{ marginBottom: 8 }}>
                重新索引
              </Button>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Dashboard
