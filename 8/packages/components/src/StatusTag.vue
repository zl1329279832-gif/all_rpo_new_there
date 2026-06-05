<template>
  <el-tag :type="tagType" size="small">{{ displayText }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  status: number;
  options?: Record<number, { text: string; type: string }>;
}>();

const defaultOptions: Record<number, { text: string; type: string }> = {
  0: { text: "禁用", type: "danger" },
  1: { text: "正常", type: "success" }
};

const displayText = computed(() => {
  const opts = props.options || defaultOptions;
  return opts[props.status]?.text || "未知";
});

const tagType = computed(() => {
  const opts = props.options || defaultOptions;
  return opts[props.status]?.type || "info";
});
</script>
