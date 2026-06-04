<template>
  <div class="page-container">
    <div v-if="title || $slots.title" class="page-header">
      <div class="page-title">
        <el-breadcrumb v-if="breadcrumb" separator="/">
          <el-breadcrumb-item v-for="item in breadcrumb" :key="item.path" :to="item.path">
            {{ item.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>
        <h2 v-if="title" class="title-text">{{ title }}</h2>
        <slot name="title" />
      </div>
      <div v-if="$slots.extra" class="page-extra">
        <slot name="extra" />
      </div>
    </div>
    <div v-if="showFilter && ($slots.filter || filterFields)" class="page-filter">
      <slot name="filter">
        <el-form :model="filterModel" inline @submit.prevent>
          <el-form-item v-for="field in filterFields" :key="field.prop" :label="field.label">
            <el-input
              v-if="field.type === 'input'"
              v-model="filterModel[field.prop]"
              :placeholder="field.placeholder || '请输入' + field.label"
              clearable
            />
            <el-select
              v-else-if="field.type === 'select'"
              v-model="filterModel[field.prop]"
              :placeholder="field.placeholder || '请选择' + field.label"
              clearable
            >
              <el-option
                v-for="opt in field.options"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            <el-date-picker
              v-else-if="field.type === 'date'"
              v-model="filterModel[field.prop]"
              :type="field.dateType || 'date'"
              :placeholder="field.placeholder || '请选择' + field.label"
              value-format="YYYY-MM-DD"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </slot>
    </div>
    <div v-if="$slots.toolbar" class="page-toolbar">
      <slot name="toolbar" />
    </div>
    <div class="page-content">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { Search, Refresh } from '@element-plus/icons-vue';

interface BreadcrumbItem {
  path: string;
  title: string;
}

interface FilterField {
  prop: string;
  label: string;
  type: 'input' | 'select' | 'date';
  placeholder?: string;
  options?: Array<{ label: string; value: string | number }>;
  dateType?: string;
}

const props = defineProps<{
  title?: string;
  breadcrumb?: BreadcrumbItem[];
  showFilter?: boolean;
  filterFields?: FilterField[];
}>();

const emit = defineEmits<{
  search: [data: Record<string, any>];
  reset: [];
}>();

const filterModel = reactive<Record<string, any>>({});

function handleSearch() {
  emit('search', { ...filterModel });
}

function handleReset() {
  Object.keys(filterModel).forEach((key) => {
    filterModel[key] = '';
  });
  emit('reset');
}
</script>

<style scoped>
.page-container {
  padding: 16px;
  min-height: calc(100vh - 64px - 16px);
  background: var(--el-bg-color-page);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.page-title {
  flex: 1;
}

.title-text {
  margin: 8px 0 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.page-extra {
  flex-shrink: 0;
}

.page-filter {
  background: var(--el-bg-color);
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-content {
  background: var(--el-bg-color);
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}
</style>
