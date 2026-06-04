<template>
  <ExceptionPage code="404">
    <template #actions>
      <el-button type="primary" @click="goHome">返回首页</el-button>
      <el-button @click="goBack">返回上一页</el-button>
    </template>
  </ExceptionPage>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import ExceptionPage from '@platform/shared-components/src/ExceptionPage.vue';

const router = useRouter();

function goHome() {
  router.push('/');
}

function goBack() {
  router.go(-1);
}
</script>
