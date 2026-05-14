<template>
  <el-empty :description="description" />
</template>

<script setup lang="ts">
withDefaults(defineProps<{ description?: string }>(), { description: '暂无数据' })
</script>
