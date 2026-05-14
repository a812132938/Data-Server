<template>
  <el-tag :type="tagType" size="small">{{ tagLabel }}</el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  status: string
  statusMap?: Record<string, { label: string; type: string }>
}>(), {
  statusMap: () => ({})
})

const tagLabel = computed(() => props.statusMap[props.status]?.label || props.status)
const tagType = computed(() => (props.statusMap[props.status]?.type || 'info') as any)
</script>
