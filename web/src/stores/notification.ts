import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getNotifications } from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)

  async function fetchUnreadCount() {
    try {
      const data = await getNotifications({ status: 'UNREAD', limit: 1 })
      unreadCount.value = data.unreadCount
    } catch {
      // ignore
    }
  }

  function decrementUnread(count = 1) {
    unreadCount.value = Math.max(0, unreadCount.value - count)
  }

  function clearUnread() {
    unreadCount.value = 0
  }

  return { unreadCount, fetchUnreadCount, decrementUnread, clearUnread }
})
