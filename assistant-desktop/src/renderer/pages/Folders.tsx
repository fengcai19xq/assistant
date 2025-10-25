import React, { useEffect, useState } from 'react'
import { 
  Card, 
  List, 
  Button, 
  Typography, 
  Space, 
  Tag, 
  Popconfirm, 
  message,
  Modal,
  Form,
  Input,
  Switch,
  Spin,
  Alert
} from 'antd'
import { 
  FolderOutlined, 
  PlusOutlined, 
  DeleteOutlined, 
  ReloadOutlined,
  FolderOpenOutlined 
} from '@ant-design/icons'
import { useApiStore } from '../stores/apiStore'

const { Title, Text } = Typography

const Folders: React.FC = () => {
  const { getWatchFolders, addWatchFolder, removeWatchFolder } = useApiStore()
  const [folders, setFolders] = useState<any[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [modalVisible, setModalVisible] = useState(false)
  const [form] = Form.useForm()

  const loadFolders = async () => {
    setLoading(true)
    setError(null)

    try {
      const response = await getWatchFolders()
      if (response.success) {
        setFolders(response.data?.folders || [])
      } else {
        setError(response.message || '获取文件夹列表失败')
      }
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadFolders()
  }, [])

  const handleAddFolder = async (values: any) => {
    try {
      const response = await addWatchFolder(values.path, values.recursive)
      if (response.success) {
        message.success('文件夹添加成功')
        setModalVisible(false)
        form.resetFields()
        loadFolders()
      } else {
        message.error(response.message || '添加失败')
      }
    } catch (err: any) {
      message.error('添加失败: ' + err.message)
    }
  }

  const handleRemoveFolder = async (id: number) => {
    try {
      const response = await removeWatchFolder(id)
      if (response.success) {
        message.success('文件夹删除成功')
        loadFolders()
      } else {
        message.error(response.message || '删除失败')
      }
    } catch (err: any) {
      message.error('删除失败: ' + err.message)
    }
  }

  const handleSelectFolder = () => {
    if (window.electronAPI) {
      window.electronAPI.showOpenDialog({
        properties: ['openDirectory'],
        title: '选择要监控的文件夹'
      }).then((result: any) => {
        if (!result.canceled && result.filePaths.length > 0) {
          form.setFieldsValue({ path: result.filePaths[0] })
        }
      })
    }
  }

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
            <Button size="small" onClick={loadFolders}>
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
        <Title level={2}>文件夹管理</Title>
        <Space>
          <Button
            icon={<ReloadOutlined />}
            onClick={loadFolders}
            loading={loading}
          >
            刷新
          </Button>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setModalVisible(true)}
          >
            添加文件夹
          </Button>
        </Space>
      </div>

      <Card className="search-card">
        {folders.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '50px' }}>
            <FolderOutlined style={{ fontSize: 48, color: '#d9d9d9', marginBottom: 16 }} />
            <div style={{ color: '#999' }}>暂无监控文件夹</div>
            <Button 
              type="primary" 
              icon={<PlusOutlined />} 
              onClick={() => setModalVisible(true)}
              style={{ marginTop: 16 }}
            >
              添加第一个文件夹
            </Button>
          </div>
        ) : (
          <List
            dataSource={folders}
            renderItem={(folder: any) => (
              <List.Item
                actions={[
                  <Popconfirm
                    title="确定要删除这个监控文件夹吗？"
                    onConfirm={() => handleRemoveFolder(folder.id)}
                    okText="确定"
                    cancelText="取消"
                  >
                    <Button 
                      type="text" 
                      danger 
                      icon={<DeleteOutlined />}
                    >
                      删除
                    </Button>
                  </Popconfirm>
                ]}
              >
                <List.Item.Meta
                  avatar={<FolderOpenOutlined style={{ fontSize: 24, color: '#1890ff' }} />}
                  title={
                    <div>
                      <Text strong>{folder.path}</Text>
                      <Tag 
                        color={folder.enabled ? 'green' : 'red'} 
                        style={{ marginLeft: 8 }}
                      >
                        {folder.enabled ? '已启用' : '已禁用'}
                      </Tag>
                    </div>
                  }
                  description={
                    <div>
                      <Text type="secondary">
                        递归监控: {folder.recursive ? '是' : '否'}
                      </Text>
                      <br />
                      <Text type="secondary">
                        添加时间: {new Date(folder.createdTime).toLocaleString()}
                      </Text>
                    </div>
                  }
                />
              </List.Item>
            )}
          />
        )}
      </Card>

      <Modal
        title="添加监控文件夹"
        open={modalVisible}
        onCancel={() => {
          setModalVisible(false)
          form.resetFields()
        }}
        footer={null}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleAddFolder}
        >
          <Form.Item
            name="path"
            label="文件夹路径"
            rules={[{ required: true, message: '请输入文件夹路径' }]}
          >
            <Input
              placeholder="选择或输入文件夹路径"
              addonAfter={
                <Button icon={<FolderOutlined />} onClick={handleSelectFolder}>
                  选择
                </Button>
              }
            />
          </Form.Item>

          <Form.Item
            name="recursive"
            label="递归监控"
            valuePropName="checked"
            initialValue={true}
          >
            <Switch />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                添加
              </Button>
              <Button onClick={() => setModalVisible(false)}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default Folders
