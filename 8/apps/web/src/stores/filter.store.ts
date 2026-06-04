import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getStorage, setStorage, removeStorage } from '@platform/shared-utils';

export const useFilterStore = defineStore('filter', () => {
  const filters = ref<Record<string, Record<string, any>>>(getStorage('global_filters') || {});

  function setFilter(pageKey: string, filter: Record<string, any>) {
    filters.value[pageKey] = { ...filters.value[pageKey], ...filter };
    saveToStorage();
  }

  function getFilter(pageKey: string): Record<string, any> {
    return filters.value[pageKey] || {};
  }

  function clearFilter(pageKey: string) {
    delete filters.value[pageKey];
    saveToStorage();
  }

  function clearAllFilters() {
    filters.value = {};
    removeStorage('global_filters');
  }

  function saveToStorage() {
    setStorage('global_filters', filters.value);
  }

  function setGlobal(key: string, value: any) {
    filters.value['__global__'] = filters.value['__global__'] || {};
    filters.value['__global__'][key] = value;
    saveToStorage();
  }

  function getGlobal(key: string): any {
    return filters.value['__global__']?.[key];
  }

  return {
    filters,
    setFilter,
    getFilter,
    clearFilter,
    clearAllFilters,
    setGlobal,
    getGlobal,
  };
});
