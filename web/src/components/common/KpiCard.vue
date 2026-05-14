<template>
  <div class="kpi-card">
    <div class="kpi-header">
      <span class="dot" :style="{ background: color }"></span>
      <span class="kpi-title">{{ title }}</span>
    </div>
    <div class="kpi-value">{{ displayValue }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { formatNumber } from '@/utils/format'

const props = withDefaults(defineProps<{
  title: string
  value: number | string
  color?: string
  suffix?: string
}>(), { color: '#2878FF' })

const displayValue = computed(() => {
  if (typeof props.value === 'string') return props.value
  return formatNumber(props.value) + (props.suffix || '')
})
</script>

<style lang="scss" scoped>
.kpi-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  flex: 1;

  .kpi-header {
    display: flex;
    align-items: center;
    gap: 8px;
    .dot { width: 10px; height: 10px; border-radius: 50%; }
    .kpi-title { font-size: 15px; color: #86909C; }
  }

  .kpi-value {
    margin-top: 12px;
    font-size: 32px;
    font-weight: 600;
    color: #1D2129;
  }
}
</style>
