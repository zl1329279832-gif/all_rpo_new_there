import { defineStore } from "pinia";
import { ref } from "vue";

export const useAppStore = defineStore(
  "app",
  () => {
    const sidebarCollapsed = ref(false);
    const globalFilters = ref<Record<string, any>>({
      dateRange: [],
      status: "",
      keyword: ""
    });
    const unreadMessageCount = ref(0);

    function toggleSidebar() {
      sidebarCollapsed.value = !sidebarCollapsed.value;
    }

    function setGlobalFilters(filters: Record<string, any>) {
      globalFilters.value = { ...globalFilters.value, ...filters };
    }

    function resetGlobalFilters() {
      globalFilters.value = {
        dateRange: [],
        status: "",
        keyword: ""
      };
    }

    function setUnreadCount(count: number) {
      unreadMessageCount.value = count;
    }

    return {
      sidebarCollapsed,
      globalFilters,
      unreadMessageCount,
      toggleSidebar,
      setGlobalFilters,
      resetGlobalFilters,
      setUnreadCount
    };
  },
  {
    persist: {
      key: "platform-app",
      paths: ["sidebarCollapsed", "globalFilters"]
    }
  }
);
