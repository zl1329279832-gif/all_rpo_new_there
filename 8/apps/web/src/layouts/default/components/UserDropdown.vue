<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <div class="user-info">
      <el-avatar :size="32" :src="userStore.user?.avatar">
        {{ userStore.user?.realName?.charAt(0) }}
      </el-avatar>
      <span class="user-name">{{ userStore.user?.realName }}</span>
      <el-icon><ArrowDown /></el-icon>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="profile">
          <el-icon><User /></el-icon>
          个人中心
        </el-dropdown-item>
        <el-dropdown-item command="password">
          <el-icon><Lock /></el-icon>
          修改密码
        </el-dropdown-item>
        <el-dropdown-item command="settings">
          <el-icon><Setting /></el-icon>
          系统设置
        </el-dropdown-item>
        <el-dropdown-item divided command="logout">
          <el-icon><SwitchButton /></el-icon>
          退出登录
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>

  <el-dialog v-model="passwordDialogVisible" title="修改密码" width="420px">
    <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="passwordForm.oldPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" show-password />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { User, Lock, Setting, SwitchButton, ArrowDown } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { useUserStore } from '@/stores';
import { authApi } from '@/api';

const router = useRouter();
const userStore = useUserStore();

const passwordDialogVisible = ref(false);
const passwordLoading = ref(false);
const passwordFormRef = ref<FormInstance>();

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'));
  } else {
    callback();
  }
};

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
};

async function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/user/detail/' + userStore.user?.id);
      break;
    case 'password':
      passwordDialogVisible.value = true;
      break;
    case 'settings':
      router.push('/settings');
      break;
    case 'logout':
      handleLogout();
      break;
  }
}

async function handleChangePassword() {
  if (!passwordFormRef.value) return;

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true;
      try {
        const res = await authApi.changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword,
        });
        if (res.code === 0) {
          ElMessage.success('密码修改成功，请重新登录');
          passwordDialogVisible.value = false;
          passwordFormRef.value?.resetFields();
          await userStore.logout();
          router.push('/login');
        }
      } finally {
        passwordLoading.value = false;
      }
    }
  });
}

async function handleLogout() {
  await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  });
  await userStore.logout();
  ElMessage.success('已退出登录');
  router.push('/login');
}
</script>

<style scoped lang="scss">
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  .user-name {
    font-size: 14px;
    color: var(--el-text-color-primary);
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
