<template>
  <div class="logo-container">
    <div class="logo-wrapper">
      <img src="/favicon.svg" alt="Logo" class="logo-img" />
      <transition name="fade">
        <span v-if="!collapsed" class="logo-text">集团运营中台</span>
      </transition>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  collapsed?: boolean;
}>();
</script>

<style scoped lang="scss">
.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background: linear-gradient(135deg, var(--el-color-primary) 0%, var(--el-color-primary-dark-2) 100%);
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-img {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}
</style>
