<template>
  <div>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="API ID">{{ detail.id }}</el-descriptions-item>
      <el-descriptions-item label="API 名称">{{ detail.name }}</el-descriptions-item>
      <el-descriptions-item label="请求方法">
        <el-tag :type="METHOD_COLORS[detail.method] || 'info'" size="small">{{ detail.method }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="请求路径">{{ detail.path }}</el-descriptions-item>
      <el-descriptions-item label="实际访问地址" :span="2">
        <div class="api-url">
          <el-link type="primary" :href="actualApiUrl" target="_blank" :underline="false">{{ actualApiUrl }}</el-link>
          <el-button link type="primary" size="small" @click="copyActualApiUrl">复制</el-button>
        </div>
      </el-descriptions-item>
      <el-descriptions-item label="分组">{{ detail.groupName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="数据源">{{ detail.datasourceName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建模式">{{ detail.createMode === 'SCRIPT' ? '脚本模式' : '向导模式' }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <StatusTag :status="detail.status" :status-map="API_STATUS_MAP" />
      </el-descriptions-item>
      <el-descriptions-item label="超时">{{ detail.timeoutMs }}ms</el-descriptions-item>
      <el-descriptions-item label="QPS 限制">{{ detail.defaultQpsLimit }}</el-descriptions-item>
      <el-descriptions-item label="缓存">{{ detail.cacheEnable ? `开启(${detail.cacheTtl}s)` : '关闭' }}</el-descriptions-item>
      <el-descriptions-item label="当前版本">{{ detail.version || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建人">{{ detail.createdBy || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ formatTime(detail.createdAt) }}</el-descriptions-item>
      <el-descriptions-item label="描述" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
    </el-descriptions>

    <h4 style="margin-top: 20px">请求参数 ({{ detail.params?.length || 0 }})</h4>
    <el-table :data="detail.params || []" border size="small" style="margin-top: 8px">
      <el-table-column prop="name" label="参数名" width="120" />
      <el-table-column prop="location" label="来源" width="80">
        <template #default="{ row }"><el-tag size="small">{{ row.location }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="dataType" label="类型" width="80" />
      <el-table-column label="必填" width="60">
        <template #default="{ row }">{{ row.required ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column prop="operator" label="操作符" width="80" />
      <el-table-column prop="defaultValue" label="默认值" width="100" />
      <el-table-column prop="example" label="示例" width="100" />
      <el-table-column prop="description" label="描述" />
    </el-table>

    <h4 style="margin-top: 20px">返回字段 ({{ detail.responseFields?.length || 0 }})</h4>
    <el-table :data="detail.responseFields || []" border size="small" style="margin-top: 8px">
      <el-table-column prop="fieldName" label="字段名" width="120" />
      <el-table-column prop="alias" label="别名" width="100" />
      <el-table-column prop="dataType" label="类型" width="80" />
      <el-table-column label="脱敏" width="60">
        <template #default="{ row }">{{ row.sensitive ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column prop="description" label="描述" />
    </el-table>

    <template v-if="detail.sqlTemplate">
      <h4 style="margin-top: 20px">SQL 模板</h4>
      <MonacoEditor :model-value="formattedSql" read-only height="200px" style="margin-top: 8px" />
    </template>

    <div style="margin-top: 24px; display: flex; gap: 12px">
      <el-button type="primary" @click="handleEdit">编辑</el-button>
      <el-button v-if="detail.status === 'PUBLISHED'" type="warning" @click="handleOffline">下线</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { format as formatSql } from 'sql-formatter'
import StatusTag from '@/components/common/StatusTag.vue'
import MonacoEditor from '@/components/editor/MonacoEditor.vue'
import { offlineApi } from '@/api/api-version'
import { API_STATUS_MAP, METHOD_COLORS } from '@/utils/constants'
import { formatTime } from '@/utils/format'
import type { ApiVO } from '@/types/api-definition'

const props = defineProps<{ detail: ApiVO }>()
const emit = defineEmits<{ refresh: [] }>()
const router = useRouter()

const formattedSql = computed(() => {
  if (!props.detail.sqlTemplate) return ''
  try {
    return formatSql(props.detail.sqlTemplate, { language: 'mysql', tabWidth: 2 })
  } catch {
    return props.detail.sqlTemplate
  }
})

const actualApiUrl = computed(() => {
  const gatewayBaseUrl = getGatewayBaseUrl()
  return joinUrl(gatewayBaseUrl, getGatewayPath(props.detail.path, gatewayBaseUrl))
})

function getGatewayBaseUrl() {
  const gatewayBaseUrl = String(import.meta.env.VITE_GATEWAY_BASE_URL || '').trim()
  if (gatewayBaseUrl) return gatewayBaseUrl
  const apiBaseUrl = String(import.meta.env.VITE_API_BASE_URL || '').trim()
  if (apiBaseUrl) return apiBaseUrl
  return window.location.origin
}

function getGatewayPath(path: string, baseUrl: string) {
  const cleanPath = String(path || '').trim().replace(/^\/+/, '')
  if (!cleanPath) return '/gateway'
  if (baseUrlHasGatewayPrefix(baseUrl)) {
    return `/${cleanPath.replace(/^gateway\/?/i, '')}`
  }
  return cleanPath.startsWith('gateway/') ? `/${cleanPath}` : `/gateway/${cleanPath}`
}

function baseUrlHasGatewayPrefix(baseUrl: string) {
  try {
    return new URL(baseUrl).pathname.replace(/\/+$/, '').endsWith('/gateway')
  } catch {
    return baseUrl.replace(/\/+$/, '').endsWith('/gateway')
  }
}

function joinUrl(baseUrl: string, path: string) {
  return `${baseUrl.replace(/\/+$/, '')}/${path.replace(/^\/+/, '')}`
}

async function copyActualApiUrl() {
  if (navigator.clipboard?.writeText) {
    await navigator.clipboard.writeText(actualApiUrl.value)
  } else {
    const textarea = document.createElement('textarea')
    textarea.value = actualApiUrl.value
    textarea.style.position = 'fixed'
    textarea.style.left = '-9999px'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
  }
  ElMessage.success('访问地址已复制')
}

function handleEdit() {
  router.push({ path: '/apis/create', query: { id: props.detail.id } })
}

async function handleOffline() {
  await ElMessageBox.confirm('确认下线该 API？下线后将无法通过网关访问。', '下线确认', { type: 'warning' })
  await offlineApi(props.detail.id)
  ElMessage.success('下线成功')
  emit('refresh')
}
</script>

<style scoped>
.api-url {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.api-url :deep(.el-link__inner) {
  word-break: break-all;
}
</style>
