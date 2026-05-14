<template>
  <el-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" title="申请 API 授权" width="520px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="目标 API" prop="apiId">
        <el-select v-model="form.apiId" filterable placeholder="搜索并选择 API" style="width: 100%" @focus="loadApis">
          <el-option v-for="api in apiOptions" :key="api.id" :label="`${api.name} (${api.path})`" :value="api.id" :disabled="api._authorized" />
        </el-select>
      </el-form-item>
      <el-form-item label="QPS 限制" prop="qpsLimit">
        <el-input-number v-model="form.qpsLimit" :min="1" :max="10000" />
        <span style="margin-left: 8px; color: #999; font-size: 12px">
          API 上限: {{ selectedApiQps || '-' }}
        </span>
      </el-form-item>
      <el-form-item label="有效期">
        <el-radio-group v-model="form.expireType">
          <el-radio value="PERMANENT">永久</el-radio>
          <el-radio value="DATE">指定日期</el-radio>
        </el-radio-group>
        <el-date-picker v-if="form.expireType === 'DATE'" v-model="form.expireAt" type="date" placeholder="选择到期日" style="margin-left: 12px" />
      </el-form-item>
      <el-form-item label="申请理由" prop="reason">
        <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请说明授权申请原因" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getApiList } from '@/api/api-definition'
import { createApproval } from '@/api/approval'

const props = defineProps<{ modelValue: boolean; appId: number }>()
const emit = defineEmits<{ 'update:modelValue': [v: boolean]; applied: [] }>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const apiOptions = ref<any[]>([])

const form = reactive({
  apiId: null as number | null,
  qpsLimit: 100,
  expireType: 'PERMANENT',
  expireAt: '',
  reason: '',
})

const rules: FormRules = {
  apiId: [{ required: true, message: '请选择 API', trigger: 'change' }],
  qpsLimit: [{ required: true, message: '请输入 QPS 限制', trigger: 'blur' }],
  reason: [{ required: true, message: '请填写申请理由', trigger: 'blur' }],
}

const selectedApiQps = computed(() => {
  const api = apiOptions.value.find(a => a.id === form.apiId)
  return api?.defaultQpsLimit || null
})

async function loadApis() {
  if (apiOptions.value.length) return
  const res = await getApiList({ page: 1, size: 200, status: 'PUBLISHED' })
  apiOptions.value = res.records.map((a: any) => ({
    ...a,
    _authorized: false, // could cross-check with existing auths
  }))
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await createApproval({
      appId: props.appId,
      apiId: form.apiId!,
      qpsLimit: form.qpsLimit,
      expireAt: form.expireType === 'DATE' ? form.expireAt : undefined,
      reason: form.reason,
    })
    ElMessage.success('授权申请已自动通过')
    emit('update:modelValue', false)
    emit('applied')
  } finally {
    submitting.value = false
  }
}
</script>
