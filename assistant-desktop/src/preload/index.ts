import { contextBridge, ipcRenderer } from 'electron'

// 暴露安全的API给渲染进程
contextBridge.exposeInMainWorld('electronAPI', {
  // 获取应用版本
  getAppVersion: () => ipcRenderer.invoke('get-app-version'),
  
  // 显示消息框
  showMessageBox: (options: any) => ipcRenderer.invoke('show-message-box', options),
  
  // 显示打开对话框
  showOpenDialog: (options: any) => ipcRenderer.invoke('show-open-dialog', options),
  
  // 监听事件
  on: (channel: string, callback: Function) => {
    const validChannels = ['reindex-files', 'add-folder']
    if (validChannels.includes(channel)) {
      ipcRenderer.on(channel, callback)
    }
  },
  
  // 移除监听器
  removeAllListeners: (channel: string) => {
    ipcRenderer.removeAllListeners(channel)
  }
})

// 类型声明
declare global {
  interface Window {
    electronAPI: {
      getAppVersion: () => Promise<string>
      showMessageBox: (options: any) => Promise<any>
      showOpenDialog: (options: any) => Promise<any>
      on: (channel: string, callback: Function) => void
      removeAllListeners: (channel: string) => void
    }
  }
}
