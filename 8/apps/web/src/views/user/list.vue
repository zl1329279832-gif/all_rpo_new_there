<template>
  <PageContainer title="用户列表" :show-filter="true">
    <template #filter>
      <el-form :model="filterForm" inline @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="filterForm.keyword" placeholder="请输入用户名/姓名" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="filterForm.department" placeholder="请选择部门" clearable>
            <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="filterForm.role" placeholder="请选择角色" clearable>
            <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </template>

    <template #toolbar>
      <div class="toolbar-left">
        <el-button type="primary" v-permission="'user:add'" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
        <el-button type="danger" v-permission="'user:delete'" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
        <el-button v-permission="'user:export'" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </template>

    <DataTable
      ref="dataTableRef"
      :columns="columns"
      :data="tableData"
      :total="total"
      :page-size="pageSize"
      :loading="loading"
      selectable
      @page-change="handlePageChange"
      @selection-change="handleSelectionChange"
    >
      <template #actions="{ row }">
        <el-button type="primary" link v-permission="'user:edit'" @click="handleEdit(row)">
          编辑
        </el-button>
        <el-button type="primary" link @click="handleView(row)">
          详情
        </el-button>
        <el-button :type="row.status === 'active' ? 'warning' : 'success'" link v-permission="'user:status'" @click="handleToggleStatus(row)">
          {{ row.status === 'active' ? '禁用' : '启用' }}
        </el-button>
        <el-button type="danger" link v-permission="'user:delete'" @click="handleDelete(row)">
          删除
        </el-button>
      </template>
    </DataTable>
  </PageContainer>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
    <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="formData.username" :disabled="isEdit" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="姓名" prop="realName">
            <el-input v-model="formData.realName" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="formData.phone" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="部门" prop="department">
            <el-select v-model="formData.department" style="width: 100%">
              <el-option v-for="dept in departments" :key="dept.id" :label="dept.name" :value="dept.name" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="角色" prop="roles">
            <el-select v-model="formData.roles" multiple style="width: 100%">
              <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.code" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio value="active">启用</el-radio>
              <el-radio value="disabled">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12" v-if="!isEdit">
          <el-form-item label="密码" prop="password">
            <el-input v-model="formData.password" type="password" show-password />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Search, Refresh, Plus, Delete, Download } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { useFilterStore } from '@/stores';
import { userApi } from '@/api';
import type { User } from '@platform/shared-types';

const router = useRouter();
const filterStore = useFilterStore();
const savedFilter = filterStore.getFilter('user_list');

const loading = ref(false);
const submitLoading = ref(false);
const dialogVisible = ref(false);
const isEdit = ref(false);
const formRef = ref<FormInstance>();
const dataTableRef = ref<any>();

const page = ref(1);
const pageSize = ref(20);
const total = ref(0);
const tableData = ref<User[]>([]);
const selectedIds = ref<string[]>([]);
const departments = ref<Array<{ id: string; name: string; parentId: string | null }>>([]);
const roles = ref<Array<{ id: string; name: string; code: string }>>([]);

const filterForm = reactive({
  keyword: savedFilter.keyword || '',
  department: savedFilter.department || '',
  status: savedFilter.status || '',
  role: savedFilter.role || '',
  dateRange: savedFilter.dateRange || [],
});

const dialogTitle = computed(() => (isEdit.value ? '编辑用户' : '新增用户'));

const formData = reactive<Partial<User> & { password?: string }>({
  username: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  roles: [],
  status: 'active',
  password: '',
});

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度为4-20个字符', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  department: [{ required: true, message: '请选择部门', trigger: 'change' }],
  roles: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

const columns = [
  { prop: 'username', label: '用户名', minWidth: 120 },
  { prop: 'realName', label: '姓名', minWidth: 100 },
  { prop: 'email', label: '邮箱', minWidth: 180 },
  { prop: 'phone', label: '手机号', minWidth: 120 },
  { prop: 'department', label: '部门', minWidth: 120 },
  { prop: 'roles', label: '角色', minWidth: 120, slotName: 'roles' },
  { prop: 'status', label: '状态', minWidth: 80, type: 'status' },
  { prop: 'createdAt', label: '创建时间', minWidth: 160, type: 'date' },
];

async function loadData() {
  loading.value = true;
  try {
    const params: any = {
      page: page.value,
      pageSize: pageSize.value,
      keyword: filterForm.keyword || undefined,
      department: filterForm.department || undefined,
      status: filterForm.status || undefined,
      role: filterForm.role || undefined,
    };
    if (filterForm.dateRange?.length === 2) {
      params.startDate = filterForm.dateRange[0];
      params.endDate = filterForm.dateRange[1];
    }

    const res = await userApi.getList(params);
    if (res.code === 0) {
      tableData.value = res.data.list;
      total.value = res.data.total;
    }
  } finally {
    loading.value = false;
  }
}

async function loadMeta() {
  const [deptRes, roleRes] = await Promise.all([userApi.getDepartments(), userApi.getRoles()]);
  if (deptRes.code === 0) departments.value = deptRes.data;
  if (roleRes.code === 0) roles.value = roleRes.data;
}

function handleSearch() {
  page.value = 1;
  filterStore.setFilter('user_list', filterForm);
  loadData();
}

function handleReset() {
  filterForm.keyword = '';
  filterForm.department = '';
  filterForm.status = '';
  filterForm.role = '';
  filterForm.dateRange = [];
  filterStore.clearFilter('user_list');
  page.value = 1;
  loadData();
}

function handlePageChange(p: number, ps: number) {
  page.value = p;
  pageSize.value = ps;
  loadData();
}

function handleSelectionChange(selection: User[]) {
  selectedIds.value = selection.map((item) => item.id);
}

function handleAdd() {
  isEdit.value = false;
  Object.assign(formData, {
    username: '',
    realName: '',
    email: '',
    phone: '',
    department: '',
    roles: [],
    status: 'active',
    password: '',
  });
  dialogVisible.value = true;
}

function handleEdit(row: User) {
  isEdit.value = true;
  Object.assign(formData, row);
  dialogVisible.value = true;
}

function handleView(row: User) {
  router.push(`/user/detail/${row.id}`);
}

async function handleToggleStatus(row: User) {
  const newStatus = row.status === 'active' ? 'disabled' : 'active';
  await ElMessageBox.confirm(`确定要${newStatus === 'active' ? '启用' : '禁用'}用户「${row.realName}」吗？`, '提示', {
    type: 'warning',
  });
  const res = await userApi.updateStatus(row.id, newStatus as 'active' | 'disabled');
  if (res.code === 0) {
    ElMessage.success('操作成功');
    loadData();
  }
}

async function handleDelete(row: User) {
  await ElMessageBox.confirm(`确定要删除用户「${row.realName}」吗？`, '提示', {
    type: 'warning',
  });
  const res = await userApi.remove(row.id);
  if (res.code === 0) {
    ElMessage.success('删除成功');
    loadData();
  }
}

async function handleBatchDelete() {
  await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个用户吗？`, '提示', {
    type: 'warning',
  });
  const res = await userApi.batchRemove(selectedIds.value);
  if (res.code === 0) {
    ElMessage