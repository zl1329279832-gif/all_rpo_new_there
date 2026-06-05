<template>
  <div class="page-container">
    <div class="profile-header">
      <el-avatar :size="80" :src="userStore.userInfo?.avatar">
        {{ userStore.userInfo?.realName?.charAt(0) }}
      </el-avatar>
      <div class="profile-info">
        <h2>{{ userStore.userInfo?.realName }}</h2>
        <p>{{ userStore.userInfo?.roleName }} | {{ userStore.userInfo?.department }}</p>
        <p class="text-muted">用户名：{{ userStore.userInfo?.username }}</p>
      </div>
    </div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="basic">
        <el-form label-width="100px" class="profile-form">
          <el-form-item label="姓名">
            <el-input v-model="form.realName" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" />
          </el-form-item>
          <el-form-item label="部门">
            <el-input v-model="form.department" disabled />
          </el-form-item>
          <el-form-item label="角色">
            <el-input v-model="form.roleName" disabled />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSave">保存修改</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="修改密码" name="password">
        <el-form label-width="100px" class="profile-form">
          <el-form-item label="原密码">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user";

const userStore = useUserStore();
const activeTab = ref("basic");

const form = reactive({
  realName: "",
  email: "",
  phone: "",
  department: "",
  roleName: ""
});

const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
});

onMounted(() => {
  if (userStore.userInfo) {
    form.realName = userStore.userInfo.realName;
    form.email = userStore.userInfo.email;
    form.phone = userStore.userInfo.phone;
    form.department = userStore.userInfo.department;
    form.roleName = userStore.userInfo.roleName;
  }
});

function handleSave() {
  ElMessage.success("保存成功");
}

function handleChangePassword() {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error("两次输入的密码不一致");
    return;
  }
  ElMessage.success("密码修改成功");
}
</script>

<style scoped lang="scss">
.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #ebeef5;

  .profile-info {
    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      font-weight: 600;
    }

    p {
      margin: 4px 0;
      color: #606266;

      &.text-muted {
        color: #909399;
        font-size: 13px;
      }
    }
  }
}

.profile-form {
  max-width: 500px;
  margin-top: 24px;
}
</style>
