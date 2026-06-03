<template>
  <div class="error-page">
    <el-result
      icon="warning"
      title="404"
      sub-title="抱歉，您访问的页面不存在"
    >
      <template #extra>
        <el-button type="primary" @click="goHome">
          <el-icon><House /></el-icon>返回首页
        </el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { House } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}
</script>

<style lang="scss" scoped>
.error-page {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  :deep(.el-result) {
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16px;
    padding: 60px 80px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  }
  
  :deep(.el-result__title) {
    font-size: 72px !important;
    font-weight: 700;
    color: #409eff !important;
    line-height: 1;
    margin-bottom: 10px !important;
  }
  
  :deep(.el-result__subtitle) {
    font-size: 16px !important;
    color: #909399 !important;
  }
}
</style>
