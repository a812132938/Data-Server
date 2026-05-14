<template>
  <div class="test-panel">
    <div class="test-panel__left">
      <h4>请求参数</h4>
      <el-form label-position="top" style="margin-top: 12px">
        <el-form-item v-for="p in params" :key="p.name" :label="p.name">
          <template #label>
            <span>{{ p.name }}</span>
            <el-tag size="small" type="info" style="margin-left: 8px">{{ p.dataType }}</el-tag>
            <el-tag v-if="p.required" size="small" type="danger" style="margin-left: 4px">必填</el-tag>
          </template>
          <el-input v-model="paramValues[p.name]" :placeholder="p.example || p.defaultValue || ''" />
        </el-form-item>
      </el-form>
      <el-button type="primary" :loading="running" @click="handleRun" style="margin-top: 12px">
        <el-icon><VideoPlay /></el-icon> 执行测试
      </el-button>
    </div>
    <div class="test-panel__right">
      <h4>响应</h4>
      <div v-if="result" class="test-response-bar" style="margin-top: 12px">
        <el-tag :type="result.httpStatus < 400 ? 'success' : 'danger'" size="large">
          {{ result.httpStatus }} {{ result.httpStatus < 400 ? 'OK' : 'Error' }}
        </el-tag>
        <span style="margin-left: 12px; color: #999">耗时: {{ result.costMs }}ms</span>
      </div>
      <MonacoEditor
        v-if="responseBody"
        :model-value="responseBody"
        read-only
        language="json"
        height="400px"
        style="margin-top: 12px"
      />
      <el-empty v-else description="执行测试查看响应" style="margin-top: 40px" />

      <el-divider />
      <TestCaseList :api-id="apiId" :current-params="paramValues" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { VideoPlay } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import MonacoEditor from '@/components/editor/MonacoEditor.vue'
import TestCaseList from './TestCaseList.vue'
import { runTest } from '@/api/api-test'
import { getParams } from '@/api/api-param'
import type { ApiVO, RunTestResp } from '@/types/api-definition'

const props = defineProps<{ apiId: number; detail: ApiVO }>()

const params = ref<any[]>([])
const paramValues = ref<Record<string, string>>({})
const running = ref(false)
const result = ref<RunTestResp | null>(null)

const responseBody = computed(() => {
  if (result.value?.data == null) return ''
  try {
    return JSON.stringify(result.value.data, null, 2)
  } catch {
    return String(result.value.data)
  }
})

onMounted(async () => {
  try {
    params.value = await getParams(props.apiId)
    params.value.forEach(p => {
      paramValues.value[p.name] = p.defaultValue || ''
    })
  } catch { /* use detail params as fallback */ }
  if (!params.value.length && props.detail.params?.length) {
    params.value = props.detail.params
    params.value.forEach(p => {
      paramValues.value[p.name] = p.defaultValue || ''
    })
  }
})

async function handleRun() {
  running.value = true
  try {
    result.value = await runTest(props.apiId, { params: paramValues.value })
    if (result.value.bizCode !== 0) {
      ElMessage.warning('请求返回异常状态')
    }
  } catch {
    ElMessage.error('测试执行失败')
  } finally {
    running.value = false
  }
}
</script>

<style scoped>
.test-panel {
  display: flex;
  gap: 24px;
}
.test-panel__left {
  width: 40%;
  flex-shrink: 0;
}
.test-panel__right {
  flex: 1;
  min-width: 0;
}
</style>
