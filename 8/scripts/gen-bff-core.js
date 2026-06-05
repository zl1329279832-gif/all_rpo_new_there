const fs = require("fs");
const path = require("path");

function w(f, c) {
  const d = path.dirname(f);
  if (!fs.existsSync(d)) fs.mkdirSync(d, { recursive: true });
  fs.writeFileSync(f, c, "utf8");
  console.log("OK:", f);
}

// ========== Shared Components ==========
w("packages/components/package.json", JSON.stringify({
  name: "@platform/components",
  version: "1.0.0",
  private: true,
  main: "src/index.ts",
  peerDependencies: {
    vue: "^3.4.0",
    "element-plus": "^2.4.4"
  }
}, null, 2));

w("packages/components/src/index.ts", `export { default as PageContainer } from "./PageContainer.vue";
export { default as SearchBar } from "./SearchBar.vue";
export { default as TableToolbar } from "./TableToolbar.vue";
export { default as StatusTag } from "./StatusTag.vue";
export { default as EmptyState } from "./EmptyState.vue";
`);

w("packages/components/src/PageContainer.vue", `<template>
  <div class="page-container">
    <div class="page-header" v-if="title || $slots.header">
      <h2 class="page-title">{{ title }}</h2>
      <slot name="header" />
    </div>
    <slot />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string;
}>();
</script>

<style scoped lang="scss">
.page-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  min-height: calc(100vh - 180px);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;

  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0;
  }
}
</style>
`);

w("packages/components/src/StatusTag.vue", `<template>
  <el-tag :type="tagType" size="small">{{ displayText }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  status: number;
  options?: Record<number, { text: string; type: string }>;
}>();

const defaultOptions: Record<number, { text: string; type: string }> = {
  0: { text: "禁用", type: "danger" },
  1: { text: "正常", type: "success" }
};

const displayText = computed(() => {
  const opts = props.options || defaultOptions;
  return opts[props.status]?.text || "未知";
});

const tagType = computed(() => {
  const opts = props.options || defaultOptions;
  return opts[props.status]?.type || "info";
});
</script>
`);

// ========== Messages Page ==========
w("apps/web/src/views/messages/index.vue", `<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">消息中心</h2>
      <div>
        <el-button v-permission="'message:send'" type="primary" @click="handleSend">发送消息</el-button>
        <el-button @click="markAllRead">全部已读</el-button>
      </div>
    </div>
    <el-tabs v-model="activeTab" @tab-change="loadData">
      <el-tab-pane label="全部消息" name="all"></el-tab-pane>
      <el-tab-pane label="系统通知" name="system"></el-tab-pane>
      <el-tab-pane label="待办事项" name="todo"></el-tab-pane>
      <el-tab-pane label="未读消息" name="unread"></el-tab-pane>
    </el-tabs>
    <el-list :data="tableData" v-loading="loading">
      <el-list-item v-for="item in tableData" :key="item.id" @click="handleRead(item)">
        <el-list-item-meta :title="item.title" :description="item.content">
          <template #title>
            <div class="msg-title">
              <el-tag v-if="!item.isRead" type="danger" size="small">新</el-tag>
              <span>{{ item.title }}</span>
              <span class="msg-time">{{ item.createTime }}</span>
            </div>
          </template>
          <template #description>
            <span class="msg-content">{{ item.content }}</span>
            <el-tag size="small" :type="item.type === 'system' ? 'primary' : 'warning'">{{ item.typeName }}</el-tag>
          </template>
        </el-list-item-meta>
      </el-list-item>
    </el-list>
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { getMessageList, markAsRead, markAllAsRead } from "@/api/message";
import type { Message, PageParams } from "@/types";

const loading = ref(false);
const tableData = ref<Message[]>([]);
const total = ref(0);
const activeTab = ref("all");

const query = reactive<PageParams & { isRead?: number; type?: string }>({
  page: 1,
  pageSize: 20
});

async function loadData() {
  loading.value = true;
  try {
    const params = { ...query };
    if (activeTab.value === "unread") params.isRead = 0;
    else if (activeTab.value === "system") params.type = "system";
    else if (activeTab.value === "todo") params.type = "todo";
    const res = await getMessageList(params);
    tableData.value = res.data.list;
    total.value = res.data.total;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

async function handleRead(item: Message) {
  if (!item.isRead) {
    try {
      await markAsRead(item.id);
      item.isRead = 1;
    } catch (e) {
      console.error(e);
    }
  }
}

async function markAllRead() {
  try {
    await markAllAsRead();
    ElMessage.success("已全部标记为已读");
    loadData();
  } catch (e) {
    console.error(e);
  }
}

function handleSend() {
  ElMessage.info("发送消息功能");
}

onMounted(() => {
  loadData();
});
</script>

<style scoped lang="scss">
.msg-title {
  display: flex;
  align-items: center;
  gap: 8px;

  .msg-time {
    margin-left: auto;
    font-size: 12px;
    color: #909399;
  }
}

.msg-content {
  display: block;
  margin-bottom: 8px;
}
</style>
`);

// ========== Logs Page ==========
w("apps/web/src/views/logs/index.vue", `<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">操作日志</h2>
      <div>
        <el-button v-permission="'log:export'" type="primary" @click="handleExport">导出日志</el-button>
      </div>
    </div>
    <div class="search-bar">
      <el-input v-model="query.keyword" placeholder="用户名/模块" style="width: 200px" clearable />
      <el-select v-model="query.status" placeholder="状态" style="width: 120px" clearable>
        <el-option label="成功" :value="1" />
        <el-option label="失败" :value="0" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="width: 280px"
      />
      <el-button type="primary" :icon="Search" @click="loadData">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="module" label="模块" width="120" />
      <el-table-column prop="action" label="操作" width="120" />
      <el-table-column prop="method" label="请求方法" width="80" />
      <el-table-column prop="ip" label="IP地址" width="140" />
      <el-table-column prop="duration" label="耗时(ms)" width="100" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="操作时间" width="180" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { Search, Refresh } from "@element-plus/icons-vue";
import { getLogList, exportLogs } from "@/api/log";
import type { OperationLog, PageParams } from "@/types";

const loading = ref(false);
const tableData = ref<OperationLog[]>([]);
const total = ref(0);
const dateRange = ref<any[]>([]);

const query = reactive<PageParams & { keyword: string; status: number | null }>({
  page: 1,
  pageSize: 10,
  keyword: "",
  status: null
});

async function loadData() {
  loading.value = true;
  try {
    const params = { ...query };
    if (dateRange.value?.length === 2) {
      (params as any).startDate = dateRange.value[0];
      (params as any).endDate = dateRange.value[1];
    }
    const res = await getLogList(params);
    tableData.value = res.data.list;
    total.value = res.data.total;
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
}

function resetQuery() {
  query.page = 1;
  query.keyword = "";
  query.status = null;
  dateRange.value = [];
  loadData();
}

function handleDetail(row: OperationLog) {
  ElMessage.info("日志详情：" + row.id);
}

function handleExport() {
  ElMessage.info("导出日志功能");
}

onMounted(() => {
  loadData();
});
</script>
`);

// ========== Profile Page ==========
w("apps/web/src/views/profile/index.vue", `<template>
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
`);

console.log("=== Components and pages generated! ===");