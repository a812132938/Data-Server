<template>
  <el-dialog :model-value="visible" :title="title" width="420px" @update:model-value="emit('update:visible', $event)">
    <div class="confirm-content">
      <el-icon :size="24" :color="type === 'danger' ? '#FF4D4F' : '#FAAD14'">
        <WarningFilled />
      </el-icon>
      <span>{{ message }}</span>
    </div>
    <template #footer>
      <el-button @click="emit('update:visible', false)">{{ cancelText }}</el-button>
      <el-button :type="type === 'danger' ? 'danger' : 'primary'" @click="emit('confirm')">{{ confirmText }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  visible: boolean
  title: string
  message: string
  type?: 'warning' | 'danger'
  confirmText?: string
  cancelText?: string
}>(), {
  type: 'warning',
  confirmText: '确认',
  cancelText: '取消',
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  confirm: []
}>()
</script>

<style lang="scss" scoped>
.confirm-content {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}
</style>
