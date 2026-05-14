<template>
  <el-drawer :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" title="日志详情" size="560px" @open="fetchDetail">
    <template v-if="detail">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="Trace ID">
          <code>{{ detail.traceId }}</code>
        </el-descriptions-item>
        <el-descriptions-item label="API">{{ detail.apiName }} ({{ detail.method }})</el-descriptions-item>
        <el-descriptions-item label="请求路径">{{ detail.apiPath }}</el-descriptions-item>
        <el-descriptions-item label="应用">{{ detail.appName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="HTTP 状态">
          <el-tag :type="detail.httpStatus < 400 ? 'success' : 'danger'" size="small">{{ detail.httpStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.costMs }}ms</el-descriptions-item>
        <el-descriptions-item label="客户端 IP">{{ detail.clientIp }}</el-descriptions-item>
        <el-descriptions-item label="响应大小">{{ detail.responseSize ? detail.responseSize + ' bytes' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="返回记录数">{{ detail.recordCount ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="业务码">{{ detail.bizCode ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="错误消息">{{ detail.errorMessage || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求时间">{{ formatTime(detail.createdAt) }}</el-descriptions-item>
      </el-descriptions>

      <h4 style="margin-top: 16px">请求参数</h4>
      <el-input
        type="textarea"
        :model-value="formatJson(detail.requestParams)"
        :rows="6"
        readonly
        style="margin-top: 8px; font-family: monospace"
      />

    </template>
    <el-skeleton v-else :rows="10" animated />
  </el-drawer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { getLogDetail } from '@/api/log'
import { formatTime } from '@/utils/format'

const props = defineProps<{ modelValue: boolean; logId: number }>()
defineEmits<{ 'update:modelValue': [v: boolean] }>()

const detail = ref<any>(null)

async function fetchDetail() {
  detail.value = null
  if (!props.logId) return
  detail.value = await getLogDetail(props.logId)
}

function formatJson(val: any) {
  if (!val) return '-'
  if (typeof val === 'string') {
    try { return JSON.stringify(JSON.parse(val), null, 2) } catch { return val }
  }
  return JSON.stringify(val, null, 2)
}
</script>
