<template>
  <el-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" :title="appId ? '编辑应用' : '新建应用'" width="500px" @open="handleOpen">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="应用名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入应用名称" />
      </el-form-item>
      <el-form-item label="应用描述">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入应用描述（选填）" />
      </el-form-item>
      <el-form-item label="负责人" prop="owner">
        <el-input v-model="form.owner" placeholder="请选择负责人" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">{{ appId ? '保存' : '创建' }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createApp, updateApp, getApp } from '@/api/app'
import type { AppCredential } from '@/types/app'

const props = defineProps<{ modelValue: boolean; appId: number }>()
const emit = defineEmits<{ 'update:modelValue': [v: boolean]; saved: [cred?: AppCredential] }>()

const formRef = ref<FormInstance>()
const saving = ref(false)
const form = reactive({ name: '', owner: '', description: '' })
const rules: FormRules = {
  name: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  owner: [{ required: true, message: '请输入负责人', trigger: 'blur' }],
}

async function handleOpen() {
  formRef.value?.resetFields()
  if (props.appId) {
    const detail = await getApp(props.appId)
    Object.assign(form, { name: detail.name, owner: detail.ownerName || detail.owner, description: detail.description || detail.purpose || '' })
  } else {
    Object.assign(form, { name: '', owner: '', description: '' })
  }
}

async function handleSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (props.appId) {
      await updateApp(props.appId, form)
      ElMessage.success('更新成功')
      emit('saved')
    } else {
      const res = await createApp(form)
      ElMessage.success('创建成功')
      emit('saved', res)
    }
  } finally {
    saving.value = false
  }
}
</script>
