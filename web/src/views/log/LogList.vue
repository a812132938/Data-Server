<template>
  <div class="page-container">
    <PageHeader :breadcrumb="['首页', '调用日志']" title="调用日志" description="查看 API 网关调用日志" />

    <KpiCardRow>
      <KpiCard title="今日调用" :value="overview.todayCount" color="#2878FF" />
      <KpiCard title="成功率" :value="overview.successRate + '%'" color="#52C41A" />
      <KpiCard title="平均延迟" :value="overview.avgCost + 'ms'" color="#FAAD14" />
      <KpiCard title="异常数" :value="overview.errorCount" color="#ff4d4f" />
    </KpiCardRow>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="filters.traceId" placeholder="Trace ID" clearable style="width: 200px" @keyup.enter="handleSearch" />
        <el-select v-model="filters.apiId" placeholder="选择 API" clearable filterable style="width: 180px" @change="handleSearch">
          <el-option v-for="a in apiOptions" :key="a.id" :label="a.name" :value="a.id" />
        </el-select>
        <el-select v-model="filters.appId" placeholder="选择应用" clearable filterable style="width: 180px" @change="handleSearch">
          <el-option v-for="a in appOptions" :key="a.id" :label="a.name" :value="a.id" />
        </el-select>
        <el-select v-model="filters.httpStatus" placeholder="HTTP 状态" clearable style="width: 120px" @change="handleSearch">
          <el-option label="200" value="200" />
          <el-option label="400" value="400" />
          <el-option label="401" value="401" />
          <el-option label="403" value="403" />
          <el-option label="500" value="500" />
        </el-select>
        <el-date-picker v-model="filters.timeRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" style="width: 340px" @change="handleSearch" />
      </div>
      <div class="toolbar-right">
        <el-button @click="fetchData"><el-icon><Refresh /></el-icon></el-button>
      </div>
    </div>

    <el-table :data="records" v-loading="loading" stripe border class="log-table">
      <el-table-column label="Trace ID" width="160" align="center" header-align="center">
        <template #default="{ row }">
          <el-tooltip :content="row.traceId" placement="top">
            <code>{{ truncateStr(row.traceId, 16) }}</code>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="API" min-width="180" align="center" header-align="center">
        <template #default="{ row }">
          <el-tag :type="METHOD_COLORS[row.method] || 'info'" size="small" style="margin-right: 4px">{{ row.method }}</el-tag>
          {{ row.apiName }}
        </template>
      </el-table-column>
      <el-table-column prop="appName" label="应用" width="120" align="center" header-align="center" />
      <el-table-column label="HTTP 状态" width="100" align="center" header-align="center">
        <template #default="{ row }">
          <el-tag :type="row.httpStatus < 400 ? 'success' : row.httpStatus < 500 ? 'warning' : 'danger'" size="small">{{ row.httpStatus }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="耗时" width="90" align="center" header-align="center">
        <template #default="{ row }">{{ row.costMs }}ms</template>
      </el-table-column>
      <el-table-column prop="clientIp" label="IP" width="130" align="center" header-align="center" />
      <el-table-column label="时间" min-width="170" align="center" header-align="center">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="80" align="center" header-align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleDetail(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination background
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </div>

    <LogDetailDrawer v-model="drawerVisible" :log-id="selectedLogId" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import KpiCardRow from '@/components/common/KpiCardRow.vue'
import KpiCard from '@/components/common/KpiCard.vue'
import LogDetailDrawer from './LogDetailDrawer.vue'
import { getLogList } from '@/api/log'
import { getStatsOverview } from '@/api/stats'
import { getApiList } from '@/api/api-definition'
import { getAppList } from '@/api/app'
import { formatTime, truncateStr } from '@/utils/format'
import { METHOD_COLORS } from '@/utils/constants'

const loading = ref(false)
const records = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const overview = ref({ todayCount: 0, successRate: '0', avgCost: 0, errorCount: 0 })
const drawerVisible = ref(false)
const selectedLogId = ref(0)
const apiOptions = ref<any[]>([])
const appOptions = ref<any[]>([])

const filters = reactive({
  traceId: '',
  apiId: null as number | null,
  appId: null as number | null,
  httpStatus: '',
  timeRange: null as any,
})

async function fetchData() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: size.value }
    if (filters.traceId) params.traceId = filters.traceId
    if (filters.apiId) params.apiId = filters.apiId
    if (filters.appId) params.appId = filters.appId
    if (filters.httpStatus) params.httpStatus = Number(filters.httpStatus)
    if (filters.timeRange?.length === 2) {
      params.startTime = filters.timeRange[0]
      params.endTime = filters.timeRange[1]
    }
    const res = await getLogList(params)
    records.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function fetchOverview() {
  const ov = await getStatsOverview()
  overview.value = {
    todayCount: ov.todayCalls ?? 0,
    successRate: String(ov.successRate ?? 0),
    avgCost: ov.avgCost ?? 0,
    errorCount: ov.errorCount ?? 0,
  }
}

async function loadOptions() {
  const [apis, apps] = await Promise.all([
    getApiList({ page: 1, size: 200 }),
    getAppList({ page: 1, size: 200 }),
  ])
  apiOptions.value = apis.records
  appOptions.value = apps.records
}

function handleSearch() { page.value = 1; fetchData() }

function handleDetail(row: any) {
  selectedLogId.value = row.id
  drawerVisible.value = true
}

onMounted(() => { fetchData(); fetchOverview(); loadOptions() })
</script>

<style lang="scss" scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  .toolbar-left { display: flex; gap: 12px; flex-wrap: wrap; }
  .toolbar-right { display: flex; gap: 8px; }
}
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
code { font-family: 'Consolas', monospace; font-size: 14px; color: #4E5969; }

.log-table {
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
