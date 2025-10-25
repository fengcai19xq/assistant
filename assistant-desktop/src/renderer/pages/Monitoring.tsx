import React, { useEffect, useState } from 'react'
import {
  Card,
  Row,
  Col,
  Statistic,
  Typography,
  Spin,
  Alert,
  Button,
  List,
  Progress,
  Tag,
  Space,
  Divider
} from 'antd'
import {
  ReloadOutlined,
  MonitorOutlined,
  DatabaseOutlined,
  AlertOutlined,
  CheckCircleOutlined,
  ExclamationCircleOutlined,
  HddOutlined
} from '@ant-design/icons'
import { useApiStore } from '../stores/apiStore'

const { Title, Text } = Typography

const Monitoring: React.FC = () => {
  const { getMonitoringDashboard } = useApiStore()
  const [loading, setLoading] = useState(true)
  const [monitoringData, setMonitoringData] = useState<any>(null)
  const [error, setError] = useState<string | null>(null)

  const loadMonitoringData = async () => {
    setLoading(true)
    setError(null)

    try {
      const response = await getMonitoringDashboard()
      if (response.success) {
        setMonitoringData(response.data)
      } else {
        setError(response.message || '获取监控数据失败')
      }
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadMonitoringData()

    // 每30秒自动刷新一次
    const interval = setInterval(loadMonitoringData, 30000)
    return () => clearInterval(interval)
  }, [])

  const formatBytes = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
    return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB'
  }

  const getAlertType = (level: string) => {
    switch (level) {
      case 'CRITICAL': return 'error'
      case 'ERROR': return 'error'
      case 'WARNING': return 'warning'
      default: return 'info'
    }
  }

  const getAlertIcon = (level: string) => {
    switch (level) {
      case 'CRITICAL':
      case 'ERROR':
        return <ExclamationCircleOutlined />
      case 'WARNING':
        return <AlertOutlined />
      default:
        return <CheckCircleOutlined />
    }
  }

  if (loading && !monitoringData) {
    return (
      <div style={{ textAlign: 'center', padding: '50px' }}>
        <Spin size="large" />
        <div style={{ marginTop: 16 }}>加载监控数据中...</div>
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
            <Button size="small" onClick={loadMonitoringData}>
              重试
            </Button>
          }
        />
      </div>
    )
  }

  const systemMetrics = monitoringData?.systemMetrics || {}
  const performanceStats = monitoringData?.performanceStats || {}
  const activeAlerts = monitoringData?.activeAlerts || []
  const alertStats = monitoringData?.alertStats || {}

  return (
    <div className="fade-in">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={2}>系统监控</Title>
        <Button
          icon={<ReloadOutlined />}
          onClick={loadMonitoringData}
          loading={loading}
        >
          刷新数据
        </Button>
      </div>

      {/* 系统基础指标 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card className="search-card">
            <Statistic
              title="内存使用率"
              value={systemMetrics.heapUsagePercent || 0}
              suffix="%"
              precision={2}
              prefix={<HddOutlined style={{ color: '#1890ff' }} />}
            />
            <Progress
              percent={systemMetrics.heapUsagePercent || 0}
              size="small"
              status={systemMetrics.heapUsagePercent > 80 ? 'exception' : 'normal'}
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="search-card">
            <Statistic
              title="线程数量"
              value={systemMetrics.threadCount || 0}
              suffix="个"
              prefix={<MonitorOutlined style={{ color: '#52c41a' }} />}
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="search-card">
            <Statistic
              title="磁盘使用率"
              value={systemMetrics.diskUsagePercent || 0}
              suffix="%"
              precision={2}
              prefix={<DatabaseOutlined style={{ color: '#fa8c16' }} />}
            />
            <Progress
              percent={systemMetrics.diskUsagePercent || 0}
              size="small"
              status={systemMetrics.diskUsagePercent > 90 ? 'exception' : 'normal'}
            />
          </Card>
        </Col>

        <Col xs={24} sm={12} lg={6}>
          <Card className="search-card">
            <Statistic
              title="系统负载"
              value={systemMetrics.systemLoadAverage || 0}
              precision={2}
              prefix={<MonitorOutlined style={{ color: '#722ed1' }} />}
            />
          </Card>
        </Col>
      </Row>

      {/* 性能统计 */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} lg={12}>
          <Card title="性能统计" className="search-card">
            <Row gutter={16}>
              <Col span={12}>
                <Statistic
                  title="平均搜索时间"
                  value={performanceStats.avgSearchTimeMs || 0}
                  suffix="ms"
                  precision={2}
                />
              </Col>
              <Col span={12}>
                <Statistic
                  title="总搜索次数"
                  value={performanceStats.totalSearches || 0}
                  suffix="次"
                />
              </Col>
            </Row>
            <Divider />
            <Row gutter={16}>
              <Col span={12}>
                <Statistic
                  title="平均索引时间"
                  value={performanceStats.avgIndexTimeMs || 0}
                  suffix="ms"
                  precision={2}
                />
              </Col>
              <Col span={12}>
                <Statistic
                  title="总错误次数"
                  value={performanceStats.totalErrors || 0}
                  suffix="次"
                />
              </Col>
            </Row>
          </Card>
        </Col>

        <Col xs={24} lg={12}>
          <Card title="内存详情" className="search-card">
            <Row gutter={16}>
              <Col span={12}>
                <Statistic
                  title="堆内存已用"
                  value={formatBytes(systemMetrics.heapUsed || 0)}
                />
              </Col>
              <Col span={12}>
                <Statistic
                  title="堆内存最大"
                  value={formatBytes(systemMetrics.heapMax || 0)}
                />
              </Col>
            </Row>
            <Divider />
            <Row gutter={16}>
              <Col span={12}>
                <Statistic
                  title="非堆内存已用"
                  value={formatBytes(systemMetrics.nonHeapUsed || 0)}
                />
              </Col>
              <Col span={12}>
                <Statistic
                  title="非堆内存最大"
                  value={formatBytes(systemMetrics.nonHeapMax || 0)}
                />
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>

      {/* 活跃告警 */}
      {activeAlerts.length > 0 && (
        <Card title="系统告警" className="search-card" style={{ marginBottom: 24 }}>
          <List
            dataSource={activeAlerts}
            renderItem={(alert: any) => (
              <List.Item>
                <Alert
                  message={alert.title}
                  description={alert.message}
                  type={getAlertType(alert.level)}
                  icon={getAlertIcon(alert.level)}
                  showIcon
                  style={{ width: '100%' }}
                  action={
                    <Space>
                      <Tag color={alert.level === 'CRITICAL' ? 'red' :
                                   alert.level === 'ERROR' ? 'red' :
                                   alert.level === 'WARNING' ? 'orange' : 'blue'}>
                        {alert.level}
                      </Tag>
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        {new Date(alert.timestamp).toLocaleString()}
                      </Text>
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        </Card>
      )}

      {/* 告警统计 */}
      {Object.keys(alertStats).length > 0 && (
        <Card title="告警统计" className="search-card">
          <Row gutter={16}>
            <Col span={6}>
              <Statistic
                title="总告警数"
                value={alertStats.totalAlerts || 0}
                suffix="个"
              />
            </Col>
            <Col span={6}>
              <Statistic
                title="严重告警"
                value={alertStats.criticalAlerts || 0}
                suffix="个"
                valueStyle={{ color: '#ff4d4f' }}
              />
            </Col>
            <Col span={6}>
              <Statistic
                title="错误告警"
                value={alertStats.errorAlerts || 0}
                suffix="个"
                valueStyle={{ color: '#ff4d4f' }}
              />
            </Col>
            <Col span={6}>
              <Statistic
                title="警告告警"
                value={alertStats.warningAlerts || 0}
                suffix="个"
                valueStyle={{ color: '#fa8c16' }}
              />
            </Col>
          </Row>
        </Card>
      )}

      {/* 无告警时的提示 */}
      {activeAlerts.length === 0 && (
        <Card className="search-card">
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <CheckCircleOutlined style={{ fontSize: 48, color: '#52c41a', marginBottom: 16 }} />
            <Title level={4} style={{ color: '#52c41a' }}>系统运行正常</Title>
            <Text type="secondary">当前没有活跃的系统告警</Text>
          </div>
        </Card>
      )}
    </div>
  )
}

export default Monitoring