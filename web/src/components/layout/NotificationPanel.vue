<template>
  <div class="notification-panel">
    <div class="panel-header">
      <span class="title">通知</span>
      <el-link type="primary" :underline="false" @click="handleReadAll">全部已读</el-link>
    </div>
    <div class="panel-body" v-loading="loading">
      <div v-if="notifications.length === 0" class="empty">暂无通知</div>
      <div
        v-for="item in notifications"
        :key="item.id"
        class="notification-item"
        :class="{ unread: item.status === 'UNREAD' }"
        @click="handleClick(item)"
      >
        <div class="item-dot" v-if="item.status === 'UNREAD'"></div>
        <div class="item-content">
          <div class="item-title">{{ item.title }}</div>
          <div class="item-desc">{{ item.content }}</div>
          <div class="item-time">{{ item.createdAt }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getNotifications, markAsRead, markAllAsRead, type NotificationItem } from '@/api/notification'
import { useNotificationStore } from '@/stores/notification'

const emit = defineEmits<{ close: [] }>()
const notificationStore = useNotificationStore()
const loading = ref(false)
const notifications = ref<NotificationItem[]>([])

async function fetchList() {
  loading.value = true
  try {
    const data = await getNotifications({ limit: 20 })
    notifications.value = data.records
  } finally {
    loading.value = false
  }
}

async function handleReadAll() {
  await markAllAsRead()
  notificationStore.clearUnread()
  notifications.value.forEach(n => n.status = 'READ')
}

async function handleClick(item: NotificationItem) {
  if (item.status === 'UNREAD') {
    await markAsRead(item.id)
    item.status = 'READ'
    notificationStore.decrementUnread()
  }
  emit('close')
}

onMounted(fetchList)
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.notification-panel {
  position: absolute;
  top: 40px;
  right: -40px;
  width: 360px;
  max-height: 400px;
  background: $bg-white;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  z-index: 2000;
  overflow: hidden;

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid $border-color;
    .title { font-weight: 600; font-size: 15px; }
  }

  .panel-body {
    max-height: 340px;
    overflow-y: auto;

    .empty { padding: 40px; text-align: center; color: $text-secondary; }

    .notification-item {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      padding: 12px 16px;
      cursor: pointer;
      &:hover { background: #f7f8fa; }
      &.unread { background: #f0f5ff; }

      .item-dot {
        width: 6px; height: 6px;
        border-radius: 50%;
        background: $primary-color;
        margin-top: 7px;
        flex-shrink: 0;
      }

      .item-content {
        flex: 1;
        .item-title { font-size: 14px; font-weight: 500; color: $text-primary; }
        .item-desc { font-size: 13px; color: $text-secondary; margin-top: 4px; }
        .item-time { font-size: 12px; color: $text-placeholder; margin-top: 4px; }
      }
    }
  }
}
</style>
