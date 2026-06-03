import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebar: {
      opened: true,
      withoutAnimation: false
    },
    theme: 'light',
    language: 'zh-CN',
    settings: {
      showTagsView: true,
      showSidebarLogo: true,
      fixedHeader: true
    }
  }),
  actions: {
    toggleSidebar() {
      this.sidebar.opened = !this.sidebar.opened
      this.sidebar.withoutAnimation = false
    },
    closeSidebar(withoutAnimation) {
      this.sidebar.opened = false
      this.sidebar.withoutAnimation = withoutAnimation
    },
    toggleTheme() {
      this.theme = this.theme === 'light' ? 'dark' : 'light'
    },
    setLanguage(lang) {
      this.language = lang
    },
    updateSettings(settings) {
      this.settings = { ...this.settings, ...settings }
    }
  },
  persist: {
    key: 'wms-app',
    storage: localStorage,
    paths: ['sidebar', 'theme', 'language', 'settings']
  }
})
