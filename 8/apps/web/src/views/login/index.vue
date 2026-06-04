<template>
  <div class="login-container">
    <div class="login-left">
      <div class="login-brand">
        <img src="/favicon.svg" alt="Logo" class="brand-logo" />
        <h1 class="brand-title">集团运营中台</h1>
        <p class="brand-desc">统一身份认证 · 多应用协同 · 数据智能分析</p>
      </div>
      <div class="login-features">
        <div class="feature-item">
          <el-icon><User /></el-icon>
          <div>
            <h3>统一身份认证</h3>
            <p>支持多因子认证，保障账户安全</p>
          </div>
        </div>
        <div class="feature-item">
          <el-icon><Grid /></el-icon>
          <div>
            <h3>多应用协同</h3>
            <p>一站式访问集团所有业务系统</p>
          </div>
        </div>
        <div class="feature-item">
          <el-icon><DataLine /></el-icon>
          <div>
            <h3>数据智能分析</h3>
            <p>实时数据看板，助力决策分析</p>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-card">
        <h2 class="login-title">用户登录</h2>
        <p class="login-subtitle">请输入您的账号信息</p>

        <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              auto-complete="username"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              auto-complete="current-password"
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="captcha" v-if="captchaEnabled">
            <div class="captcha-wrapper">
              <el-input
                v-model="loginForm.captcha"
                placeholder="验证码"
                size="large"
                maxlength="4"
                auto-complete="off"
              >
                <template #prefix>
                  <el-icon><Key /></el-icon>
                </template>
              </el-input>
              <div class="captcha-image" @click="refreshCaptcha" v-if="captchaData">
                <img :src="captchaData.image" alt="验证码" />
              </div>
            </div>
          </el-form-item>

          <el-form-item>
            <div class="login-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <el-button type="primary" link>忘记密码？</el-button>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-tips">
          <el-alert
            title="演示账号"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>管理员：admin / admin123</p>
              <p>普通用户：user / user123</p>
            </template>
          </el-alert>
        </div>

        <div class="login-footer">
          <p>© 2024 集团运营中台 · 技术支持中心</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { User, Lock, Key, Grid, DataLine } from '@element-plus/icons-vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useUserStore, useAppStore } from '@/stores';
import { authApi } from '@/api';
import { getStorage, setStorage } from '@platform/shared-utils';
import type { LoginRequest } from '@platform/shared-types';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const appStore = useAppStore();

const loading = ref(false);
const captchaEnabled = ref(false);
const captchaData = ref<{ id: string; image: string } | null>(null);
const rememberMe = ref(false);
const loginFormRef = ref<FormInstance>();

const loginForm = reactive<LoginRequest>({
  username: '',
  password: '',
  captcha: '',
});

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
};

async function refreshCaptcha() {
  try {
    const res = await authApi.getCaptcha();
    if (res.code === 0) {
      captchaData.value = res.data;
      loginForm.captcha = '';
    }
  } catch (error) {
    console.error('获取验证码失败:', error);
  }
}

async function handleLogin() {
  if (!loginFormRef.value) return;

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const res = await userStore.login(loginForm);
        if (res.code === 0) {
          if (rememberMe.value) {
            setStorage('remember_username', loginForm.username, { expire: 7 * 24 * 60 * 60 * 1000 });
          }

          await Promise.all([
            appStore.loadApps(),
            appStore.loadMenus(),
          ]);

          ElMessage.success('登录成功');

          const redirect = route.query.redirect as string;
          router.push(redirect || '/dashboard');
        }
      } finally {
        loading.value = false;
      }
    }
  });
}

onMounted(() => {
  const savedUsername = getStorage<string>('remember_username');
  if (savedUsername) {
    loginForm.username = savedUsername;
    rememberMe.value = true;
  }
});
</script>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  display: flex;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-left {
  flex: 1;
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
}

.login-brand {
  margin-bottom: 60px;

  .brand-logo {
    width: 64px;
    height: 64px;
    margin-bottom: 20px;
  }

  .brand-title {
    font-size: 42px;
    font-weight: 700;
    margin: 0 0 12px;
    color: #fff;
  }

  .brand-desc {
    font-size: 18px;
    margin: 0;
    opacity: 0.9;
  }
}

.login-features {
  display: flex;
  flex-direction: column;
  gap: 24px;

  .feature-item {
    display: flex;
    align-items: flex-start;
    gap: 16px;

    .el-icon {
      font-size: 32px;
      padding: 12px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 12px;
      flex-shrink: 0;
    }

    h3 {
      font-size: 18px;
      font-weight: 600;
      margin: 0 0 4px;
    }

    p {
      font-size: 14px;
      margin: 0;
      opacity: 0.8;
    }
  }
}

.login-right {
  width: 520px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-card {
  width: 100%;
  max-width: 400px;

  .login-title {
    font-size: 28px;
    font-weight: 700;
    margin: 0 0 8px;
    color: var(--el-text-color-primary);
  }

  .login-subtitle {
    font-size: 14px;
    color: var(--el-text-color-secondary);
    margin: 0 0 32px;
  }
}

.login-form {
  .captcha-wrapper {
    display: flex;
    gap: 12px;
  }

  .captcha-image {
    width: 120px;
    height: 40px;
    border-radius: 4px;
    overflow: hidden;
    cursor: pointer;
    border: 1px solid var(--el-border-color);

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .login-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .login-btn {
    width: 100%;
    height: 44px;
    font-size: 16px;
    font-weight: 500;
  }
}

.login-tips {
  margin-top: 24px;

  :deep(.el-alert) {
    --el-alert-padding: 12px;
    font-size: 12px;

    p {
      margin: 4px 0;
    }
  }
}

.login-footer {
  margin-top: 32px;
  text-align: center;
  font-size: 12px;
  color: var(--el-text-color-secondary);

  p {
    margin: 0;
  }
}

@media (max-width: 1024px) {
  .login-left {
    display: none;
  }

  .login-right {
    width: 100%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  .login-card {
    background: #fff;
    padding: 40px;
    border-radius: 16px;
  }
}
</style>
