<template>
  <el-tooltip :content="isFullscreen ? '退出全屏' : '全屏显示'" placement="bottom">
    <el-icon class="header-icon" @click="toggleFullscreen">
      <FullScreen v-if="!isFullscreen" />
      <Aim v-else />
    </el-icon>
  </el-tooltip>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { FullScreen, Aim } from '@element-plus/icons-vue';

const isFullscreen = ref(false);

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen();
  } else {
    document.exitFullscreen();
  }
}

function handleFullscreenChange() {
  isFullscreen.value = !!document.fullscreenElement;
}

onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullscreenChange);
});

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange);
});
</script>

<style scoped lang="scss">
.header-icon {
  font-size: 18px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  color: var(--el-text-color-regular);
  transition: all 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
    color: var(--el-color-primary);
  }
}
</style>
