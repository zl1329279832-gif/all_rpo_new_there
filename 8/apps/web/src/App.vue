<template>
  <el-config-provider :locale="locale">
    <router-view />
  </el-config-provider>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import { useAppStore } from './stores';

const appStore = useAppStore();

const locale = computed(() => (appStore.locale === 'zh-CN' ? zhCn : zhCn));
</script>
