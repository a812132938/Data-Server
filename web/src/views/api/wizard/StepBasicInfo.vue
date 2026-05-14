<template>
  <el-form ref="formRef" :model="modelValue" label-width="100px" :rules="rules">
    <el-form-item label="API 名称" prop="name">
      <el-input v-model="modelValue.name" placeholder="请输入 API 名称" />
    </el-form-item>
    <el-form-item label="分组">
      <el-select v-model="modelValue.groupId" placeholder="选择分组" clearable>
        <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
      </el-select>
    </el-form-item>
    <el-form-item label="请求路径" prop="path">
      <el-input v-model="modelValue.path" placeholder="/gateway/your-api-path">
        <template #prepend>/gateway/</template>
      </el-input>
    </el-form-item>
    <el-form-item label="请求方法">
      <el-radio-group v-model="modelValue.method">
        <el-radio-button value="GET">GET</el-radio-button>
        <el-radio-button value="POST">POST</el-radio-button>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="超时(ms)">
      <el-input-number v-model="modelValue.timeoutMs" :min="1000" :max="300000" :step="1000" />
    </el-form-item>
    <el-form-item label="缓存">
      <el-switch v-model="modelValue.cacheEnable" />
      <el-input-number v-if="modelValue.cacheEnable" v-model="modelValue.cacheTtl" :min="1" style="margin-left:12px" />
      <span v-if="modelValue.cacheEnable" style="margin-left:4px;color:#86909C">秒</span>
    </el-form-item>
    <el-form-item label="限流 QPS" prop="defaultQpsLimit">
      <el-input-number v-model="modelValue.defaultQpsLimit" :min="1" :max="100000" />
    </el-form-item>
    <el-form-item label="描述">
      <el-input v-model="modelValue.description" type="textarea" :rows="3" />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getApiGroups } from '@/api/api-group'
import type { ApiGroupVO } from '@/types/api-definition'

defineProps<{ modelValue: any }>()
const groups = ref<ApiGroupVO[]>([])
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路径', trigger: 'blur' }],
  defaultQpsLimit: [{ required: true, message: '请输入QPS', trigger: 'blur' }],
}

onMounted(async () => {
  const d = await getApiGroups()
  groups.value = Array.isArray(d) ? d : []
})
</script>
