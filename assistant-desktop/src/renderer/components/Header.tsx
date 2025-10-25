import React, { useEffect, useState } from 'react'
import { Typography, Space, Tag, Button } from 'antd'
import { GithubOutlined, ReloadOutlined } from '@ant-design/icons'
import { useAppStore } from '../stores/appStore'

const { Title, Text } = Typography

const Header: React.FC = () => {
  const { appVersion, backendStatus, setBackendStatus } = useAppStore()
  const [currentTime, setCurrentTime] = useState(new Date())

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date())
    }, 1000)

    return () => clearInterval(timer)
  }, [])

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'connected': return 'success'
      case 'disconnected': return 'error'
      case 'checking': return 'processing'
      default: return 'default'
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case 'connected': return '已连接'
      case 'disconnected': return '未连接'
      case 'checking': return '检查中'
      default: return '未知'
    }
  }

  return (
    <div style={{
      padding: '16px 24px',
      background: '#fff',
      borderBottom: '1px solid #f0f0f0',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center'
    }}>
      <div>
        <Title level={4} style={{ margin: 0, color: '#1890ff' }}>
          文件AI助手
        </Title>
        <Text type="secondary" style={{ fontSize: '12px' }}>
          版本 {appVersion} | {currentTime.toLocaleString()}
        </Text>
      </div>

      <Space>
        <Tag color={getStatusColor(backendStatus)}>
          {getStatusText(backendStatus)}
        </Tag>
        
        <Button
          type="text"
          icon={<ReloadOutlined />}
          onClick={() => {
            setBackendStatus('checking')
            // 这里可以添加重新检查后端状态的逻辑
            setTimeout(() => {
              setBackendStatus('connected')
            }, 1000)
          }}
        >
          刷新状态
        </Button>

        <Button
          type="text"
          icon={<GithubOutlined />}
          href="https://github.com"
          target="_blank"
        >
          GitHub
        </Button>
      </Space>
    </div>
  )
}

export default Header
