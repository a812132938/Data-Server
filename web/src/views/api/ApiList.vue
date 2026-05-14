<template>
  <div class="api-page-layout">
    <GroupPanel v-model:selectedGroupId="selectedGroupId" @group-changed="onGroupChanged" />
    <div class="api-page-content">
      <PageHeader title="API 定义" :description="headerDesc"
        :breadcrumbs="[{ label: '首页', path: '/' }, { label: 'API 定义' }]" />

      <KpiCardRow>
        <KpiCard :title="selectedGroupId != null ? '本组 API' : 'API 总数'" :value="summary?.total ?? 0" color="#2878FF" />
        <KpiCard title="已发布" :value="summary?.published ?? 0" color="#52C41A" />
        <KpiCard title="草稿" :value="summary?.draft ?? 0" color="#FAAD14" />
        <KpiCard :title="selectedGroupId != null ? '已下线' : '测试中'" :value="selectedGroupId != null ? (summary?.offline ?? 0) : (summary?.testing ?? 0)" :color="selectedGroupId != null ? '#909399' : '#FF4D4F'" />
      </KpiCardRow>

      <div class="toolbar">
        <div class="toolbar-left">
          <el-input v-model="filters.keyword" placeholder="搜索API名称或路径..." clearable style="width: 220px" @keyup.enter="handleSearch" />
          <el-select v-model="filters.status" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
            <el-option v-for="(v, k) in API_STATUS_MAP" :key="k" :label="v.label" :value="k" />
          </el-select>
        </div>
        <div class="toolbar-right">
          <el-button @click="fetchData"><el-icon><Refresh /></el-icon></el-button>
          <el-button type="primary" @click="$router.push('/apis/create')">+ 新建 API</el-button>
        </div>
      </div>

      <el-table :data="records" v-loading="loading" stripe border class="api-table">
        <el-table-column prop="name" label="API名称" min-width="140" align="center" header-align="center">
          <template #default="{ row }">
            <router-link :to="`/apis/${row.id}`" class="link-text">{{ row.name }}</router-link>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径" min-width="200" align="center" header-align="center" show-overflow-tooltip>
          <template #default="{ row }"><code>{{ row.path }}</code></template>
        </el-table-column>
        <el-table-column prop="method" label="方式" width="90" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag size="small" :color="METHOD_COLORS[row.method]" style="color:#fff;border:none">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="datasourceName" label="数据源" min-width="120" align="center" header-align="center" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center" header-align="center">
          <template #default="{ row }"><StatusTag :status="row.status" :status-map="API_STATUS_MAP" /></template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" align="center" header-align="center" />
        <el-table-column prop="ownerName" label="负责人" width="100" align="center" header-align="center" />
        <el-table-column prop="updatedAt" label="更新时间" min-width="160" align="center" header-align="center" />
        <el-table-column label="操作" width="200" align="center" header-align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-if="row.status !== 'DRAFT'" @click="$router.push(`/apis/${row.id}?tab=test`)">测试</el-button>
            <el-button link type="primary" v-if="row.status !== 'OFFLINE'" @click="$router.push(`/apis/create?id=${row.id}`)">编辑</el-button>
            <el-button link type="danger" v-if="['DRAFT','OFFLINE'].includes(row.status)" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next, sizes" :total="total"
          v-model:current-page="page" v-model:page-size="size" :page-sizes="[10,20,50]"
          @current-change="handlePageChange" @size-change="handleSizeChange" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import KpiCard from '@/components/common/KpiCard.vue'
import KpiCardRow from '@/components/common/KpiCardRow.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import GroupPanel from './GroupPanel.vue'
import { useTable } from '@/hooks/useTable'
import { useSummary } from '@/hooks/useSummary'
import { getApiList, getApiSummary, deleteApi } from '@/api/api-definition'
import { API_STATUS_MAP, METHOD_COLORS } from '@/utils/constants'
import type { ApiVO, ApiQuery } from '@/types/api-definition'

const { loading, records, total, page, size, filters, fetchData, handleSearch, handlePageChange, handleSizeChange } =
  useTable<ApiVO, ApiQuery>({ api: getApiList })

const { summary, fetchSummary } = useSummary(getApiSummary)

const selectedGroupId = ref<number | undefined>(undefined)
const groupPanelRef = ref()

const headerDesc = computed(() => {
  if (selectedGroupId.value == null) return '管理数据API的定义与生命周期'
  // 从表格记录中找分组名，或显示通用描述
  const row = records.value.find((r: ApiVO) => r.groupId === selectedGroupId.value)
  if (row) return `当前分组：${row.groupName}`
  return '管理数据API的定义与生命周期'
})

watch(selectedGroupId, (id) => {
  filters.groupId = id
  handleSearch()
})

function onGroupChanged() {
  fetchData()
  fetchSummary()
}

async function handleDelete(row: ApiVO) {
  await ElMessageBox.confirm(`确定删除 API "${row.name}"？`, '确认删除', { type: 'warning' })
  await deleteApi(row.id)
  ElMessage.success('删除成功')
  fetchData()
}
</script>

<style lang="scss" scoped>
.api-page-layout {
  display: flex;
  height: 100%;
  margin: -20px;
  background: #F2F3F5;
}

.api-page-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  min-width: 0;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  .toolbar-left { display: flex; gap: 12px; }
  .toolbar-right { display: flex; gap: 8px; }
}
.link-text { color: #2878FF; font-weight: 500; font-size: 15px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
code { font-family: 'Consolas', monospace; font-size: 14px; color: #4E5969; }

.api-table {
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
