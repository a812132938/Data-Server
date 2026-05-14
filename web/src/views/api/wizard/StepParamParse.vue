<template>
  <div>
    <el-button type="primary" @click="handleParse" :loading="parsing">解析参数</el-button>
    <el-alert v-for="(w, i) in warnings" :key="i" :title="w" type="warning" :closable="false" show-icon style="margin-top:8px" />
    <el-table :data="modelValue.params" border style="margin-top:16px">
      <el-table-column prop="name" label="参数名" width="120" />
      <el-table-column label="来源" width="100">
        <template #default="{ row }"><el-tag size="small">{{ row.location }}</el-tag></template>
      </el-table-column>
      <el-table-column label="类型" width="120">
        <template #default="{ row }">
          <el-select v-model="row.dataType" size="small">
            <el-option v-for="t in dataTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="必填" width="70">
        <template #default="{ row }"><el-switch v-model="row.required" size="small" /></template>
      </el-table-column>
      <el-table-column label="默认值" width="120">
        <template #default="{ row }"><el-input v-model="row.defaultValue" size="small" /></template>
      </el-table-column>
      <el-table-column label="示例" width="120">
        <template #default="{ row }"><el-input v-model="row.example" size="small" /></template>
      </el-table-column>
      <el-table-column label="描述" min-width="140">
        <template #default="{ row }"><el-input v-model="row.description" size="small" /></template>
      </el-table-column>
      <el-table-column label="操作" width="60">
        <template #default="{ $index }">
          <el-button link type="danger" @click="modelValue.params.splice($index, 1)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-button style="margin-top:8px" @click="addParam">+ 添加参数</el-button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { parseParams } from '@/api/api-param'
import { useRoute } from 'vue-router'

const props = defineProps<{ modelValue: any }>()
const route = useRoute()
const parsing = ref(false)
const warnings = ref<string[]>([])
const dataTypes = ['STRING', 'INT', 'LONG', 'BOOLEAN', 'DECIMAL']

async function handleParse() {
  const apiId = Number(route.query.id) || 0
  if (!apiId && !props.modelValue.sqlTemplate) return
  parsing.value = true
  try {
    const res = await parseParams(apiId, { sqlTemplate: props.modelValue.sqlTemplate })
    props.modelValue.params = res.params
    warnings.value = res.warnings || []
  } finally { parsing.value = false }
}

function addParam() {
  props.modelValue.params.push({
    name: '', location: 'QUERY', dataType: 'STRING', required: false,
    defaultValue: '', example: '', description: '', sort: props.modelValue.params.length,
  })
}
</script>
