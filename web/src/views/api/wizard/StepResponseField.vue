<template>
  <div>
    <h4 style="margin-bottom:12px">返回字段配置</h4>
    <el-table :data="rows" border>
      <el-table-column v-if="isJoinWizard" prop="sourceAlias" label="来源别名" width="90" />
      <el-table-column v-if="isJoinWizard" prop="sourceTable" label="来源表" min-width="140" />
      <el-table-column v-if="isJoinWizard" prop="sourceField" label="原字段名" min-width="120" />
      <el-table-column prop="fieldName" label="输出字段名" min-width="130" />
      <el-table-column label="别名" width="140">
        <template #default="{ row }"><el-input v-model="row.alias" size="small" @change="syncAlias(row)" /></template>
      </el-table-column>
      <el-table-column label="类型" width="120">
        <template #default="{ row }"><el-tag size="small" type="info">{{ row.dataType }}</el-tag></template>
      </el-table-column>
      <el-table-column label="描述" min-width="160">
        <template #default="{ row }"><el-input v-model="row.description" size="small" /></template>
      </el-table-column>
      <el-table-column label="脱敏" width="70">
        <template #default="{ row }"><el-switch v-model="row.sensitive" size="small" /></template>
      </el-table-column>
      <el-table-column label="脱敏规则" width="130">
        <template #default="{ row }">
          <el-select v-if="row.sensitive" v-model="row.sensitiveRule" size="small">
            <el-option label="手机号" value="PHONE" />
            <el-option label="邮箱" value="EMAIL" />
            <el-option label="身份证" value="IDCARD" />
            <el-option label="自定义" value="CUSTOM" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="排序" width="96">
        <template #default="{ row }"><el-input-number v-model="row.sort" size="small" :min="0" controls-position="right" /></template>
      </el-table-column>
    </el-table>
    <el-alert
      v-if="isJoinWizard && !modelValue.responseFields.length"
      title="请先在选表与字段页点击生成 SQL，或进入保存确认页自动生成返回字段。"
      type="info"
      :closable="false"
      show-icon
      style="margin-top:12px"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'

const props = defineProps<{ modelValue: any }>()

const isJoinWizard = computed(() => props.modelValue.createMode !== 'SCRIPT')
const selectFieldMap = computed(() => {
  const map = new Map<string, any>()
  ;(props.modelValue.queryDesign?.selectFields || []).forEach((field: any) => {
    map.set(field.alias || field.field, field)
  })
  return map
})
const joinedTables = computed(() => {
  const design = props.modelValue.queryDesign
  const items: Array<{ tableName: string; alias: string }> = []
  if (design?.mainTable) items.push({ tableName: design.mainTable, alias: design.mainAlias || 't1' })
  ;(design?.joins || []).forEach((join: any) => {
    if (join.table && join.alias) items.push({ tableName: join.table, alias: join.alias })
  })
  return items
})
const rows = computed(() => (props.modelValue.responseFields || []).map((field: any) => {
  const source = selectFieldMap.value.get(field.fieldName) || selectFieldMap.value.get(field.alias)
  const table = joinedTables.value.find((item) => item.alias === source?.tableAlias)
  field.sourceAlias = source?.tableAlias || ''
  field.sourceTable = table?.tableName || ''
  field.sourceField = source?.field || ''
  return field
}))

watch(() => props.modelValue.selectedColumns, (cols) => {
  if (isJoinWizard.value || !cols || cols.length === 0) return
  const existingFields = new Map(
    (props.modelValue.responseFields || []).map((field: any) => [field.fieldName, field])
  )
  props.modelValue.responseFields = cols.map((col: any, index: number) => {
    const existing = existingFields.get(col.name) as any
    if (existing) return { ...existing, sort: existing.sort ?? index }
    return {
      fieldName: col.name,
      alias: col._alias || '',
      dataType: col.type,
      description: '',
      sensitive: false,
      sensitiveRule: '',
      sort: index,
    }
  })
}, { immediate: true })

function syncAlias(row: any) {
  const realField = props.modelValue.responseFields.find((field: any) => field.fieldName === row.fieldName)
  if (realField) realField.alias = row.alias
  const selectField = selectFieldMap.value.get(row.fieldName) || selectFieldMap.value.get(row.alias)
  if (selectField) selectField.alias = row.alias
}
</script>
