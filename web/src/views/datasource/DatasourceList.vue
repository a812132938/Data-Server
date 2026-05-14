<template>
  <div class="page-container">
    <PageHeader title="数据源管理" description="管理平台数据源连接配置"
      :breadcrumbs="[{ label: '首页', path: '/' }, { label: '数据源管理' }]" />

    <KpiCardRow>
      <KpiCard title="数据源总数" :value="summary?.total ?? 0" color="#2878FF" />
      <KpiCard title="启用中" :value="summary?.enabled ?? 0" color="#52C41A" />
      <KpiCard title="已禁用" :value="summary?.disabled ?? 0" color="#909399" />
      <KpiCard title="被引用API" :value="summary?.referencedApiCount ?? 0" color="#FAAD14" />
    </KpiCardRow>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="filters.keyword" placeholder="搜索数据源名称" clearable style="width: 220px" @clear="handleSearch" @keyup.enter="handleSearch" />
        <el-select v-model="filters.type" placeholder="类型" clearable style="width: 140px" @change="handleSearch">
          <el-option v-for="t in DATASOURCE_TYPES" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
        <el-select v-model="filters.status" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
          <el-option label="启用" value="ENABLED" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button @click="fetchData">
          <el-icon><Refresh /></el-icon>
        </el-button>
        <el-button type="primary" @click="openForm()">+ 新增数据源</el-button>
      </div>
    </div>

    <el-table :data="records" v-loading="loading" stripe border class="ds-table" style="width: 100%">
      <el-table-column prop="id" label="ID" width="70" align="center" header-align="center" />
      <el-table-column prop="name" label="名称" min-width="150" align="center" header-align="center" show-overflow-tooltip>
        <template #default="{ row }"><span class="name-text">{{ row.name }}</span></template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="120" align="center" header-align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="typeTagMap[row.type] || 'info'">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="地址" min-width="180" align="center" header-align="center">
        <template #default="{ row }"><code>{{ row.host }}:{{ row.port }}</code></template>
      </el-table-column>
      <el-table-column prop="databaseName" label="数据库" min-width="120" align="center" header-align="center" show-overflow-tooltip />
      <el-table-column label="状态" width="120" align="center" header-align="center">
        <template #default="{ row }">
          <StatusSwitch :model-value="row.status === 'ENABLED'" @change="(val: boolean) => handleToggleStatus(row, val)" />
        </template>
      </el-table-column>
      <el-table-column prop="referencedApiCount" label="引用API" width="90" align="center" header-align="center" />
      <el-table-column prop="createdByName" label="创建人" width="100" align="center" header-align="center" />
      <el-table-column label="创建时间" min-width="170" align="center" header-align="center">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" header-align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleTest(row.id)">测试</el-button>
          <el-button link type="primary" @click="openForm(row.id)">编辑</el-button>
          <el-button link type="primary" @click="openSchema(row)">Schema</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
        v-model:current-page="page" v-model:page-size="size" :page-sizes="[10, 20, 50]"
        @current-change="handlePageChange" @size-change="handleSizeChange" />
    </div>

    <DatasourceForm v-model:visible="formVisible" :datasource-id="editingId" @success="onFormSuccess" />
    <SchemaDrawer v-model:visible="schemaVisible" :datasource="schemaTarget" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import KpiCard from '@/components/common/KpiCard.vue'
import KpiCardRow from '@/components/common/KpiCardRow.vue'
import StatusSwitch from '@/components/common/StatusSwitch.vue'
import DatasourceForm from './DatasourceForm.vue'
import SchemaDrawer from './SchemaDrawer.vue'
import { useTable } from '@/hooks/useTable'
import { useSummary } from '@/hooks/useSummary'
import { getDatasourceList, getDatasourceSummary, testExistingConnection, enableDatasource, disableDatasource, deleteDatasource } from '@/api/datasource'
import { DATASOURCE_TYPES } from '@/utils/constants'
import { formatTime } from '@/utils/format'
import type { DatasourceVO, DatasourceQuery } from '@/types/datasource'

const typeTagMap: Record<string, string> = { MYSQL: '', POSTGRES: 'success', ORACLE: 'warning', CLICKHOUSE: 'danger' }

const { loading, records, total, page, size, filters, fetchData, handleSearch, handlePageChange, handleSizeChange } =
  useTable<DatasourceVO, DatasourceQuery>({ api: getDatasourceList })

const { summary, fetchSummary } = useSummary(getDatasourceSummary)

const formVisible = ref(false)
const editingId = ref<number>()
const schemaVisible = ref(false)
const schemaTarget = ref<{ id: number; name: string }>({ id: 0, name: '' })

function openForm(id?: number) {
  editingId.value = id
  formVisible.value = true
}

function onFormSuccess() {
  fetchData()
  fetchSummary()
}

function openSchema(row: DatasourceVO) {
  schemaTarget.value = { id: row.id, name: row.name }
  schemaVisible.value = true
}

async function handleTest(id: number) {
  try {
    const res = await testExistingConnection(id)
    if (res.success) {
      ElMessage.success(`连接成功 (${res.costMs}ms)`)
    } else {
      ElMessage.error(`连接失败: ${res.message}`)
    }
  } catch {}
}

async function handleToggleStatus(row: DatasourceVO, enable: boolean) {
  try {
    if (enable) {
      await enableDatasource(row.id)
    } else {
      await disableDatasource(row.id)
    }
    fetchData()
    fetchSummary()
  } catch {}
}

async function handleDelete(row: DatasourceVO) {
  if (row.referencedApiCount > 0) {
    ElMessage.warning('该数据源被 API 引用，无法删除')
    return
  }
  await ElMessageBox.confirm('确定删除该数据源？', '确认删除', { type: 'warning' })
  await deleteDatasource(row.id)
  ElMessage.success('删除成功')
  fetchData()
  fetchSummary()
}
</script>

<style lang="scss" scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  .toolbar-left { display: flex; gap: 12px; }
  .toolbar-right { display: flex; gap: 8px; }
}
.name-text { font-weight: 500; font-size: 15px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
code { font-family: 'Consolas', monospace; font-size: 14px; color: #4E5969; }

.ds-table {
  font-size: 15px;

  :deep(.el-table__header) {
    font-size: 15px;
    th {
      font-weight: 600;
      color: #1D2129;
    }
  }

  :deep(.el-table__body) {
    font-size: 15px;
  }

  :deep(.el-button) {
    font-size: 14px;
  }
}
</style>
