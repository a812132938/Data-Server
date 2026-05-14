<template>
  <div class="join-wizard-step">
    <div class="step-toolbar">
      <div>
        <h4>选表与字段</h4>
        <p>从数据源 Schema 中选择主表、关联表、JOIN 条件和输出字段。</p>
      </div>
      <div class="toolbar-actions">
        <el-button type="primary" :loading="rendering" @click="handleRender">生成 SQL</el-button>
        <el-button :loading="previewing" @click="handlePreview">预览结果</el-button>
      </div>
    </div>

    <div class="join-layout">
      <section class="table-browser">
        <el-input v-model="tableKeyword" placeholder="搜索表名" clearable size="small" />
        <div v-loading="loading" class="table-list">
          <button
            v-for="table in filteredTables"
            :key="table.name"
            class="table-item"
            :class="{ active: inspectTable?.name === table.name }"
            type="button"
            @click="inspectTable = table"
          >
            <span>{{ table.name }}</span>
            <small>{{ table.comment || '无注释' }}</small>
          </button>
        </div>
        <div v-if="inspectTable" class="column-list">
          <div class="column-title">{{ inspectTable.name }} 字段</div>
          <div v-for="col in inspectTable.columns" :key="col.name" class="column-item">
            <span class="column-name">{{ col.name }}</span>
            <el-tag size="small" type="info">{{ col.type }}</el-tag>
            <el-tag v-if="isPk(col)" size="small" type="warning">PK</el-tag>
            <el-tag v-if="isFk(inspectTable.name, col.name)" size="small" type="success">FK</el-tag>
            <el-tag v-if="hasIndex(inspectTable, col.name)" size="small">IDX</el-tag>
          </div>
        </div>
      </section>

      <section class="join-config">
        <div class="config-block">
          <div class="block-title">主表</div>
          <div class="form-row">
            <el-select v-model="design.mainTable" placeholder="选择主表" filterable @change="handleMainTableChange">
              <el-option v-for="table in tables" :key="table.name" :label="table.name" :value="table.name" />
            </el-select>
            <el-input v-model="design.mainAlias" placeholder="别名" @blur="normalizeAliases" />
          </div>
        </div>

        <div class="config-block">
          <div class="block-heading">
            <div class="block-title">关联表</div>
            <el-button size="small" @click="addJoin" :disabled="!design.mainTable">添加关联表</el-button>
          </div>
          <div v-if="design.joins.length === 0" class="empty-tip">尚未添加关联表</div>
          <div v-for="(join, index) in design.joins" :key="index" class="join-item">
            <div class="join-row">
              <el-select v-model="join.type" class="join-type">
                <el-option label="LEFT" value="LEFT" />
                <el-option label="INNER" value="INNER" />
              </el-select>
              <el-select v-model="join.table" placeholder="关联表" filterable @change="handleJoinTableChange(join)">
                <el-option
                  v-for="table in joinableTables"
                  :key="table.name"
                  :label="table.name"
                  :value="table.name"
                />
              </el-select>
              <el-input v-model="join.alias" class="alias-input" placeholder="别名" @blur="normalizeAliases" />
              <el-button link type="danger" @click="removeJoin(index)">删除</el-button>
            </div>
            <div v-for="(condition, cIndex) in join.conditions" :key="cIndex" class="condition-row">
              <el-select v-model="condition.leftField" filterable placeholder="左侧字段">
                <el-option v-for="field in joinFieldOptions" :key="field.value" :label="field.label" :value="field.value" />
              </el-select>
              <span>=</span>
              <el-select v-model="condition.rightField" filterable placeholder="右侧字段">
                <el-option v-for="field in joinFieldOptions" :key="field.value" :label="field.label" :value="field.value" />
              </el-select>
              <el-button link type="danger" @click="removeJoinCondition(join, cIndex)">移除</el-button>
            </div>
            <el-button size="small" text @click="join.conditions.push({ leftField: '', operator: '=', rightField: '' })">
              添加 JOIN 条件
            </el-button>
          </div>
        </div>

        <el-alert
          v-for="(warning, index) in modelValue.renderWarnings"
          :key="index"
          :title="warning"
          type="warning"
          :closable="false"
          show-icon
        />
      </section>

      <section class="field-picker">
        <div class="block-title">输出字段</div>
        <div v-if="joinedFields.length === 0" class="empty-tip">请先选择主表或关联表</div>
        <el-table v-else :data="joinedFields" border height="420">
          <el-table-column label="选择" width="64">
            <template #default="{ row }">
              <el-checkbox :model-value="isSelected(row)" @change="toggleField(row, $event)" />
            </template>
          </el-table-column>
          <el-table-column prop="tableAlias" label="表别名" width="82" />
          <el-table-column prop="field" label="字段" min-width="130" />
          <el-table-column prop="type" label="类型" width="110" />
          <el-table-column label="输出别名" width="150">
            <template #default="{ row }">
              <el-input
                :model-value="getSelectAlias(row)"
                size="small"
                :disabled="!isSelected(row)"
                @update:model-value="updateSelectAlias(row, $event)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="comment" label="注释" min-width="130" />
        </el-table>
        <el-alert v-if="duplicateAliases.length" type="error" :closable="false" show-icon class="alias-alert">
          <template #title>输出别名重复：{{ duplicateAliases.join(', ') }}</template>
        </el-alert>
      </section>
    </div>

    <el-collapse v-if="modelValue.sqlTemplate || modelValue.previewRows.length" class="preview-collapse">
      <el-collapse-item title="生成 SQL">
        <pre class="sql-preview">{{ modelValue.sqlTemplate || '尚未生成 SQL' }}</pre>
      </el-collapse-item>
      <el-collapse-item v-if="modelValue.previewRenderedSql" title="预览执行 SQL">
        <pre class="sql-preview">{{ modelValue.previewRenderedSql }}</pre>
      </el-collapse-item>
      <el-collapse-item v-if="modelValue.previewRows.length" title="样例数据">
        <el-table :data="modelValue.previewRows" border size="small">
          <el-table-column v-for="key in previewColumns" :key="key" :prop="key" :label="key" min-width="120" />
        </el-table>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDatasourceSchemas } from '@/api/datasource'
