<template>
  <div class="search-bar">
    <el-input
      v-model="keyword"
      placeholder="搜索菜单、功能..."
      class="search-input"
      clearable
      @keyup.enter="handleSearch"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();
const keyword = ref('');

function handleSearch() {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词');
    return;
  }
  router.push({ path: '/search', query: { q: keyword.value } });
}
</script>

<style scoped lang="scss">
.search-bar {
  .search-input {
    width: 240px;
  }
}
</style>
