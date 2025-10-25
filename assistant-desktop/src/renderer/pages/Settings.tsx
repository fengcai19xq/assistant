import React, { useEffect, useState } from 'react'
import { 
  Card, 
  Form, 
  Input, 
  Button, 
  Typography, 
  Space, 
  Switch, 
  Divider,
  message,
  Alert
} from 'antd'
import { SaveOutlined, ReloadOutlined } from '@ant-design/icons'
import { useAppStore } from '../stores/appStore'

const { Title, Text } = Typography

const Settings: React.FC = () => {
  const { backendUrl, setBackendUrl } = useAppStore()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    form.setFieldsValue({
      backendUrl: backendUrl,
      autoStart: true,
      minimizeToTray: true,
      enableNotifications: true,
      theme: 'light'
    })
  }, [backendUrl, form])

  const handleSave = async (values: any) => {
    setLoading(true)
    
    try {
      // 这里可以添加保存设置的逻辑
      setBackendUrl(values.backendUrl)
      message.success('设置保存成功')
    } catch (error) {
      message.error('保存设置失败')
    } finally {
      setLoading(false)
    }
  }

  const handleTestConnection = async () => {
    const url = form.getFieldValue('backendUrl')
    if (!url) {
      message.warning('请输入后端URL')
      return
    }

    try {
      const response = await fetch(`${url}/api/status`)
      if (response.ok) {
        message.success('连接测试成功')
      } else {
        message.error('连接测试失败')
      }
    } catch (error) {
      message.error('连接测试失败: 无法连接到后端服务')
    }
  }

  return (
    <div className="fade-in">
      <Title level={2}>系统设置</Title>
      
      <Card title="连接设置" className="search-card" style={{ marginBottom: 24 }}>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSave}
        >
          <Form.Item
            name="backendUrl"
            label="后端服务地址"
            rules={[{ required: true, message: '请输入后端服务地址' }]}
            extra="设置后端Spring Boot服务的地址"
          >
            <Input placeholder="http://localhost:8080" />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button 
                type="primary" 
                htmlType="submit" 
                icon={<SaveOutlined />}
                loading={loading}
              >
                保存设置
              </Button>
              <Button 
                icon={<ReloadOutlined />}
                onClick={handleTestConnection}
              >
                测试连接
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card title="应用设置" className="search-card" style={{ marginBottom: 24 }}>
        <Form
          form={form}
          layout="vertical"
        >
          <Form.Item
            name="autoStart"
            label="开机自启动"
            valuePropName="checked"
          >
            <Switch />
          </Form.Item>

          <Form.Item
            name="minimizeToTray"
            label="最小化到系统托盘"
            valuePropName="checked"
          >
            <Switch />
          </Form.Item>

          <Form.Item
            name="enableNotifications"
            label="启用通知"
            valuePropName="checked"
          >
            <Switch />
          </Form.Item>
        </Form>
      </Card>

      <Card title="界面设置" className="search-card" style={{ marginBottom: 24 }}>
        <Form
          form={form}
          layout="vertical"
        >
          <Form.Item
            name="theme"
            label="主题"
          >
            <Input placeholder="light" disabled />
          </Form.Item>
        </Form>
      </Card>

      <Card title="关于" className="search-card">
        <div style={{ padding: '16px 0' }}>
          <p><strong>应用名称:</strong> 文件AI助手</p>
          <p><strong>版本:</strong> 1.0.0</p>
          <p><strong>技术栈:</strong> Electron + React + Spring Boot</p>
          <p><strong>开发时间:</strong> 2024年10月</p>
          <Divider />
          <Alert
            message="提示"
            description="修改设置后需要重启应用才能生效"
            type="info"
            showIcon
          />
        </div>
      </Card>
    </div>
  )
}

export default Settings
