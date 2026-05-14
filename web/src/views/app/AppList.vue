<template>
  <div class="page-container">
    <PageHeader :breadcrumb="['首页', '应用管理']" title="应用管理" description="管理已注册的应用及其凭证" />

    <KpiCardRow>
      <KpiCard title="应用总数" :value="summary.total" color="#2878FF" />
      <KpiCard title="启用中" :value="summary.enabled" color="#52C41A" />
      <KpiCard title="已禁用" :value="summary.disabled" color="#ff4d4f" />
      <KpiCard title="今日调用量" :value="summary.todayCalls ?? 0" color="#FAAD14" />
    </KpiCardRow>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="keyword" placeholder="搜索应用名称或 AppKey..." clearable style="width: 260px" @clear="handleSearch" @keyup.enter="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px" @change="handleSearch">
          <el-option label="启用" value="ACTIVE" />
          <el-option label="禁用" value="DISABLED" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" @click="handleCreate">+ 创建应用</el-button>
      </div>
    </div>

    <el-table :data="records" v-loading="loading" stripe border class="app-table">
      <el-table-column label="应用名称" min-width="140" align="center" header-align="center">
        <template #default="{ row }">
          <el-button link type="primary" class="link-text" @click="goDetail(row.id)">{{ row.name }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="AppKey" min-width="160" align="center" header-align="center" show-overflow-tooltip>
        <template #default="{ row }"><code>{{ maskKey(row.appKey) }}</code></template>
      </el-table-column>
      <el-table-column prop="ownerName" label="负责人" width="100" align="center" header-align="center" />
      <el-table-column prop="authCount" label="授权API数" width="100" align="center" header-align="center" />
      <el-table-column label="今日调用" width="100" align="center" header-align="center">
        <template #default="{ row }">{{ row.todayCalls != null ? row.todayCalls.toLocaleString() : '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center" header-align="center">
        <template #default="{ row }">
          <StatusTag :status="row.status" :status-map="STATUS_MAP" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170" align="center" header-align="center">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center" header-align="center" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="goDetail(row.id)">详情</el-button>
          <el-button link :type="row.status === 'ACTIVE' ? 'danger' : 'success'" size="small" @click="handleToggleStatus(row)">
            {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination background
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, prev, pager, next, sizes"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </div>

    <AppForm v-model="formVisible" :app-id="editingId" @saved="handleSaved" />
    <CredentialDialog v-model="credentialVisible" :credential="credential" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import KpiCardRow from '@/components/common/KpiCardRow.vue'
import KpiCard from '@/components/common/KpiCard.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import AppForm from './AppForm.vue'
import CredentialDialog from './CredentialDialog.vue'
import { getAppList, enableApp, disableApp, getAppSummary } from '@/api/app'
import { formatTime } from '@/utils/format'
import type { AppVO, AppCredential } from '@/types/app'

const STATUS_MAP: Record<string, { label: string; type: string }> = {
  ACTIVE: { label: '启用', type: 'success' },
  DISABLED: { label: '禁用', type: 'danger' },
}

const router = useRouter()
const loading = ref(false)
const records = ref<AppVO[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const statusFilter = ref('')
const summary = ref({ total: 0, enabled: 0, disabled: 0, authCount: 0, todayCalls: 0 })
const formVisible = ref(false)
const editingId = ref(0)
const credentialVisible = ref(false)
const credential = ref<AppCredential | null>(null)

function maskKey(key: string) {
  if (!key) return '-'
  return key.substring(0, 8) + '****' + key.substring(key.length - 4)
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getAppList({ page: page.value, size: size.value, keyword: keyword.value, status: statusFilter.value as any || undefined })
    records.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function fetchSummary() {
  summary.value = await getAppSummary()
}

function handleSearch() { page.value = 1; fetchData() }
function handleCreate() { editingId.value = 0; formVisible.value = true }
function goDetail(id: number) { router.push(`/apps/${id}`) }

async function handleSaved(cred?: AppCredential) {
  formVisible.value = false
  fetchData()
  fetchSummary()
  if (cred) {
    credential.value = cred
    credentialVisible.value = true
  }
}

async function handleToggleStatus(row: AppVO) {
  const action = row.status === 'ACTIVE' ? '禁用' : '启用'
  await ElMessageBox.confirm(`确认${action}应用「${row.name}」？`, '确认', { type: 'warning' })
  if (row.status === 'ACTIVE') {
    await disableApp(row.id)
  } else {
    await enableApp(row.id)
  }
  ElMessage.success(`${action}成功`)
  fetchData()
  fetchSummary()
}

onMounted(() => { fetchData(); fetchSummary() })
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
.link-text { font-weight: 500; font-size: 15px; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
code { font-family: 'Consolas', monospace; font-size: 14px; color: #4E5969; }

.app-table {
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
