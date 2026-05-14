<template>
  <div class="page-container">
    <PageHeader
      :breadcrumbs="[
        { label: '首页', path: '/' },
        { label: '告警管理' },
      ]"
      title="告警管理"
      description="配置告警规则并查看告警事件"
    />

    <!-- Alert Rules Section -->
    <div class="section">
      <div class="toolbar">
        <div class="toolbar-left">
          <h4 class="section-title">告警规则</h4>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" @click="handleCreateRule">+ 新建规则</el-button>
        </div>
      </div>
      <el-table :data="rules" v-loading="rulesLoading" stripe border class="alert-table">
        <el-table-column prop="id" label="ID" width="70" align="center" header-align="center" />
        <el-table-column prop="name" label="规则名称" min-width="140" align="center" header-align="center" show-overflow-tooltip />
        <el-table-column label="目标" width="120" align="center" header-align="center">
          <template #default="{ row }">{{ row.targetName || row.targetType }}</template>
        </el-table-column>
        <el-table-column label="指标" width="120" align="center" header-align="center">
          <template #default="{ row }"><el-tag size="small" type="info">{{ row.metric }}</el-tag></template>
        </el-table-column>
        <el-table-column label="条件" min-width="140" align="center" header-align="center">
          <template #default="{ row }">{{ row.operator }} {{ row.threshold }}{{ row.unit || '' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" header-align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEditRule(row)">编辑</el-button>
            <el-button link :type="row.enabled ? 'danger' : 'success'" size="small" @click="handleToggleRule(row)">
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Alert Events Section -->
    <div class="section">
      <h4 class="section-title" style="margin-bottom: 20px">最近事件</h4>
      <el-table :data="events" v-loading="eventsLoading" stripe border class="alert-table">
        <el-table-column prop="id" label="ID" width="70" align="center" header-align="center" />
        <el-table-column prop="ruleName" label="规则" min-width="140" align="center" header-align="center" show-overflow-tooltip />
        <el-table-column label="指标值" width="100" align="center" header-align="center">
          <template #default="{ row }">{{ row.metricValue }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'FIRING' ? 'danger' : 'success'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="触发时间" min-width="170" align="center" header-align="center">
          <template #default="{ row }">{{ formatTime(row.firedAt) }}</template>
        </el-table-column>
        <el-table-column label="恢复时间" min-width="170" align="center" header-align="center">
          <template #default="{ row }">{{ row.resolvedAt ? formatTime(row.resolvedAt) : '-' }}</template>
        </el-table-column>
        <el-table-column label="通知" width="90" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag :type="row.notified ? 'success' : 'warning'" size="small">{{ row.notified ? '已通知' : '未通知' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <AlertRuleForm v-model="ruleFormVisible" :rule="editingRule" @saved="onRuleSaved" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import AlertRuleForm from './AlertRuleForm.vue'
import { getAlertRules, updateAlertRule, getRecentAlertEvents } from '@/api/alert'
import { formatTime } from '@/utils/format'
import type { AlertRuleVO, AlertEventVO } from '@/types/alert'

const rules = ref<AlertRuleVO[]>([])
const events = ref<AlertEventVO[]>([])
const rulesLoading = ref(false)
const eventsLoading = ref(false)
const ruleFormVisible = ref(false)
const editingRule = ref<AlertRuleVO | null>(null)

async function fetchRules() {
  rulesLoading.value = true
  try {
    rules.value = (await getAlertRules({})).records
  } finally {
    rulesLoading.value = false
  }
}

async function fetchEvents() {
  eventsLoading.value = true
  try {
    const res = await getRecentAlertEvents(20)
    events.value = res.records
  } finally {
    eventsLoading.value = false
  }
}

function handleCreateRule() {
  editingRule.value = null
  ruleFormVisible.value = true
}

function handleEditRule(row: AlertRuleVO) {
  editingRule.value = { ...row }
  ruleFormVisible.value = true
}

async function handleToggleRule(row: AlertRuleVO) {
  const action = row.enabled ? '禁用' : '启用'
  await ElMessageBox.confirm(`确认${action}规则「${row.name}」？`, '确认', { type: 'warning' })
  await updateAlertRule(row.id, { ...row, enabled: !row.enabled })
  ElMessage.success(`${action}成功`)
  fetchRules()
}

function onRuleSaved() {
  ruleFormVisible.value = false
  fetchRules()
}

onMounted(() => { fetchRules(); fetchEvents() })
</script>

<style lang="scss" scoped>
.section { margin-top: 24px; }
.section-title { font-size: 15px; font-weight: 600; color: #1D2129; margin: 0; }

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  .toolbar-left { display: flex; gap: 12px; align-items: center; }
  .toolbar-right { display: flex; gap: 8px; }
}

.alert-table {
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
