<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="应用凭证"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
  >
    <el-alert type="error" :closable="false" show-icon style="margin-bottom: 16px">
      <template #title>
        <strong>请妥善保管以下凭证信息，关闭后将无法再次查看！</strong>
      </template>
    </el-alert>

    <el-descriptions :column="1" border>
      <el-descriptions-item label="AppKey">
        <div style="display: flex; align-items: center; gap: 8px">
          <code>{{ credential?.appKey }}</code>
          <el-button link type="primary" size="small" @click="copy(credential?.appKey)">复制</el-button>
        </div>
      </el-descriptions-item>
      <el-descriptions-item label="AppSecret">
        <div style="display: flex; align-items: center; gap: 8px">
          <code>{{ credential?.appSecret }}</code>
          <el-button link type="primary" size="small" @click="copy(credential?.appSecret)">复制</el-button>
        </div>
      </el-descriptions-item>
      <el-descriptions-item label="AppCode">
        <div style="display: flex; align-items: center; gap: 8px">
          <code>{{ credential?.appCode }}</code>
          <el-button link type="primary" size="small" @click="copy(credential?.appCode)">复制</el-button>
        </div>
      </el-descriptions-item>
    </el-descriptions>

    <template #footer>
      <el-button type="primary" @click="handleClose">我已复制，关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import type { AppCredential } from '@/types/app'

defineProps<{ modelValue: boolean; credential: AppCredential | null }>()
const emit = defineEmits<{ 'update:modelValue': [v: boolean] }>()

function copy(text?: string) {
  if (!text) return
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  })
}

function handleClose() {
  emit('update:modelValue', false)
}
</script>
