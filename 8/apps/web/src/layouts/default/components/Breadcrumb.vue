<template>
  <el-breadcrumb separator="/" class="breadcrumb">
    <el-breadcrumb-item
      v-for="(item, index) in breadcrumbs"
      :key="index"
      :to="index < breadcrumbs.length - 1 ? item.path : undefined"
    >
      <el-icon v-if="item.icon" class="breadcrumb-icon"><component :is="item.icon" /></el-icon>
      <span>{{ item.title }}</span>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

interface BreadcrumbItem {
  path: string;
  title: string;
  icon?: string;
}

const route = useRoute();

const breadcrumbs = computed<BreadcrumbItem[]>(() => {
  const matched = route.matched.filter((item) => item.meta && item.meta.title);
  return matched.map((item) => ({
    path: item.path,
    title: item.meta.title as string,
    icon: item.meta.icon as string,
  }));
});
</script>

<style scoped lang="scss">
.breadcrumb {
  .breadcrumb-icon {
    margin-right: 4px;
  }
}
</style>