import { previewSql, renderQueryDesign } from '@/api/api-definition'
import type { SchemaColumn, SchemaForeignKey, SchemaTable } from '@/types/datasource'
import type { QueryDesignJoin } from '@/types/api-definition'
import { normalizeResponseFieldTypes } from '@/utils/api-field-type'

const props = defineProps<{ modelValue: any }>()
const loading = ref(false)
const rendering = ref(false)
const previewing = ref(false)
const tableKeyword = ref('')
const inspectTable = ref<SchemaTable>()

const design = computed(() => props.modelValue.queryDesign)
const tables = computed<SchemaTable[]>(() => props.modelValue.schemas?.[0]?.tables || [])
const filteredTables = computed(() => {
  const keyword = tableKeyword.value.trim().toLowerCase()
  if (!keyword) return tables.value
  return tables.value.filter((table) => table.name.toLowerCase().includes(keyword))
})
const joinableTables = computed(() => tables.value.filter((table) => table.name !== design.value.mainTable))
const joinedTables = computed(() => {
  const items: Array<{ tableName: string; alias: string; role: string }> = []
  if (design.value.mainTable) {
    items.push({ tableName: design.value.mainTable, alias: design.value.mainAlias || 't1', role: '主表' })
  }
  design.value.joins.forEach((join: QueryDesignJoin) => {
    if (join.table && join.alias) items.push({ tableName: join.table, alias: join.alias, role: join.type })
  })
  return items
})
const joinedFields = computed(() => {
  return joinedTables.value.flatMap((item) => {
    const table = findTable(item.tableName)
    return (table?.columns || []).map((col) => ({
      tableName: item.tableName,
      tableAlias: item.alias,
      field: col.name,
      type: col.type,
      comment: col.comment || '',
      key: `${item.alias}.${col.name}`,
    }))
  })
})
const joinFieldOptions = computed(() => joinedFields.value.map((field) => ({
  label: `${field.tableAlias}.${field.field} (${field.tableName})`,
  value: `${field.tableAlias}.${field.field}`,
})))
const duplicateAliases = computed(() => {
  const aliases = design.value.selectFields.map((field: any) => field.alias).filter(Boolean)
  return aliases.filter((alias: string, index: number) => aliases.indexOf(alias) !== index)
})
const previewColumns = computed(() => Object.keys(props.modelValue.previewRows?.[0] || {}))

