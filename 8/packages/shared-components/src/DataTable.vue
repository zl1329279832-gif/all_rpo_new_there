<template>
  <div class="data-table">
    <el-table
      v-loading="loading"
      :data="data"
      :border="border"
      :stripe="stripe"
      :height="height"
      :default-sort="defaultSort"
      @sort-change="handleSortChange"
      @selection-change="handleSelectionChange"
    >
      <el-table-column v-if="selectable" type="selection" width="55" />
      <el-table-column v-if="showIndex" type="index" label="序号" width="60" />
      <el-table-column
        v-for="col in columns"
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :min-width="col.minWidth"
        :fixed="col.fixed"
        :sortable="col.sortable"
        :show-overflow-tooltip="col.tooltip !== false"
        :align="col.align || 'left'"
      >
        <template #default="{ row }">
          <slot v-if="col.slotName" :name="col.slotName" :row="row" :col="col" />
          <template v-else-if="col.type === 'status'">
            <StatusTag :status="row[col.prop]" :options="col.statusOptions" />
          </template>
          <template v-else-if="col.type === 'date'">
            {{ formatDate(row[col.prop], col.dateFormat) }}
          </template>
          <template v-else-if="col.type === 'money'">
            {{ formatMoney(row[col.prop]) }}
          </template>
          <template v-else-if="col.type === 'image'">
            <el-image
              :src="row[col.prop]"
              :preview-src-list="[row[col.prop]]"
              :preview-teleported="true"
              style="width: 40px; height: 40px"
              fit="cover"
            />
          </template>
          <template v-else-if="col.type === 'tag'">
            <el-tag v-if="row[col.prop]" :type="col.tagType || 'primary'">
              {{ row[col.prop] }}
            </el-tag>
          </template>
          <template v-else-if="col.enum">
            {{ col.enum[row[col.prop]] || row[col.prop] }}
          </template>
          <template v-else>
            {{ row[col.prop] }}
          </template>
        </template>
      </el-table-column>
      <el-table-column v-if="$slots.actions" label="操作" :width="actionsWidth || 180" fixed="right" align="center">
        <template #default="{ row }">
          <slot name="actions" :row="row" />
        </template>
      </el-table-column>
    </el-table>

    <div v-if="showPagination" class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="pageSizes"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { formatDate, formatMoney } from '@platform/shared-utils';

interface StatusOption {
  [key: string]: { label: string; type: string };
}

interface TableColumn {
  prop: string;
  label: string;
  width?: string | number;
  minWidth?: string | number;
  fixed?: string | boolean;
  sortable?: string | boolean;
  tooltip?: boolean;
  align?: 'left' | 'center' | 'right';
  slotName?: string;
  type?: 'status' | 'date' | 'money' | 'image' | 'tag';
  dateFormat?: string;
  statusOptions?: StatusOption;
  tagType?: string;
  enum?: Record<string, string>;
}

const props = withDefaults(defineProps<{
  columns: TableColumn[];
  data: any[];
  loading?: boolean;
  border?: boolean;
  stripe?: boolean;
  height?: string | number;
  defaultSort?: { prop: string; order: string };
  showIndex?: boolean;
  selectable?: boolean;
  showPagination?: boolean;
  total?: number;
  pageSize?: number;
  pageSizes?: number[];
  actionsWidth?: number;
}>(), {
  loading: false,
  border: false,
  stripe: true,
  showIndex: true,
  selectable: false,
  showPagination: true,
  pageSize: 20,
  pageSizes: () => [10, 20, 50, 100],
  total: 0,
});

const emit = defineEmits<{
  'update:pageSize': [value: number];
  'update:currentPage': [value: number];
  'page-change': [page: number, pageSize: number];
  'sort-change': [sort: { prop: string; order: string }];
  'selection-change': [selection: any[]];
}>();

const currentPage = ref(1);
const pageSize = ref(props.pageSize);

watch(
  () => props.pageSize,
  (val) => {
    pageSize.value = val;
  }
);

function handleSizeChange(val: number) {
  pageSize.value = val;
  currentPage.value = 1;
  emit('update:pageSize', val);
  emit('page-change', currentPage.value, val);
}

function handleCurrentChange(val: number) {
  currentPage.value = val;
  emit('update:currentPage', val);
  emit('page-change', val, pageSize.value);
}

function handleSortChange(sort: { prop: string; order: string }) {
  emit('sort-change', sort);
}

function handleSelectionChange(selection: any[]) {
  emit('selection-change', selection);
}

defineExpose({
  currentPage,
  pageSize,
  reset: () => {
    currentPage.value = 1;
  },
});
</script>

<style scoped>
.data-table {
  .pagination {
    display: flex;
    justify-content: flex-end;
    padding: 16px 0;
  }
}
</style>
