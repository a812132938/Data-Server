<template>
  <div v-if="detail" class="page-container">
    <PageHeader :breadcrumb="['首页', '应用管理', detail.name]" title="应用管理" :description="detail.name" />

    <!-- App info card -->
    <div class="info-card">
      <div class="info-card-left">
        <span class="info-card-name">{{ detail.name }}</span>
        <StatusTag :status="detail.status" :status-map="STATUS_MAP" />
        <span class="info-card-meta">AppKey: {{ maskKey(detail.appKey) }}</span>
        <span class="info-card-meta">负责人: {{ detail.ownerName || detail.owner }}</span>
        <span class="info-card-meta">创建时间: {{ formatTime(detail.createdAt) }}</span>
      </div>
      <div class="info-card-right">
        <el-button @click="handleEdit">编辑</el-button>
        <el-button :type="detail.status === 'ACTIVE' ? 'danger' : 'success'" @click="handleToggle">
          {{ detail.status === 'ACTIVE' ? '禁用' : '启用' }}
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" style="margin-top: 16px">
      <el-tab-pane label="基本信息" name="info">
        <el-descriptions :column="2" border class="detail-desc">
          <el-descriptions-item label="应用 ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="应用名称">{{ detail.name }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ detail.ownerName || detail.owner }}</el-descriptions-item>
          <el-descriptions-item label="AppKey">{{ maskKey(detail.appKey) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <StatusTag :status="detail.status" :status-map="STATUS_MAP" />
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(detail.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detail.updatedAt ? formatTime(detail.updatedAt) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detail.description || detail.purpose || '-' }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 16px; display: flex; gap: 12px">
          <el-button type="warning" @click="handleRotate">轮换密钥</el-button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="已授权 API" name="auths">
        <div class="toolbar">
          <div class="toolbar-left" />
          <div class="toolbar-right">
            <el-button type="primary" @click="authDialogVisible = true">+ 申请授权</el-button>
          </div>
        </div>
        <el-table :data="auths" v-loading="authLoading" stripe border class="app-table">
          <el-table-column prop="apiName" label="API 名称" min-width="140" align="center" header-align="center" />
          <el-table-column prop="apiPath" label="请求路径" min-width="160" align="center" header-align="center" show-overflow-tooltip>
            <template #default="{ row }"><code>{{ row.apiPath || row.path }}</code></template>
          </el-table-column>
          <el-table-column prop="qpsLimit" label="QPS 限制" width="100" align="center" header-align="center" />
          <el-table-column label="有效期" min-width="170" align="center" header-align="center">
            <template #default="{ row }">{{ row.expireAt ? formatTime(row.expireAt) : '永久' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center" header-align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'EFFECTIVE' ? 'success' : 'info'" size="small">
                {{ row.status === 'EFFECTIVE' ? '生效' : row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" header-align="center" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.status === 'EFFECTIVE'" link type="danger" size="small" @click="handleRevoke(row)">撤销</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination background layout="total, prev, pager, next" :total="authTotal"
            v-model:current-page="authPage" v-model:page-size="authSize"
            @current-change="fetchAuths" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <AppForm v-model="formVisible" :app-id="detail.id" @saved="onFormSaved" />
    <CredentialDialog v-model="credVisible" :credential="credData" />
    <AuthApplyDialog v-model="authDialogVisible" :app-id="detail.id" @applied="fetchAuths" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import AppForm from './AppForm.vue'
import CredentialDialog from './CredentialDialog.vue'
import AuthApplyDialog from './AuthApplyDialog.vue'
import { getApp, rotateSecret, enableApp, disableApp, getAppAuths } from '@/api/app'
import { revokeAuth } from '@/api/auth'
import { formatTime } from '@/utils/format'
import type { AppVO, AppCredential, AppAuthVO } from '@/types/app'

const STATUS_MAP: Record<string, { label: string; type: string }> = {
  ACTIVE: { label: '启用', type: 'success' },
  DISABLED: { label: '禁用', type: 'danger' },
}

const route = useRoute()
const id = Number(route.params.id)
const activeTab = ref('info')
const detail = ref<AppVO | null>(null)
const auths = ref<AppAuthVO[]>([])
const authLoading = ref(false)
const authPage = ref(1)
const authSize = ref(10)
const authTotal = ref(0)
const formVisible = ref(false)
const credVisible = ref(false)
const credData = ref<AppCredential | null>(null)
const authDialogVisible = ref(false)

function maskKey(key: string) {
  if (!key) return '-'
  return key.substring(0, 8) + '****' + key.substring(key.length - 4)
}

async function fetchDetail() {
  detail.value = await getApp(id)
}

async function fetchAuths() {
  authLoading.value = true
  try {
    const res = await getAppAuths(id, { page: authPage.value, size: authSize.value })
    auths.value = res.records
    authTotal.value = res.total ?? 0
  } finally {
    authLoading.value = false
  }
}

function handleEdit() { formVisible.value = true }

async function onFormSaved() {
  formVisible.value = false
  fetchDetail()
}

async function handleRotate() {
  await ElMessageBox.confirm('轮换密钥后旧密钥将立即失效，确认？', '轮换密钥', { type: 'warning' })
  const res = await rotateSecret(id)
  credData.value = { appKey: res.appKey, appSecret: res.appSecret, appCode: detail.value!.appCode } as any
  credVisible.value = true
}

async function handleToggle() {
  const action = detail.value?.status === 'ACTIVE' ? '禁用' : '启用'
  await ElMessageBox.confirm(`确认${action}？`, '确认', { type: 'warning' })
  if (detail.value?.status === 'ACTIVE') {
    await disableApp(id)
  } else {
    await enableApp(id)
  }
  ElMessage.success(`${action}成功`)
  fetchDetail()
}

async function handleRevoke(row: AppAuthVO) {
  await ElMessageBox.confirm('确认撤销该授权？', '撤销授权', { type: 'warning' })
  await revokeAuth(row.id)
  ElMessage.success('已撤销')
  fetchAuths()
}

onMounted(() => { fetchDetail(); fetchAuths() })
</script>

<style lang="scss" scoped>
.info-card {
  margin-top: 16px;
  padding: 16px 20px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.info-card-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.info-card-name { font-size: 18px; font-weight: 600; color: #1D2129; }
.info-card-meta { color: #909399; font-size: 14px; }
.info-card-right { display: flex; gap: 8px; }

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  .toolbar-left { display: flex; gap: 12px; }
  .toolbar-right { display: flex; gap: 8px; }
}
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
code { font-family: 'Consolas', monospace; font-size: 14px; color: #4E5969; }

.detail-desc {
  :deep(.el-descriptions__label) { font-size: 15px; font-weight: 600; color: #1D2129; }
  :deep(.el-descriptions__content) { font-size: 15px; }
}

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
