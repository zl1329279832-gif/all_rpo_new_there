<template>
  <el-tag v-if="option" :type="option.type" :effect="effect">
    {{ option.label }}
  </el-tag>
  <span v-else>{{ status }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface StatusOption {
  [key: string]: { label: string; type: string };
}

const props = withDefaults(defineProps<{
  status: string | number;
  options?: StatusOption;
  effect?: 'dark' | 'light' | 'plain';
}>(), {
  effect: 'light',
});

const defaultOptions: StatusOption = {
  active: { label: '启用', type: 'success' },
  disabled: { label: '禁用', type: 'danger' },
  pending: { label: '待处理', type: 'warning' },
  processing: { label: '处理中', type: 'primary' },
  completed: { label: '已完成', type: 'success' },
  cancelled: { label: '已取消', type: 'info' },
  success: { label: '成功', type: 'success' },
  fail: { label: '失败', type: 'danger' },
  paid: { label: '已支付', type: 'success' },
  shipped: { label: '已发货', type: 'primary' },
  refunded: { label: '已退款', type: 'warning' },
  resolved: { label: '已解决', type: 'success' },
  closed: { label: '已关闭', type: 'info' },
  low: { label: '低', type: 'info' },
  medium: { label: '中', type: 'warning' },
  high: { label: '高', type: 'danger' },
  urgent: { label: '紧急', type: 'danger' },
  bug: { label: 'Bug', type: 'danger' },
  feature: { label: '功能需求', type: 'primary' },
  consult: { label: '咨询', type: 'success' },
  complaint: { label: '投诉', type: 'warning' },
  system: { label: '系统通知', type: 'primary' },
  business: { label: '业务通知', type: 'success' },
  warning: { label: '警告', type: 'warning' },
  error: { label: '错误', type: 'danger' },
};

const option = computed(() => {
  const opts = props.options || defaultOptions;
  return opts[String(props.status)];
});
</script>
