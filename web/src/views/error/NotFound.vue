<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<script setup lang="ts">
</script>

<style lang="scss" scoped>
.not-found {
  text-align: center;
  padding-top: 120px;
  h1 { font-size: 72px; color: #C9CDD4; margin-bottom: 8px; }
  p { font-size: 16px; color: #86909C; margin-bottom: 24px; }
}
</style>
