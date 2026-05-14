import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/token'
import type { LoginReq, UserVO } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserVO | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const displayName = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')

  async function login(data: LoginReq) {
    const res = await loginApi(data)
    token.value = res.token
    setToken(res.token)
    return res
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      resetState()
    }
  }

  async function fetchUser() {
    const res = await getCurrentUser()
    userInfo.value = res
    return res
  }

  function resetState() {
    token.value = null
    userInfo.value = null
    removeToken()
  }

  return { token, userInfo, isLoggedIn, displayName, login, logout, fetchUser, resetState }
})
