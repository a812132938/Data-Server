<template>
  <el-switch
    :model-value="modelValue"
    :loading="loading"
    active-text="启用"
    inactive-text="禁用"
    active-color="#52C41A"
    @change="(val: any) => emit('change', val)"
  />
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  modelValue: boolean
  loading?: boolean
}>(), { loading: false })

const emit = defineEmits<{
  change: [value: boolean]
}>()
</script>
