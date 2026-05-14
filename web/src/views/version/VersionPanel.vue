<template>
  <div>
    <!-- Current Version Card -->
    <el-card shadow="never">
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div>
          <span style="font-size: 16px; font-weight: 600">当前版本: </span>
          <el-tag :type="effectiveVersion ? 'success' : 'info'" size="large">{{ effectiveVersion?.versionNo || '未发布' }}</el-tag>
          <span v-if="effectiveVersion?.publishedByName" style="margin-left: 16px; color: #999">
            发布人: {{ effectiveVersion.publishedByName }} | {{ formatTime(effectiveVersion.publishedAt) }}
          </span>
        </div>
        <el-button type="primary" @click="handlePublish" :disabled="detail.status === 'DRAFT'">
          发布新版本
        </el-button>
      </div>
    </el-card>

    <!-- Version History -->
    <h4 style="margin-top: 20px">版本历史</h4>
    <el-table :data="versions" border size="small" style="margin-top: 8px" v-loading="loading" :cell-style="versionCellStyle">
      <el-table-column prop="versionNo" label="版本号" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isCurrent ? 'success' : 'info'" size="small">
            {{ row.isCurrent ? '生效中' : '历史' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="参数摘要" min-width="260">
        <template #default="{ row }">
          <template v-if="row.paramSummary?.length">
            <el-tag v-for="p in row.paramSummary" :key="p.name" size="small" style="margin-right: 4px">
              {{ p.name }}({{ p.dataType }}, {{ p.operator }}){{ p.required ? ' *' : '' }}
            </el-tag>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="参数/字段" width="100">
        <template #default="{ row }">
          <span>{{ row.paramCount ?? '-' }} / {{ row.responseFieldCount ?? '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="超时" width="80">
        <template #default="{ row }">{{ row.timeoutMs != null ? row.timeoutMs + 'ms' : '-' }}</template>
      </el-table-column>
      <el-table-column label="缓存" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.cacheEnable" size="small" type="success">{{ row.cacheTtl }}s</el-tag>
          <span v-else>关闭</span>
        </template>
      </el-table-column>
      <el-table-column label="QPS" width="70">
        <template #default="{ row }">{{ row.defaultQpsLimit ?? '-' }}</template>
      </el-table-column>
      <el-table-column prop="changeLog" label="变更说明" min-width="160" show-overflow-tooltip />
      <el-table-column prop="publishedByName" label="发布人" width="100" />
      <el-table-column label="发布时间" width="170">
        <template #default="{ row }">{{ formatTime(row.publishedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleViewSnapshot(row)">查看</el-button>
          <el-button v-if="row.status === 'HISTORY'" link type="warning" size="small" @click="handleRollback(row)">回滚</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Publish Dialog -->
    <el-dialog v-model="publishVisible" title="发布新版本" width="480px">
      <el-form label-width="80px">
        <el-form-item label="变更说明">
          <el-input v-model="changeLog" type="textarea" :rows="3" placeholder="请输入本次发布的变更说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="confirmPublish">确认发布</el-button>
      </template>
    </el-dialog>

    <!-- Rollback Dialog -->
    <el-dialog v-model="rollbackVisible" title="版本回滚" width="480px">
      <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 16px">
        确认回滚到版本 {{ rollbackTarget?.version }}？此操作将替换当前生效版本。
      </el-alert>
      <el-form label-width="80px">
        <el-form-item label="回滚原因">
          <el-input v-model="rollbackReason" type="textarea" :rows="2" placeholder="请输入回滚原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rollbackVisible = false">取消</el-button>
        <el-button type="warning" :loading="rollingBack" @click="confirmRollback">确认回滚</el-button>
      </template>
    </el-dialog>

    <!-- Snapshot Drawer -->
    <el-drawer v-model="snapshotVisible" title="版本快照" size="600px">
      <template v-if="snapshotData">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="版本号">{{ snapshotData.versionNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ versionStatusLabel(snapshotData.status) }}</el-descriptions-item>
          <el-descriptions-item label="发布人">{{ snapshotData.publishedByName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ formatTime(snapshotData.publishedAt) }}</el-descriptions-item>
          <el-descriptions-item label="参数摘要" :span="2">
            <template v-if="snapshotData.paramSummary?.length">
              <el-tag v-for="p in snapshotData.paramSummary" :key="p.name" size="small" style="margin-right: 6px; margin-bottom: 4px">
                {{ p.name }}({{ p.dataType }}, {{ p.operator }}){{ p.required ? ' *' : '' }}
              </el-tag>
            </template>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="变更说明" :span="2">{{ snapshotData.changeLog || '-' }}</el-descriptions-item>
          <el-descriptions-item label="参数数量">{{ snapshotData.paramCount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="响应字段数">{{ snapshotData.responseFieldCount ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="超时时间">{{ snapshotData.timeoutMs != null ? snapshotData.timeoutMs + 'ms' : '-' }}</el-descriptions-item>
          <el-descriptions-item label="QPS 限制">{{ snapshotData.defaultQpsLimit ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="缓存">{{ snapshotData.cacheEnable ? `开启(${snapshotData.cacheTtl}s)` : '关闭' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin-top: 16px">SQL 模板</h4>
        <MonacoEditor
          v-if="snapshotData.sqlTemplate"
          :model-value="formatSql(snapshotData.sqlTemplate)"
          read-only
          language="sql"
          height="200px"
          style="margin-top: 8px"
        />
        <span v-else style="color: #999; margin-top: 8px; display: inline-block">-</span>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { format as sqlFormat } from 'sql-formatter'
import MonacoEditor from '@/components/editor/MonacoEditor.vue'
import { publishApi, rollbackApi, getVersions } from '@/api/api-version'
import { formatTime } from '@/utils/format'
import type { ApiVO, ApiVersionVO } from '@/types/api-definition'

const props = defineProps<{ apiId: number; detail: ApiVO }>()
const emit = defineEmits<{ refresh: [] }>()

const versions = ref<ApiVersionVO[]>([])
const loading = ref(false)
const publishVisible = ref(false)
const publishing = ref(false)
const changeLog = ref('')
const rollbackVisible = ref(false)
const rollingBack = ref(false)
const rollbackTarget = ref<ApiVersionVO | null>(null)
const rollbackReason = ref('')
const snapshotVisible = ref(false)
const snapshotData = ref<ApiVersionVO | null>(null)

function isEffective(status: string) {
  return status === 'EFFECTIVE' || status === 'PUBLISHED'
}

const effectiveVersion = computed(() => versions.value.find(v => v.isCurrent) ?? null)

const VERSION_STATUS_LABELS: Record<string, string> = {
  PUBLISHED: '生效中',
  EFFECTIVE: '生效中',
  HISTORY: '历史',
}
function versionStatusLabel(status: string) {
  return VERSION_STATUS_LABELS[status] || status
}

onMounted(fetchVersions)

async function fetchVersions() {
  loading.value = true
  try {
    const res = await getVersions(props.apiId)
    versions.value = Array.isArray(res) ? res : res.records ?? []
  } finally {
    loading.value = false
  }
}

function handlePublish() {
  changeLog.value = ''
  publishVisible.value = true
}

async function confirmPublish() {
  publishing.value = true
  try {
    await publishApi(props.apiId, { changeLog: changeLog.value })
    ElMessage.success('发布成功')
    publishVisible.value = false
    emit('refresh')
    await fetchVersions()
  } finally {
    publishing.value = false
  }
}

function handleRollback(row: ApiVersionVO) {
  rollbackTarget.value = row
  rollbackReason.value = ''
  rollbackVisible.value = true
}

async function confirmRollback() {
  if (!rollbackTarget.value) return
  rollingBack.value = true
  try {
    await rollbackApi(props.apiId, { versionId: rollbackTarget.value.id, reason: rollbackReason.value })
    ElMessage.success('回滚成功')
    rollbackVisible.value = false
    emit('refresh')
    await fetchVersions()
  } finally {
    rollingBack.value = false
  }
}

function formatSql(sql: string): string {
  if (!sql) return sql
  try {
    return sqlFormat(sql, { language: 'mysql', tabWidth: 2 })
  } catch {
    return sql
  }
}

function versionCellStyle({ row }: { row: ApiVersionVO }) {
  return row.isCurrent ? { backgroundColor: '#f0f9eb' } : {}
}

function handleViewSnapshot(row: ApiVersionVO) {
  snapshotData.value = row
  snapshotVisible.value = true
}
</script>

