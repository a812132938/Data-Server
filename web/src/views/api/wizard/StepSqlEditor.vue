<template>
  <div class="sql-editor-step">
    <div class="editor-area">
      <el-alert v-if="hasDollar" title="警告: SQL 中包含 ${} 占位符，请使用 #{} 替代" type="error" :closable="false" show-icon />
      <div class="editor-actions">
        <el-button type="primary" :loading="parsing" @click="handleParse">解析参数</el-button>
        <el-button :loading="previewing" @click="openPreview">预览并推断返回字段</el-button>
      </div>
      <MonacoEditor v-model="modelValue.sqlTemplate" language="sql" height="350px" />
    </div>
    <div class="schema-panel">
      <h4>Schema 浏览</h4>
      <div v-loading="loading" class="schema-tree">
        <div v-for="table in tables" :key="table.name" class="schema-table">
          <div class="table-name" @click="toggleTable(table.name)">
            <el-icon><CaretRight v-if="!expanded.has(table.name)" /><CaretBottom v-else /></el-icon>
            {{ table.name }}
          </div>
          <div v-if="expanded.has(table.name)" class="table-columns">
            <div v-for="col in table.columns" :key="col.name" class="col-item" @click="insertColumn(col.name)">
              <span>{{ col.name }}</span>
              <el-tag size="small" type="info">{{ col.type }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-drawer v-model="previewDrawer" title="SQL 预览" size="560px">
      <div v-if="previewParamNames.length" class="param-form">
        <h4>参数样例值</h4>
        <el-form label-width="120px">
          <el-form-item v-for="name in previewParamNames" :key="name" :label="name">
            <el-input v-model="previewParams[name]" placeholder="用于本次预览的样例值" />
          </el-form-item>
        </el-form>
        <el-button type="primary" :loading="previewing" @click="runPreview">执行预览</el-button>
      </div>
      <template v-if="previewResult">
        <h4>Rendered SQL</h4>
        <pre class="sql-preview">{{ previewResult.renderedSql }}</pre>
        <h4>推断返回字段</h4>
        <el-table :data="previewResult.responseFields" border size="small">
          <el-table-column prop="fieldName" label="字段名" />
          <el-table-column prop="dataType" label="类型" width="100" />
          <el-table-column prop="description" label="描述" />
        </el-table>
        <h4>样例数据</h4>
        <el-table :data="previewResult.rows" border size="small">
          <el-table-column v-for="key in previewColumns" :key="key" :prop="key" :label="key" min-width="120" />
        </el-table>
        <el-alert
          v-for="(warning, index) in previewResult.warnings"
          :key="index"
          :title="warning"
          type="warning"
          :closable="false"
          show-icon
          style="margin-top:8px"
        />
        <el-button type="primary" style="margin-top:12px" @click="applyResponseFields">应用返回字段</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import MonacoEditor from '@/components/editor/MonacoEditor.vue'
import { getDatasourceSchemas } from '@/api/datasource'
import { parseParams } from '@/api/api-param'
import { previewSql } from '@/api/api-definition'
import type { SchemaTable } from '@/types/datasource'
import type { SqlPreviewResp } from '@/types/api-definition'
import { normalizeResponseFieldTypes } from '@/utils/api-field-type'

const props = defineProps<{ modelValue: any }>()
const loading = ref(false)
const parsing = ref(false)
const previewing = ref(false)
const previewDrawer = ref(false)
const previewParamNames = ref<string[]>([])
const previewParams = ref<Record<string, any>>({})
const previewResult = ref<SqlPreviewResp>()
const tables = ref<SchemaTable[]>([])
const expanded = ref(new Set<string>())

const hasDollar = computed(() => props.modelValue.sqlTemplate?.includes('${'))
const previewColumns = computed(() => Object.keys(previewResult.value?.rows?.[0] || {}))

function toggleTable(name: string) {
  if (expanded.value.has(name)) expanded.value.delete(name)
  else expanded.value.add(name)
}

function insertColumn(name: string) {
  props.modelValue.sqlTemplate += `#{${name}}`
}

async function handleParse() {
  const apiId = 0
  if (!apiId && !props.modelValue.sqlTemplate) return
  parsing.value = true
  try {
    const res = await parseParams(apiId, { sqlTemplate: props.modelValue.sqlTemplate })
    props.modelValue.params = res.params || []
    if (res.warnings?.length) ElMessage.warning(res.warnings[0])
  } finally {
    parsing.value = false
  }
}

function openPreview() {
  if (!props.modelValue.datasourceId || !props.modelValue.sqlTemplate) {
    ElMessage.warning('请先选择数据源并填写 SQL')
    return
  }
  previewParamNames.value = extractParamNames(props.modelValue.sqlTemplate)
  previewParams.value = Object.fromEntries(previewParamNames.value.map((name) => {
    const saved = (props.modelValue.params || []).find((param: any) => param.name === name)
    return [name, saved?.example || saved?.defaultValue || '']
  }))
  previewDrawer.value = true
  if (previewParamNames.value.length === 0) runPreview()
}

async function runPreview() {
  previewing.value = true
  try {
    previewResult.value = await previewSql({
      datasourceId: props.modelValue.datasourceId,
      sqlTemplate: props.modelValue.sqlTemplate,
      params: previewParams.value,
      timeoutMs: props.modelValue.timeoutMs,
      maxRows: 20,
    })
  } finally {
    previewing.value = false
  }
}

function applyResponseFields() {
  if (!previewResult.value) return
  props.modelValue.responseFields = normalizeResponseFieldTypes(previewResult.value.responseFields)
  props.modelValue.previewRows = previewResult.value.rows || []
  props.modelValue.previewRenderedSql = previewResult.value.renderedSql
  ElMessage.success('返回字段已应用')
}

function extractParamNames(sql: string) {
  const names = new Set<string>()
  const regex = /#\{\s*([A-Za-z_][A-Za-z0-9_]*)\s*\}/g
  let match = regex.exec(sql)
  while (match) {
    names.add(match[1])
    match = regex.exec(sql)
  }
  return [...names]
}

async function loadSchema() {
  if (!props.modelValue.datasourceId) return
  loading.value = true
  try {
    const data = await getDatasourceSchemas(props.modelValue.datasourceId)
    tables.value = data.schemas?.[0]?.tables || []
  } finally { loading.value = false }
}

onMounted(loadSchema)
watch(() => props.modelValue.datasourceId, loadSchema)
</script>

<style lang="scss" scoped>
.sql-editor-step { display: flex; gap: 16px; }
.editor-area { flex: 1; }
.editor-actions { display: flex; gap: 8px; margin-bottom: 10px; }
.schema-panel { width: 250px; border-left: 1px solid #E5E6EB; padding-left: 16px;
  h4 { margin: 0 0 12px; }
  .table-name { cursor: pointer; padding: 4px 0; font-weight: 500; display: flex; align-items: center; gap: 4px;
    &:hover { color: #2878FF; }
  }
  .col-item { padding: 2px 0 2px 20px; cursor: pointer; display: flex; align-items: center; gap: 6px; font-size: 13px;
    &:hover { color: #2878FF; background: #f7f8fa; }
  }
}
.param-form { margin-bottom: 18px; }
.sql-preview { margin: 0 0 12px; padding: 12px; background: #F7F8FA; border: 1px solid #E5E6EB; border-radius: 6px; white-space: pre-wrap; font-family: Consolas, monospace; font-size: 12px; line-height: 1.5; }
</style>
