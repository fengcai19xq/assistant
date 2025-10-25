import { create } from 'zustand'

interface AppState {
  appVersion: string
  backendUrl: string
  backendStatus: 'connected' | 'disconnected' | 'checking'
  setAppVersion: (version: string) => void
  setBackendUrl: (url: string) => void
  setBackendStatus: (status: 'connected' | 'disconnected' | 'checking') => void
}

export const useAppStore = create<AppState>((set) => ({
  appVersion: '1.0.0',
  backendUrl: 'http://localhost:8080/assistant',
  backendStatus: 'checking',
  setAppVersion: (version) => set({ appVersion: version }),
  setBackendUrl: (url) => set({ backendUrl: url }),
  setBackendStatus: (status) => set({ backendStatus: status }),
}))
