<template>
  <div class="topbar-container">
    <div class="topbar-left">
      <div class="logo">D</div>
      <span class="title">数据服务平台</span>
      <el-tag size="small" type="info">v1.0</el-tag>
    </div>
    <div class="topbar-right">
      <a href="/docs" target="_blank" class="help-link">
        <el-icon><Document /></el-icon> 帮助文档
      </a>
      <div class="notification-wrapper" ref="bellRef">
        <el-badge :value="notificationStore.unreadCount" :hidden="notificationStore.unreadCount === 0" :max="99">
          <el-icon class="bell-icon" @click="showPanel = !showPanel"><Bell /></el-icon>
        </el-badge>
        <NotificationPanel v-if="showPanel" :visible="showPanel" @close="showPanel = false" />
      </div>
      <el-dropdown @command="handleUserCommand">
        <div class="user-info">
          <div class="avatar">{{ avatarChar }}</div>
          <span>{{ userStore.displayName || '用户' }}</span>
          <el-icon class="arrow"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notification'
import { useUserStore } from '@/stores/user'
import NotificationPanel from './NotificationPanel.vue'
import { onClickOutside } from '@vueuse/core'

const router = useRouter()
const notificationStore = useNotificationStore()
const userStore = useUserStore()
const showPanel = ref(false)
const bellRef = ref<HTMLElement>()

const avatarChar = computed(() => {
  const name = userStore.displayName
  return name ? name.charAt(0).toUpperCase() : 'U'
})

async function handleUserCommand(command: string) {
  if (command === 'logout') {
    await userStore.logout()
    router.replace({ name: 'Login' })
  }
}

onClickOutside(bellRef, () => { showPanel.value = false })
onMounted(() => {
  notificationStore.fetchUnreadCount()
  if (!userStore.userInfo) {
    userStore.fetchUser().catch(() => {})
  }
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.topbar-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 100%;
  background: $bg-white;
  border-bottom: 1px solid $border-color;
}

.topbar-left {
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

  .title { font-size: 16px; font-weight: 600; color: $text-primary; }
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 20px;

  .help-link {
    display: flex; align-items: center; gap: 4px;
    color: $text-secondary; font-size: 14px;
    &:hover { color: $primary-color; }
  }

  .notification-wrapper {
    position: relative;
    .bell-icon { font-size: 20px; cursor: pointer; color: $text-secondary; &:hover { color: $primary-color; } }
  }

  .user-info {
    display: flex; align-items: center; gap: 8px; cursor: pointer;
    .avatar {
      width: 28px; height: 28px;
      background: #E8F0FE;
      color: $primary-color;
      border-radius: 50%;
      display: flex; align-items: center; justify-content: center;
      font-size: 14px; font-weight: 600;
    }
    span { color: $text-primary; font-size: 14px; }
    .arrow { color: $text-secondary; font-size: 12px; }
  }
}
</style>
