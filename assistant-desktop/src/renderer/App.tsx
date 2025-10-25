import React, { useEffect, useState } from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { Layout, ConfigProvider, theme } from 'antd'
import { useAppStore } from './stores/appStore'
import Header from './components/Header'
import Dashboard from './pages/Dashboard'
import Search from './pages/Search'
import Folders from './pages/Folders'
import Monitoring from './pages/Monitoring'
import Settings from './pages/Settings'
import './index.css'

const { Content, Sider } = Layout

const App: React.FC = () => {
  const { appVersion, setAppVersion } = useAppStore()
  const [collapsed, setCollapsed] = useState(false)

  useEffect(() => {
    // 获取应用版本
    if (window.electronAPI) {
      window.electronAPI.getAppVersion().then(version => {
        setAppVersion(version)
      })
    }
  }, [setAppVersion])

  const menuItems = [
    {
      key: '/',
      icon: '🏠',
      label: '仪表盘',
      path: '/'
    },
    {
      key: '/search',
      icon: '🔍',
      label: '智能搜索',
      path: '/search'
    },
    {
      key: '/folders',
      icon: '📁',
      label: '文件夹管理',
      path: '/folders'
    },
    {
      key: '/monitoring',
      icon: '📊',
      label: '系统监控',
      path: '/monitoring'
    },
    {
      key: '/settings',
      icon: '⚙️',
      label: '设置',
      path: '/settings'
    }
  ]

  return (
    <ConfigProvider
      theme={{
        algorithm: theme.defaultAlgorithm,
        token: {
          colorPrimary: '#1890ff',
          borderRadius: 6,
        },
      }}
    >
      <Router>
        <Layout style={{ height: '100vh' }}>
          <Sider
            collapsible
            collapsed={collapsed}
            onCollapse={setCollapsed}
            width={200}
            style={{
              background: '#fff',
              borderRight: '1px solid #f0f0f0',
            }}
          >
            <div style={{ 
              padding: '16px', 
              textAlign: 'center',
              borderBottom: '1px solid #f0f0f0',
              marginBottom: '16px'
            }}>
              <h2 style={{ 
                margin: 0, 
                color: '#1890ff',
                fontSize: collapsed ? '16px' : '18px',
                fontWeight: 'bold'
              }}>
                {collapsed ? 'AI' : '文件AI助手'}
              </h2>
            </div>
            
            <div style={{ padding: '0 8px' }}>
              {menuItems.map(item => (
                <div
                  key={item.key}
                  style={{
                    padding: '12px 16px',
                    margin: '4px 0',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    fontSize: collapsed ? '16px' : '14px',
                  }}
                  onClick={() => {
                    window.location.href = item.path
                  }}
                >
                  <span>{item.icon}</span>
                  {!collapsed && <span>{item.label}</span>}
                </div>
              ))}
            </div>
          </Sider>

          <Layout>
            <Header />
            <Content style={{ 
              margin: 0, 
              padding: '24px',
              background: '#f5f5f5',
              overflow: 'auto'
            }}>
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/search" element={<Search />} />
                <Route path="/folders" element={<Folders />} />
                <Route path="/monitoring" element={<Monitoring />} />
                <Route path="/settings" element={<Settings />} />
                <Route path="*" element={<Navigate to="/" replace />} />
              </Routes>
            </Content>
          </Layout>
        </Layout>
      </Router>
    </ConfigProvider>
  )
}

export default App