onMounted(loadSchema)
watch(() => props.modelValue.datasourceId, loadSchema)

async function loadSchema() {
  if (!props.modelValue.datasourceId) return
  if (props.modelValue.schemas?.length) {
    inspectTable.value = tables.value[0]
    return
  }
  loading.value = true
  try {
    const data = await getDatasourceSchemas(props.modelValue.datasourceId)
    props.modelValue.schemas = data.schemas || []
    inspectTable.value = tables.value[0]
  } finally {
    loading.value = false
  }
}

function findTable(name: string) {
  return tables.value.find((table) => table.name === name)
}

function isPk(col: SchemaColumn) {
  return Boolean(col.pk || col.primaryKey)
}

function isFk(tableName: string, columnName: string) {
  return tables.value.some((table) => (table.foreignKeys || []).some((fk) => fk.tableName === tableName && fk.columnName === columnName))
}

function hasIndex(table: SchemaTable, columnName: string) {
  return (table.indexes || []).some((index) => index.columnName === columnName)
}

function handleMainTableChange() {
  design.value.mainAlias = design.value.mainAlias || 't1'
  design.value.joins = []
  design.value.selectFields = []
  design.value.filters = []
  props.modelValue.params = []
  props.modelValue.responseFields = []
  props.modelValue.sqlTemplate = ''
  syncSelectedColumns()
}

function addJoin() {
  design.value.joins.push({
    type: 'LEFT',
    table: '',
    alias: `t${design.value.joins.length + 2}`,
    conditions: [{ leftField: '', operator: '=', rightField: '' }],
  })
}

function removeJoin(index: number | string) {
  const [removed] = design.value.joins.splice(Number(index), 1)
  if (removed?.alias) {
    design.value.selectFields = design.value.selectFields.filter((field: any) => field.tableAlias !== removed.alias)
    design.value.filters = design.value.filters.filter((field: any) => field.tableAlias !== removed.alias)
  }
  syncSelectedColumns()
}

function removeJoinCondition(join: QueryDesignJoin, index: number | string) {
  join.conditions.splice(Number(index), 1)
}

function handleJoinTableChange(join: QueryDesignJoin) {
  join.alias = join.alias || `t${design.value.joins.indexOf(join) + 2}`
  join.conditions = recommendJoinConditions(join)
}

function recommendJoinConditions(join: QueryDesignJoin) {
  const candidates = getForeignKeyCandidates(join.table)
  if (candidates.length === 0) return [{ leftField: '', operator: '=' as const, rightField: '' }]
  const first = candidates[0]
  return [{ leftField: first.leftField, operator: '=' as const, rightField: first.rightField }]
}

function getForeignKeyCandidates(targetTable: string) {
  const current = joinedTables.value.filter((item) => item.tableName !== targetTable)
  const targetJoin = design.value.joins.find((join: QueryDesignJoin) => join.table === targetTable)
  const targetAlias = targetJoin?.alias || ''
  const candidates: Array<{ leftField: string; rightField: string }> = []

  allForeignKeys().forEach((fk) => {
    current.forEach((item) => {
      if (fk.tableName === targetTable && fk.referencedTableName === item.tableName) {
        candidates.push({
          leftField: `${item.alias}.${fk.referencedColumnName}`,
          rightField: `${targetAlias}.${fk.columnName}`,
        })
      }
      if (fk.referencedTableName === targetTable && fk.tableName === item.tableName) {
        candidates.push({
          leftField: `${item.alias}.${fk.columnName}`,
          rightField: `${targetAlias}.${fk.referencedColumnName}`,
        })
      }
    })
  })
  return candidates
}

