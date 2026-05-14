<template>
  <div class="page-header">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.label" :to="item.path ? { path: item.path } : undefined">
        {{ item.label }}
      </el-breadcrumb-item>
    </el-breadcrumb>
    <h2 class="page-title">{{ title }}</h2>
    <p v-if="description" class="page-desc">{{ description }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  description?: string
  breadcrumbs: Array<{ label: string; path?: string }>
}>()
</script>

<style lang="scss" scoped>
.page-header {
  margin-bottom: 20px;

  .page-title {
    margin: 12px 0 6px;
    font-size: 24px;
    font-weight: 600;
    color: #1D2129;
  }

  .page-desc {
    font-size: 15px;
    color: #86909C;
    margin: 0;
  }
}
</style>
