<template>
  <div class="exception-page">
    <div class="exception-content">
      <div class="exception-code" :class="`code-${code}`">
        {{ code }}
      </div>
      <h2 class="exception-title">{{ title }}</h2>
      <p class="exception-desc">{{ description }}</p>
      <div class="exception-actions">
        <slot name="actions">
          <el-button type="primary" @click="handleBack">
            {{ backText }}
          </el-button>
          <el-button @click="handleRefresh">
            重新加载
          </el-button>
        </slot>
      </div>
      <div v-if="$slots.extra" class="exception-extra">
        <slot name="extra" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const props = withDefaults(defineProps<{
  code?: '403' | '404' | '500' | '502' | '503' | '504';
  title?: string;
  description?: string;
  backText?: string;
}>(), {
  code: '404',
  backText: '返回首页',
});

const router = useRouter();

const presets: Record<string, { title: string; description: string }> = {
  '403': {
    title: '访问被拒绝',
    description: '抱歉，您没有权限访问该页面，请联系管理员获取权限。',
  },
  '404': {
    title: '页面不存在',
    description: '抱歉，您访问的页面不存在或已被删除，请检查URL是否正确。',
  },
  '500': {
    title: '服务器内部错误',
    description: '抱歉，服务器出现了一些问题，请稍后再试或联系技术支持。',
  },
  '502': {
    title: '网关错误',
    description: '抱歉，服务器作为网关或代理，从上游服务器收到无效响应。',
  },
  '503': {
    title: '服务不可用',
    description: '抱歉，服务器暂时无法处理您的请求，可能是由于维护或过载。',
  },
  '504': {
    title: '网关超时',
    description: '抱歉，服务器作为网关或代理，没有及时从上游服务器收到请求。',
  },
};

const title = computed(() => props.title || presets[props.code]?.title || '错误');
const description = computed(() => props.description || presets[props.code]?.description || '发生了一个错误');

function handleBack() {
  router.push('/');
}

function handleRefresh() {
  window.location.reload();
}
</script>

<style scoped>
.exception-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 64px);
  background: var(--el-bg-color-page);
}

.exception-content {
  text-align: center;
  padding: 48px;
}

.exception-code {
  font-size: 120px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 24px;
  background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-light-3));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.code-403 {
  background: linear-gradient(135deg, var(--el-color-warning), var(--el-color-warning-light-3));
  -webkit-background-clip: text;
  background-clip: text;
}

.code-404 {
  background: linear-gradient(135deg, var(--el-color-info), var(--el-color-info-light-3));
  -webkit-background-clip: text;
  background-clip: text;
}

.code-500,
.code-502,
.code-503,
.code-504 {
  background: linear-gradient(135deg, var(--el-color-danger), var(--el-color-danger-light-3));
  -webkit-background-clip: text;
  background-clip: text;
}

.exception-title {
  font-size: 28px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 16px;
}

.exception-desc {
  font-size: 16px;
  color: var(--el-text-color-regular);
  margin: 0 0 32px;
  max-width: 480px;
  line-height: 1.6;
}

.exception-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 32px;
}

.exception-extra {
  max-width: 560px;
  margin: 0 auto;
  padding: 24px;
  background: var(--el-bg-color);
  border-radius: 8px;
  text-align: left;
}
</style>
