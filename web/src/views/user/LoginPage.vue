<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="logo">D</div>
        <h1 class="title">数据服务平台</h1>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="用户名"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                :prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="loginLoading"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            @keyup.enter="handleRegister"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="用户名（3~64 位）"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="密码（6~64 位）"
                :prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                :prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item prop="nickname">
              <el-input
                v-model="registerForm.nickname"
                placeholder="昵称（选填）"
                :prefix-icon="UserFilled"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="邮箱（选填）"
                :prefix-icon="Message"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="手机号（选填）"
                :prefix-icon="Phone"
                size="large"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="registerLoading"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Message, Phone } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { register } from '@/api/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeTab = ref('login')
const loginLoading = ref(false)
const registerLoading = ref(false)

const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  phone: '',
})

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '用户名长度需在 3~64 之间', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '密码长度需在 6~64 之间', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function handleLogin() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loginLoading.value = true
  try {
    await userStore.login({ username: loginForm.username, password: loginForm.password })
    await userStore.fetchUser()
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    router.replace(redirect)
  } finally {
    loginLoading.value = false
  }
}

async function handleRegister() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return

  registerLoading.value = true
  try {
    await register({
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname || undefined,
      email: registerForm.email || undefined,
      phone: registerForm.phone || undefined,
    })
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    loginForm.username = registerForm.username
    loginForm.password = ''
  } finally {
    registerLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables' as *;

.login-page {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 420px;
  padding: 40px;
  background: $bg-white;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 32px;

  .logo {
    width: 40px;
    height: 40px;
    background: $primary-color;
    color: #fff;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 20px;
  }

  .title {
    font-size: 22px;
    font-weight: 600;
    color: $text-primary;
    margin: 0;
  }
}

.login-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 24px;
  }

  :deep(.el-tabs__nav) {
    width: 100%;
  }

  :deep(.el-tabs__item) {
    width: 50%;
    text-align: center;
    font-size: 16px;
  }
}

.submit-btn {
  width: 100%;
}
</style>
