<template>
  <div class="sidebar-container">
    <el-menu
      :default-active="activeMenu"
      :collapse="appStore.sidebarCollapsed"
      background-color="#001529"
      text-color="#ffffffb3"
      active-text-color="#ffffff"
      router
      :unique-opened="true"
    >
      <div class="menu-group-title">数据管理</div>
      <el-menu-item index="/datasources">
        <el-icon><Grid /></el-icon>
        <template #title>数据源管理</template>
      </el-menu-item>
      <el-menu-item index="/apis">
        <el-icon><Connection /></el-icon>
        <template #title>API 定义</template>
      </el-menu-item>

      <div class="menu-group-title">应用与授权</div>
      <el-menu-item index="/apps">
        <el-icon><Box /></el-icon>
        <template #title>应用管理</template>
      </el-menu-item>
      <el-menu-item index="/approvals">
        <el-icon><Stamp /></el-icon>
        <template #title>授权审批</template>
      </el-menu-item>

      <div class="menu-group-title">运行监控</div>
      <el-menu-item index="/logs">
        <el-icon><Document /></el-icon>
        <template #title>调用日志</template>
      </el-menu-item>
      <el-menu-item index="/dashboard">
        <el-icon><DataBoard /></el-icon>
        <template #title>监控仪表盘</template>
      </el-menu-item>

      <div class="menu-group-title">运维管理</div>
      <el-menu-item index="/alerts">
        <el-icon><Bell /></el-icon>
        <template #title>告警规则</template>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const appStore = useAppStore()

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/datasources')) return '/datasources'
  if (path.startsWith('/apis')) return '/apis'
  if (path.startsWith('/apps')) return '/apps'
  if (path.startsWith('/approvals')) return '/approvals'
  if (path.startsWith('/logs')) return '/logs'
  if (path.startsWith('/dashboard')) return '/dashboard'
  if (path.startsWith('/alerts')) return '/alerts'
  return path
})
</script>

<style lang="scss" scoped>
.sidebar-container {
  height: 100%;
  background: #001529;
  overflow-y: auto;

  .el-menu {
    border-right: none;
  }

  .menu-group-title {
    padding: 20px 20px 8px;
    font-size: 12px;
    color: #ffffff4d;
    text-transform: uppercase;
    letter-spacing: 1px;
  }

  :deep(.el-menu-item) {
    height: 44px;
    line-height: 44px;
    &.is-active {
      background-color: #0050B3 !important;
    }
    &:hover {
      background-color: #ffffff1a !important;
    }
  }
}
</style>
