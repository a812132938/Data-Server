<template>
  <div>
    <h4 style="margin-bottom:12px">查询条件配置</h4>
    <el-table :data="conditions" border>
      <el-table-column label="启用" width="64">
        <template #default="{ row }"><el-checkbox v-model="row._enabled" /></template>
      </el-table-column>
      <el-table-column prop="tableAlias" label="来源别名" width="92" />
      <el-table-column prop="tableName" label="来源表" min-width="150" />
      <el-table-column prop="field" label="字段名" min-width="130" />
      <el-table-column prop="dataType" label="字段类型" width="120" />
      <el-table-column label="操作符" width="110">
        <template #default="{ row }">
          <el-select v-model="row.operator" size="small">
            <el-option v-for="op in operators" :key="op" :label="op" :value="op" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="参数名" width="140">
        <template #default="{ row }"><el-input v-model="row.paramName" size="small" /></template>
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
      <el-table-column label="描述" min-width="160">
        <template #default="{ row }"><el-input v-model="row.description" size="small" /></template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

const props = defineProps<{ modelValue: any }>()
const operators = ['EQ', 'NE', 'GT', 'GE', 'LT', 'LE', 'LIKE', 'IN']
const conditions = ref<any[]>([])

const tables = computed(() => props.modelValue.schemas?.[0]?.tables || [])
const joinedTables = computed(() => {
  const design = props.modelValue.queryDesign
  const items: Array<{ tableName: string; alias: string }> = []
  if (design?.mainTable) items.push({ tableName: design.mainTable, alias: design.mainAlias || 't1' })
  ;(design?.joins || []).forEach((join: any) => {
    if (join.table && join.alias) items.push({ tableName: join.table, alias: join.alias })
  })
  return items
})
const availableFields = computed(() => joinedTables.value.flatMap((item) => {
  const table = tables.value.find((candidate: any) => candidate.name === item.tableName)
  return (table?.columns || []).map((column: any) => ({
    tableName: item.tableName,
    tableAlias: item.alias,
    field: column.name,
    dataType: column.type,
    description: column.comment || '',
    key: `${item.alias}.${column.name}`,
  }))
}))

watch(availableFields, rebuildConditions, { immediate: true })
watch(conditions, syncFilters, { deep: true })

function rebuildConditions() {
  const existing = new Map((props.modelValue.queryDesign?.filters || []).map((filter: any) => [`${filter.tableAlias}.${filter.field}`, filter]))
  conditions.value = availableFields.value.map((field) => {
    const saved = existing.get(field.key) as any
    return {
      ...field,
      _enabled: Boolean(saved),
      operator: saved?.operator || 'EQ',
      paramName: saved?.paramName || uniqueParamName(toCamelCase(field.field), field.tableAlias),
      required: saved?.required || false,
      defaultValue: saved?.defaultValue || '',
      example: saved?.example || '',
      description: saved?.description || field.description,
    }
  })
}

function syncFilters() {
  const filters = conditions.value.filter((condition) => condition._enabled).map((condition) => ({
    tableAlias: condition.tableAlias,
    field: condition.field,
    operator: condition.operator,
    paramName: condition.paramName,
    required: condition.required,
    defaultValue: condition.defaultValue,
    example: condition.example,
    description: condition.description,
  }))
  props.modelValue.queryDesign.filters = filters
  props.modelValue.params = filters.map((filter: any, index: number) => {
    const source = conditions.value.find((condition) => condition.tableAlias === filter.tableAlias && condition.field === filter.field)
    return {
      name: filter.paramName,
      location: 'QUERY',
      dataType: source?.dataType || 'STRING',
      required: filter.required,
      operator: filter.operator,
      defaultValue: filter.defaultValue,
      example: filter.example,
      description: filter.description,
      sort: index,
    }
  })
}

function uniqueParamName(base: string, tableAlias: string) {
  const used = new Set((props.modelValue.queryDesign?.filters || []).map((filter: any) => filter.paramName))
  if (!used.has(base)) return base
  const withAlias = `${tableAlias}${base.charAt(0).toUpperCase()}${base.slice(1)}`
  return used.has(withAlias) ? `${withAlias}${used.size + 1}` : withAlias
}

function toCamelCase(value: string) {
  return value.replace(/_([a-zA-Z0-9])/g, (_, char: string) => char.toUpperCase())
}
</script>