function allForeignKeys(): SchemaForeignKey[] {
  return tables.value.flatMap((table) => table.foreignKeys || [])
}

function normalizeAliases() {
  design.value.mainAlias = safeAlias(design.value.mainAlias, 't1')
  design.value.joins.forEach((join: QueryDesignJoin, index: number) => {
    join.alias = safeAlias(join.alias, `t${index + 2}`)
  })
  design.value.selectFields.forEach((field: any) => {
    field.alias = normalizeOutputAlias(field.alias || field.field)
  })
}

function safeAlias(alias: string, fallback: string) {
  const normalized = String(alias || '').trim().replace(/[^A-Za-z0-9_]/g, '')
  return /^[A-Za-z_][A-Za-z0-9_]*$/.test(normalized) ? normalized : fallback
}

function isSelected(row: any) {
  return design.value.selectFields.some((field: any) => field.tableAlias === row.tableAlias && field.field === row.field)
}

function getSelectAlias(row: any) {
  return design.value.selectFields.find((field: any) => field.tableAlias === row.tableAlias && field.field === row.field)?.alias || ''
}

function toggleField(row: any, checked: string | number | boolean) {
  if (checked) {
    if (!isSelected(row)) {
      design.value.selectFields.push({
        tableAlias: row.tableAlias,
        field: row.field,
        alias: uniqueAlias(defaultOutputAlias(row.field)),
        description: row.comment,
        sensitive: false,
      })
    }
  } else {
    design.value.selectFields = design.value.selectFields.filter((field: any) => !(field.tableAlias === row.tableAlias && field.field === row.field))
  }
  syncSelectedColumns()
}

function updateSelectAlias(row: any, alias: string) {
  const field = design.value.selectFields.find((item: any) => item.tableAlias === row.tableAlias && item.field === row.field)
  if (field) {
    field.alias = normalizeOutputAlias(alias)
    const responseField = props.modelValue.responseFields.find((item: any) => item.fieldName === getResponseFieldName(row))
    if (responseField) responseField.alias = field.alias
  }
}

function getResponseFieldName(row: any) {
  return getSelectAlias(row) || row.field
}

function uniqueAlias(base: string) {
  const existing = new Set(design.value.selectFields.map((field: any) => field.alias))
  if (!existing.has(base)) return base
  let index = 2
  while (existing.has(`${base}${index}`)) index++
  return `${base}${index}`
}

function toCamelCase(value: string) {
  return value.replace(/_([a-zA-Z0-9])/g, (_, char: string) => char.toUpperCase())
}

function defaultOutputAlias(field: string) {
  return isPostgresDatasource() ? normalizeOutputAlias(field) : toCamelCase(field)
}

function normalizeOutputAlias(alias: string) {
  const safe = safeAlias(alias, 'field')
  return isPostgresDatasource() ? safe.toLowerCase() : safe
}

function isPostgresDatasource() {
  return String(props.modelValue.datasourceType || '').toUpperCase() === 'POSTGRES'
}

function syncSelectedColumns() {
  props.modelValue.selectedColumns = design.value.selectFields.map((field: any) => {
    const tableItem = joinedTables.value.find((item) => item.alias === field.tableAlias)
    const column = findTable(tableItem?.tableName || '')?.columns.find((col) => col.name === field.field)
    return {
      ...column,
      name: field.field,
      tableName: tableItem?.tableName,
      tableAlias: field.tableAlias,
      _alias: field.alias,
      _selected: true,
    }
  })
  props.modelValue.selectedTableName = design.value.mainTable
}

function sampleParams() {
  const params = Object.fromEntries((props.modelValue.params || []).map((param: any) => [param.name, param.example || param.defaultValue || '']))
  if (props.modelValue.sqlTemplate?.includes('#{size}') && (params.size == null || params.size === '')) params.size = 20
  if (props.modelValue.sqlTemplate?.includes('#{offset}') && (params.offset == null || params.offset === '')) params.offset = 0
  return params
}

