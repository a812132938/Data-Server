<template>
  <el-dialog :model-value="visible" @update:model-value="emit('update:visible', $event)"
    :title="datasourceId ? '编辑数据源' : '新增数据源'" width="640px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <div class="section-title">基本信息</div>
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入数据源名称" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="form.type" placeholder="选择类型" @change="onTypeChange">
          <el-option v-for="t in DATASOURCE_TYPES" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="2" />
      </el-form-item>

      <div class="section-title">连接配置</div>
      <el-form-item label="主机" prop="host">
        <el-input v-model="form.host" placeholder="localhost" />
      </el-form-item>
      <el-form-item label="端口" prop="port">
        <el-input-number v-model="form.port" :min="1" :max="65535" />
      </el-form-item>
      <el-form-item label="数据库名" prop="databaseName">
        <el-input v-model="form.databaseName" />
      </el-form-item>
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" type="password" show-password
          :placeholder="datasourceId ? '留空保留原密码' : '请输入密码'" />
      </el-form-item>
      <el-form-item label="JDBC参数">
        <el-input v-model="form.jdbcParams" placeholder="例: useSSL=false&serverTimezone=Asia/Shanghai" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:visible', false)">取消</el-button>
      <el-button @click="handleTestConnection" :loading="testing">测试连接</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { getDatasource, createDatasource, updateDatasource, testConnection } from '@/api/datasource'
import { DATASOURCE_TYPES, DATASOURCE_DEFAULT_PORTS } from '@/utils/constants'
import type { DatasourceUpsertReq } from '@/types/datasource'

const props = defineProps<{ visible: boolean; datasourceId?: number }>()
const emit = defineEmits<{ 'update:visible': [val: boolean]; success: [] }>()

const formRef = ref<FormInstance>()
const testing = ref(false)
const submitting = ref(false)

const form = reactive<DatasourceUpsertReq>({
  name: '', type: 'MYSQL', host: '', port: 3306, databaseName: '',
  username: '', password: '', jdbcParams: '', description: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  host: [{ required: true, message: '请输入主机', trigger: 'blur' }],
  port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
  databaseName: [{ required: true, message: '请输入数据库名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: !props.datasourceId, message: '请输入密码', trigger: 'blur' }],
}

function onTypeChange(type: string) {
  form.port = DATASOURCE_DEFAULT_PORTS[type] || 3306
}

watch(() => props.visible, async (val) => {
  if (val && props.datasourceId) {
    const data = await getDatasource(props.datasourceId)
    Object.assign(form, { ...data, password: '' })
  } else if (val) {
    Object.assign(form, { name: '', type: 'MYSQL', host: '', port: 3306, databaseName: '', username: '', password: '', jdbcParams: '', description: '' })
  }
})

async function handleTestConnection() {
  testing.value = true
  try {
    const res = await testConnection(form)
    if (res.success) ElMessage.success(`连接成功 (${res.costMs}ms)`)
    else ElMessage.error(`连接失败: ${res.message}`)
  } finally { testing.value = false }
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (props.datasourceId) {
      await updateDatasource(props.datasourceId, form)
      ElMessage.success('更新成功')
    } else {
      await createDatasource(form)
      ElMessage.success('创建成功')
    }
    emit('update:visible', false)
    emit('success')
  } finally { submitting.value = false }
}
</script>

<style lang="scss" scoped>
.section-title {
  font-size: 16px; font-weight: 600; color: #1D2129;
  margin: 20px 0 14px; padding-left: 10px;
  border-left: 3px solid #2878FF;
}

:deep(.el-form-item__label) {
  font-size: 15px;
}

:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  font-size: 15px;
}
</style>
