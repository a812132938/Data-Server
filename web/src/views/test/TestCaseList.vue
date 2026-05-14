<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px">
      <h4>测试用例</h4>
      <div style="display: flex; gap: 8px">
        <el-button size="small" @click="handleSaveCase">+ 保存为用例</el-button>
        <el-button size="small" type="primary" :loading="batchRunning" @click="handleBatchRun">批量执行</el-button>
      </div>
    </div>

    <el-table :data="cases" border size="small">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="用例名称" width="140" />
      <el-table-column label="参数摘要" min-width="160">
        <template #default="{ row }">
          <span class="text-ellipsis">{{ formatParamSummary(row.inputParams) }}</span>
        </template>
      </el-table-column>
      <!-- 断言一期不做，字段留存不评估 -->
      <el-table-column label="启用" width="70">
        <template #default="{ row }"><el-switch v-model="row.enabled" size="small" @change="handleToggle(row)" /></template>
      </el-table-column>
      <el-table-column label="最近状态" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.lastRunStatus" :type="row.lastRunStatus === 'SUCCESS' ? 'success' : 'danger'" size="small">{{ row.lastRunStatus }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row, $index }">
          <el-button link type="primary" size="small" @click="handleRunSingle(row)">执行</el-button>
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row, $index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="margin-top: 12px; display: flex; justify-content: flex-end">
      <el-pagination background layout="total, prev, pager, next" :total="total"
        v-model:current-page="casePage" v-model:page-size="caseSize"
        @current-change="fetchCases" />
    </div>

    <!-- Save Case Dialog -->
    <el-dialog v-model="saveVisible" title="保存测试用例" width="480px">
      <el-form :model="caseForm" label-width="80px">
        <el-form-item label="用例名称"><el-input v-model="caseForm.name" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="saveVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- Batch Result Dialog -->
    <el-dialog v-model="batchResultVisible" title="批量执行结果" width="500px">
      <el-table :data="batchResults" border size="small">
        <el-table-column prop="caseId" label="用例ID" width="80" />
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costMs" label="耗时(ms)" width="100" />
        <el-table-column prop="errorMsg" label="错误信息" />
      </el-table>
      <div style="margin-top: 12px; text-align: center">
        <el-tag :type="batchAllPassed ? 'success' : 'danger'" size="large">
          {{ batchAllPassed ? '全部通过 ✓' : '存在失败项' }}
        </el-tag>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { runTest, getTestCases, createTestCase, updateTestCase, deleteTestCase, runAllTestCases } from '@/api/api-test'
import type { TestCaseVO } from '@/types/api-definition'

const props = defineProps<{ apiId: number; currentParams: Record<string, string> }>()

const cases = ref<any[]>([])
const casePage = ref(1)
const caseSize = ref(10)
const total = ref(0)
const saveVisible = ref(false)
const batchResultVisible = ref(false)
const batchRunning = ref(false)
const batchResults = ref<any[]>([])
const batchAllPassed = computed(() => batchResults.value.every(r => r.status === 'SUCCESS'))

const caseForm = ref({ name: '', id: 0 })

onMounted(fetchCases)

async function fetchCases() {
  try {
    const res = await getTestCases(props.apiId, { page: casePage.value, size: caseSize.value })
    cases.value = res.records ?? []
    total.value = res.total ?? 0
  } catch { /* empty */ }
}

function formatParamSummary(params: string | Record<string, string>) {
  if (!params) return '-'
  const obj = typeof params === 'string' ? JSON.parse(params) : params
  const entries = Object.entries(obj)
  if (!entries.length) return '-'
  return entries.map(([k, v]) => `${k}=${v}`).join(', ')
}


function handleSaveCase() {
  caseForm.value = { name: '', id: 0 }
  saveVisible.value = true
}

async function confirmSave() {
  if (!caseForm.value.name) {
    ElMessage.warning('请输入用例名称')
    return
  }
  const payload = {
    name: caseForm.value.name,
    inputParams: props.currentParams,
    assertions: [],
    enabled: true,
  }
  if (caseForm.value.id) {
    await updateTestCase(caseForm.value.id, payload)
  } else {
    await createTestCase(props.apiId, payload)
  }
  ElMessage.success('保存成功')
  saveVisible.value = false
  fetchCases()
}

function handleEdit(row: any) {
  caseForm.value = { name: row.name, id: row.id }
  saveVisible.value = true
}

async function handleDelete(row: any, index: number) {
  await ElMessageBox.confirm('确认删除该用例？', '提示', { type: 'warning' })
  await deleteTestCase(row.id)
  cases.value.splice(index, 1)
  ElMessage.success('已删除')
}

async function handleRunSingle(row: any) {
  ElMessage.info(`正在执行用例: ${row.name}`)
  try {
    const params = typeof row.inputParams === 'string' ? JSON.parse(row.inputParams) : row.inputParams
    const res = await runTest(props.apiId, { params })
    const passed = res.bizCode === 0
    row.lastRunStatus = passed ? 'SUCCESS' : 'FAIL'
    ElMessage[passed ? 'success' : 'error'](passed ? '通过' : '失败')
  } catch {
    ElMessage.error('执行失败')
  }
}

async function handleToggle(row: any) {
  await updateTestCase(row.id, { name: row.name, inputParams: row.inputParams, assertions: row.assertions ?? null, enabled: row.enabled })
}

async function handleBatchRun() {
  batchRunning.value = true
  try {
    const res = await runAllTestCases(props.apiId)
    batchResults.value = res.results ?? []
    batchResultVisible.value = true
    fetchCases()
  } catch {
    ElMessage.error('批量执行失败')
  } finally {
    batchRunning.value = false
  }
}
</script>

<style scoped>
.text-ellipsis {
  display: inline-block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