async function handleRender() {
  normalizeAliases()
  if (duplicateAliases.value.length) {
    ElMessage.error('输出别名必须唯一')
    return
  }
  if (!design.value.mainTable || design.value.selectFields.length === 0) {
    ElMessage.warning('请先选择主表和输出字段')
    return
  }
  rendering.value = true
  try {
    design.value.datasourceId = props.modelValue.datasourceId
    const rendered = await renderQueryDesign(design.value)
    props.modelValue.sqlTemplate = rendered.sqlTemplate
    props.modelValue.params = rendered.params || []
    props.modelValue.responseFields = normalizeResponseFieldTypes(rendered.responseFields)
    props.modelValue.renderWarnings = rendered.warnings || []
    ElMessage.success('SQL 已生成')
  } finally {
    rendering.value = false
  }
}

async function handlePreview() {
  previewing.value = true
  try {
    await handleRender()
    if (!props.modelValue.sqlTemplate) return
    const result = await previewSql({
      datasourceId: props.modelValue.datasourceId,
      sqlTemplate: props.modelValue.sqlTemplate,
      params: sampleParams(),
      timeoutMs: props.modelValue.timeoutMs,
      maxRows: 20,
    })
    props.modelValue.previewRenderedSql = result.renderedSql
    props.modelValue.previewRows = result.rows || []
    if (result.responseFields?.length) props.modelValue.responseFields = normalizeResponseFieldTypes(result.responseFields)
    props.modelValue.renderWarnings = [...(props.modelValue.renderWarnings || []), ...(result.warnings || [])]
  } finally {
    previewing.value = false
  }
}
</script>

<style lang="scss" scoped>
.join-wizard-step { display: flex; flex-direction: column; gap: 16px; }
.step-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px;
  h4 { margin: 0 0 4px; }
  p { margin: 0; color: #86909C; font-size: 13px; }
}
.toolbar-actions { display: flex; gap: 8px; }
.join-layout { display: grid; grid-template-columns: 260px minmax(360px, 1fr) minmax(420px, 1.1fr); gap: 16px; align-items: start; }
.table-browser, .join-config, .field-picker { min-width: 0; }
.table-list { margin-top: 10px; max-height: 220px; overflow: auto; border: 1px solid #E5E6EB; border-radius: 6px; }
.table-item { width: 100%; border: 0; background: #fff; text-align: left; padding: 8px 10px; cursor: pointer; display: flex; flex-direction: column; gap: 2px;
  &:hover { background: #F7F8FA; }
  &.active { background: #F0F5FF; color: #2878FF; }
  small { color: #86909C; }
}
.column-list { margin-top: 12px; border: 1px solid #E5E6EB; border-radius: 6px; padding: 10px; max-height: 260px; overflow: auto; }
.column-title, .block-title { font-weight: 600; margin-bottom: 10px; }
.column-item { display: flex; align-items: center; gap: 6px; padding: 5px 0; font-size: 13px; }
.column-name { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.config-block { margin-bottom: 16px; }
.block-heading { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.form-row, .join-row, .condition-row { display: flex; align-items: center; gap: 8px; }
.join-row { margin-bottom: 8px; }
.condition-row { margin: 6px 0 6px 24px; }
.join-item { border: 1px solid #E5E6EB; border-radius: 6px; padding: 10px; margin-bottom: 10px; }
.join-type { width: 92px; flex: none; }
.alias-input { width: 90px; flex: none; }
.empty-tip { color: #86909C; font-size: 13px; padding: 12px 0; }
.alias-alert { margin-top: 10px; }
.preview-collapse { margin-top: 4px; }
.sql-preview { margin: 0; padding: 12px; background: #F7F8FA; border: 1px solid #E5E6EB; border-radius: 6px; white-space: pre-wrap; font-family: Consolas, monospace; font-size: 12px; line-height: 1.5; }
@media (max-width: 1280px) {
  .join-layout { grid-template-columns: 240px 1fr; }
  .field-picker { grid-column: 1 / -1; }
}
</style>
