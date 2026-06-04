<template>
  <ExceptionPage code="500">
    <template #actions>
      <el-button type="primary" @click="refresh">重新加载</el-button>
      <el-button @click="goHome">返回首页</el-button>
    </template>
    <template #extra>
      <el-alert title="错误详情" type="error" show-icon>
        <template #default>
          <p>时间：{{ errorTime }}</p>
          <p>URL：{{ currentUrl }}</p>
          <p>如果问题持续存在，请联系技术支持</p>
        </template>
      </el-alert>
    </template>
  </ExceptionPage>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { formatDate } from '@platform/shared-utils';
import ExceptionPage from '@platform/shared-components/src/ExceptionPage.vue';

const router = useRouter();

const errorTime = ref(formatDate(new Date()));
const currentUrl = ref(window.location.href);

function refresh() {
  window.location.reload();
}

function goHome() {
  router.push('/');
}
</script>
