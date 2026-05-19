<template>
  <el-dialog :model-value="modelValue" @update:model-value="$emit('update:modelValue', $event)" :title="rule ? '编辑告警规则' : '新建告警规则'" width="600px" @open="handleOpen">
    <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
      <el-form-item label="规则名称" prop="name">
        <el-input v-model="form.name" placeholder="如：API 错误率过高" />
      </el-form-item>
      <el-form-item label="目标类型" prop="targetType">
        <el-radio-group v-model="form.targetType">
          <el-radio v-for="t in ALERT_TARGET_TYPES" :key="t.value" :value="t.value">{{ t.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="form.targetType !== 'GLOBAL'" label="目标" prop="targetId">
        <el-select v-model="form.targetId" filterable placeholder="选择目标" style="width: 100%" @focus="loadTargets">
          <el-option v-for="t in targetOptions" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="监控指标" prop="metric">
        <el-select v-model="form.metric" placeholder="选择指标" style="width: 100%">
          <el-option v-for="m in ALERT_METRICS" :key="m.value" :label="m.label" :value="m.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="判断条件" required>
        <div style="display: flex; gap: 8px; align-items: center">
          <el-select v-model="form.operator" style="width: 100px">
            <el-option v-for="op in ALERT_OPERATORS" :key="op.value" :label="op.label" :value="op.value" />
          </el-select>
          <el-input-number v-model="form.threshold" :min="0" />
          <span style="color: #999">{{ metricUnit }}</span>
        </div>
      </el-form-item>
      <el-form-item label="时间窗口">
        <el-select v-model="form.windowMinutes" style="width: 160px">
          <el-option :value="1" label="1 分钟" />
          <el-option :value="5" label="5 分钟" />
          <el-option :value="15" label="15 分钟" />
          <el-option :value="30" label="30 分钟" />
          <el-option :value="60" label="60 分钟" />
        </el-select>
      </el-form-item>
      <el-form-item label="抑制窗口">
        <el-select v-model="form.silenceMinutes" style="width: 160px">
          <el-option :value="5" label="5 分钟" />
          <el-option :value="15" label="15 分钟" />
          <el-option :value="30" label="30 分钟" />
          <el-option :value="60" label="60 分钟" />
        </el-select>
      </el-form-item>
      <el-form-item label="通知渠道">
        <el-checkbox-group v-model="form.channels">
          <el-checkbox value="SITE" label="站内信" disabled />
        </el-checkbox-group>
      </el-form-item>
      <el-form-item label="接收人">
        <el-input v-model="form.receivers" placeholder="多个接收人用逗号分隔" />
      </el-form-item>
      <el-form-item label="启用">
        <el-switch v-model="form.enabled" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { createAlertRule, updateAlertRule } from '@/api/alert'
import { getApiList } from '@/api/api-definition'
import { getAppList } from '@/api/app'
import { ALERT_METRICS, ALERT_OPERATORS, ALERT_TARGET_TYPES } from '@/utils/constants'
import type { AlertRuleVO } from '@/types/alert'

const props = defineProps<{ modelValue: boolean; rule: AlertRuleVO | null }>()
const emit = defineEmits<{ 'update:modelValue': [v: boolean]; saved: [] }>()

const formRef = ref<FormInstance>()
const saving = ref(false)
const targetOptions = ref<any[]>([])

const form = reactive({
  name: '',
  targetType: 'GLOBAL',
  targetId: null as number | null,
  metric: '',
  operator: 'GT',
  threshold: 0,
  windowMinutes: 5,
  silenceMinutes: 15,
  channels: ['SITE'],
  receivers: '',
  enabled: true,
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  metric: [{ required: true, message: '请选择监控指标', trigger: 'change' }],
}

const metricUnit = computed(() => {
  const m = ALERT_METRICS.find(m => m.value === form.metric)
  return m?.unit || ''
})

function handleOpen() {
  formRef.value?.resetFields()
  if (props.rule) {
    Object.assign(form, {
      name: props.rule.name,
      targetType: props.rule.targetType,
      targetId: props.rule.targetId,
      metric: props.rule.metric,
      operator: props.rule.operator,
      threshold: props.rule.threshold,
      windowMinutes: props.rule.windowMinutes || 5,
      silenceMinutes: props.rule.silenceMinutes || 15,
      channels: props.rule.channels || ['SITE'],
      receivers: props.rule.receivers || '',
      enabled: props.rule.enabled,
    })
  } else {
    Object.assign(form, {
      name: '', targetType: 'GLOBAL', targetId: null, metric: '',
      operator: 'GT', threshold: 0, windowMinutes: 5, silenceMinutes: 15,
      channels: ['SITE'], receivers: '', enabled: true,
    })
  }
}

async function loadTargets() {
  if (form.targetType === 'API') {
    const res = await getApiList({ page: 1, size: 200 })
    targetOptions.value = res.records
  } else if (form.targetType === 'APP') {
    const res = await getAppList({ page: 1, size: 200 })
    targetOptions.value = res.records
  }
}

async function handleSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = { ...form }
    if (props.rule) {
      await updateAlertRule(props.rule.id, payload)
    } else {
      await createAlertRule(payload)
    }
    ElMessage.success('保存成功')
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>
