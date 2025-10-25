import { create } from 'zustand'
import { useAppStore } from './appStore'

interface ApiState {
  // 文件夹相关
  getWatchFolders: () => Promise<any>
  addWatchFolder: (path: string, recursive: boolean) => Promise<any>
  removeWatchFolder: (id: number) => Promise<any>
  
  // 搜索相关
  searchFiles: (query: string, semantic?: boolean) => Promise<any>
  
  // 系统状态
  getSystemStatus: () => Promise<any>
  getMonitoringDashboard: () => Promise<any>
}

export const useApiStore = create<ApiState>((set, get) => ({
  // 获取监控文件夹列表
  getWatchFolders: async () => {
    const { backendUrl } = useAppStore.getState()
    try {
      const response = await fetch(`${backendUrl}/api/folders`)
      const data = await response.json()
      return data
    } catch (error) {
      console.error('获取文件夹列表失败:', error)
      return { success: false, message: '网络连接失败' }
    }
  },

  // 添加监控文件夹
  addWatchFolder: async (path: string, recursive: boolean) => {
    const { backendUrl } = useAppStore.getState()
    try {
      const response = await fetch(`${backendUrl}/api/folders`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ path, recursive }),
      })
      const data = await response.json()
      return data
    } catch (error) {
      console.error('添加文件夹失败:', error)
      return { success: false, message: '网络连接失败' }
    }
  },

  // 删除监控文件夹
  removeWatchFolder: async (id: number) => {
    const { backendUrl } = useAppStore.getState()
    try {
      const response = await fetch(`${backendUrl}/api/folders/${id}`, {
        method: 'DELETE',
      })
      const data = await response.json()
      return data
    } catch (error) {
      console.error('删除文件夹失败:', error)
      return { success: false, message: '网络连接失败' }
    }
  },

  // 搜索文件
  searchFiles: async (query: string, semantic = false) => {
    const { backendUrl } = useAppStore.getState()
    try {
      const response = await fetch(`${backendUrl}/api/search`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ query, semantic }),
      })
      const data = await response.json()
      return data
    } catch (error) {
      console.error('搜索失败:', error)
      return { success: false, message: '网络连接失败' }
    }
  },

  // 获取系统状态
  getSystemStatus: async () => {
    const { backendUrl } = useAppStore.getState()
    try {
      const response = await fetch(`${backendUrl}/api/status`)
      const data = await response.json()
      return data
    } catch (error) {
      console.error('获取系统状态失败:', error)
      return { success: false, message: '网络连接失败' }
    }
  },

  // 获取监控仪表盘数据
  getMonitoringDashboard: async () => {
    const { backendUrl } = useAppStore.getState()
    try {
      const response = await fetch(`${backendUrl}/api/monitoring/dashboard`)
      const data = await response.json()
      return data
    } catch (error) {
      console.error('获取监控数据失败:', error)
      return { success: false, message: '网络连接失败' }
    }
  },
}))
