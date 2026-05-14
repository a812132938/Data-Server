<template>
  <div class="app-layout">
    <TopBar class="topbar" />
    <div class="body-area">
      <Sidebar class="sidebar" :class="{ collapsed: appStore.sidebarCollapsed }" />
      <div class="main-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import Sidebar from './Sidebar.vue'
import TopBar from './TopBar.vue'

const appStore = useAppStore()
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.app-layout {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;

  .topbar {
    height: $topbar-height;
    flex-shrink: 0;
  }

  .body-area {
    flex: 1;
    display: flex;
    overflow: hidden;

    .sidebar {
      width: $sidebar-width;
      flex-shrink: 0;
      transition: width 0.3s;
      &.collapsed { width: $sidebar-collapsed-width; }
    }

    .main-content {
      flex: 1;
      overflow-y: auto;
      padding: 20px;
      background-color: $bg-color;
    }
  }
}
</style>
