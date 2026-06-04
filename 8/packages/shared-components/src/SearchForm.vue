<template>
  <div class="search-form">
    <el-form :model="model" :inline="inline" :label-width="labelWidth" @submit.prevent>
      <slot>
        <el-form-item v-for="field in fields" :key="field.prop" :label="field.label">
          <el-input
            v-if="field.type === 'input'"
            v-model="model[field.prop]"
            :placeholder="field.placeholder || '请输入' + field.label"
            :clearable="field.clearable !== false"
            :disabled="field.disabled"
          />
          <el-select
            v-else-if="field.type === 'select'"
            v-model="model[field.prop]"
            :placeholder="field.placeholder || '请选择' + field.label"
            :clearable="field.clearable !== false"
            :disabled="field.disabled"
            :multiple="field.multiple"
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
            v-model="model[field.prop]"
            :type="field.dateType || 'date'"
            :placeholder="field.placeholder || '请选择' + field.label"
            :clearable="field.clearable !== false"
            :disabled="field.disabled"
            value-format="YYYY-MM-DD"
          />
          <el-input-number
            v-else-if="field.type === 'number'"
            v-model="model[field.prop]"
            :min="field.min"
            :max="field.max"
            :placeholder="field.placeholder || '请输入' + field.label"
            :disabled="field.disabled"
            controls-position="right"
          />
        </el-form-item>
      </slot>
      <el-form-item>
        <slot name="actions">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button v-if="showExpand" @click="expanded = !expanded">
            {{ expanded ? '收起' : '展开' }}
            <el-icon><ArrowDown :class="{ expanded }" /></el-icon>
          </el-button>
        </slot>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { Search, Refresh, ArrowDown } from '@element-plus/icons-vue';

interface SearchField {
  prop: string;
  label: string;
  type: 'input' | 'select' | 'date' | 'number';
  placeholder?: string;
  options?: Array<{ label: string; value: string | number }>;
  dateType?: string;
  clearable?: boolean;
  disabled?: boolean;
  multiple?: boolean;
  min?: number;
  max?: number;
}

const props = withDefaults(defineProps<{
  fields?: SearchField[];
  inline?: boolean;
  labelWidth?: string;
  showExpand?: boolean;
  defaultValues?: Record<string, any>;
}>(), {
  inline: true,
  labelWidth: '80px',
  showExpand: false,
});

const emit = defineEmits<{
  search: [data: Record<string, any>];
  reset: [];
}>();

const expanded = ref(false);
const model = reactive<Record<string, any>>({ ...props.defaultValues });

function handleSearch() {
  emit('search', { ...model });
}

function handleReset() {
  Object.keys(model).forEach((key) => {
    model[key] = props.defaultValues?.[key] ?? '';
  });
  emit('reset');
}
</script>

<style scoped>
.search-form {
  padding: 16px;
  background: var(--el-bg-color);
  border-radius: 8px;
}

.expanded {
  transform: rotate(180deg);
  transition: transform 0.3s;
}
</style>
