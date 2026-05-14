<template>
  <div class="doc-layout">
    <div class="doc-topbar">
      <div class="doc-topbar-left">
        <div class="logo">D</div>
        <span class="title">API 文档中心</span>
      </div>
      <router-link to="/" class="back-link">
        <el-icon><Back /></el-icon> 返回控制台
      </router-link>
    </div>
    <div class="doc-body">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.doc-layout {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
}

.doc-topbar {
  height: $topbar-height;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: $bg-white;
  border-bottom: 1px solid $border-color;

  .doc-topbar-left {
    display: flex;
    align-items: center;
    gap: 10px;

    .logo {
      width: 28px; height: 28px;
      background: $primary-color;
      color: #fff;
      border-radius: 6px;
      display: flex; align-items: center; justify-content: center;
      font-weight: bold; font-size: 16px;
    }
    .title { font-size: 16px; font-weight: 600; }
  }

  .back-link {
    display: flex; align-items: center; gap: 4px;
    color: $text-secondary;
    &:hover { color: $primary-color; }
  }
}

.doc-body {
  flex: 1;
  overflow: hidden;
}
</style>
