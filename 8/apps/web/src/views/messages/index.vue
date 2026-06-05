<template>
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
