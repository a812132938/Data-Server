<template>
  <div class="page-container">
    <PageHeader :breadcrumb="['首页', '授权审批']" title="授权审批" description="审批应用的 API 授权申请" />

    <KpiCardRow>
      <KpiCard title="待审批" :value="summary.pending" color="#FAAD14" />
      <KpiCard title="已通过" :value="summary.approved" color="#52C41A" />
      <KpiCard title="已驳回" :value="summary.rejected" color="#ff4d4f" />
    </KpiCardRow>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 140px" @change="handleSearch">
          <el-option label="待审批" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已驳回" value="REJECTED" />
        </el-select>
      </div>
      <div class="toolbar-right">
        <el-button @click="fetchData"><el-icon><Refresh /></el-icon></el-button>
      </div>
    </div>

    <el-table :data="records" v-loading="loading" stripe border class="approval-table">
      <el-table-column prop="id" label="ID" width="70" align="center" header-align="center" />
      <el-table-column prop="applicant" label="申请人" width="100" align="center" header-align="center" />
      <el-table-column label="应用" min-width="120" align="center" header-align="center">
        <template #default="{ row }">
          <el-button link type="primary" class="link-text" @click="$router.push(`/apps/${row.appId}`)">{{ row.appName }}</el-button>
        </template>
      </el-table-column>
      <el-table-column label="API" min-width="140" align="center" header-align="center">
        <template #default="{ row }">
          <el-button link type="primary" class="link-text" @click="$router.push(`/apis/${row.apiId}`)">{{ row.apiName }}</el-button>
        </template>
      </el-table-column>
      <el-table-column prop="qpsLimit" label="QPS" width="80" align="center" header-align="center" />
      <el-table-column label="有效期" min-width="140" align="center" header-align="center">
        <template #default="{ row }">{{ row.expireAt ? formatTime(row.expireAt) : '永久' }}</template>
      </el-table-column>
      <el-table-column label="申请时间" min-width="170" align="center" header-align="center">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center" header-align="center">
        <template #default="{ row }">
          <StatusTag :status="row.status" :status-map="APPROVAL_STATUS_MAP" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center" header-align="center" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'PENDING'">
            <el-button link type="primary" size="small" @click="handleApprove(row)">通过</el-button>
            <el-button link type="danger" size="small" @click="handleReject(row)">驳回</el-button>
          </template>
          <span v-else style="color: #999">-</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination background
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="fetchData"
        @size-change="fetchData"
      />
    </div>

    <!-- Approve Dialog -->
    <el-dialog v-model="approveVisible" title="审批通过" width="420px">
      <el-form label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="actionComment" type="textarea" :rows="2" placeholder="可选填审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取消</el-button>
        <el-button type="primary" :loading="acting" @click="confirmApprove">确认通过</el-button>
      </template>
    </el-dialog>

    <!-- Reject Dialog -->
    <el-dialog v-model="rejectVisible" title="驳回申请" width="420px">
      <el-form label-width="80px">
        <el-form-item label="驳回理由">
          <el-input v-model="actionComment" type="textarea" :rows="2" placeholder="请填写驳回理由（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="acting" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import KpiCardRow from '@/components/common/KpiCardRow.vue'
import KpiCard from '@/components/common/KpiCard.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { getApprovalList, approveApproval, rejectApproval, getApprovalSummary } from '@/api/approval'
import { APPROVAL_STATUS_MAP } from '@/utils/constants'
import { formatTime } from '@/utils/format'

const loading = ref(false)
const records = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const statusFilter = ref('')
const summary = ref({ pending: 0, approved: 0, rejected: 0 })

const approveVisible = ref(false)
const rejectVisible = ref(false)
const acting = ref(false)
const actionComment = ref('')
const currentRow = ref<any>(null)

async function fetchData() {
  loading.value = true
  try {
    const res = await getApprovalList({ page: page.value, size: size.value, status: statusFilter.value || undefined })
    records.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function fetchSummary() {
  summary.value = await getApprovalSummary()
}

function handleSearch() { page.value = 1; fetchData() }

function handleApprove(row: any) {
  currentRow.value = row
  actionComment.value = ''
  approveVisible.value = true
}

function handleReject(row: any) {
  currentRow.value = row
  actionComment.value = ''
  rejectVisible.value = true
}

async function confirmApprove() {
  acting.value = true
  try {
    await approveApproval(currentRow.value.id, { comment: actionComment.value })
    ElMessage.success('审批通过')
    approveVisible.value = false
    fetchData()
    fetchSummary()
  } finally {
    acting.value = false
  }
}

async function confirmReject() {
  if (!actionComment.value) {
    ElMessage.warning('请填写驳回理由')
    return
  }
  acting.value = true
  try {
    await rejectApproval(currentRow.value.id, { comment: actionComment.value })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    fetchData()
    fetchSummary()
  } finally {
    acting.value = false
  }
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

.approval-table {
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
