<template>
  <div class="wizard-page">
    <PageHeader title="新建 API" :breadcrumbs="[{ label: '首页', path: '/' }, { label: 'API 定义', path: '/apis' }, { label: isEditing ? '编辑 API' : '新建 API' }]" />

    <!-- 模式选择弹窗 -->
    <el-dialog v-model="showModeDialog" title="选择创建模式" width="400px" :close-on-click-modal="false" :show-close="false">
      <div class="mode-options">
        <div class="mode-card" :class="{ active: wizardData.createMode === 'SCRIPT' }" @click="wizardData.createMode = 'SCRIPT'">
          <h4>脚本模式</h4>
          <p>手动编写 SQL 语句，适合复杂查询</p>
        </div>
        <div class="mode-card" :class="{ active: wizardData.createMode === 'WIZARD' }" @click="wizardData.createMode = 'WIZARD'">
          <h4>向导模式</h4>
          <p>可视化选择表和字段，自动生成 SQL</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showModeDialog = false" :disabled="!wizardData.createMode">确定</el-button>
      </template>
    </el-dialog>

    <el-steps :active="currentStep" align-center class="wizard-steps">
      <el-step v-for="(s, i) in steps" :key="i" :title="s" />
    </el-steps>

    <div class="wizard-body">
      <StepBasicInfo v-if="currentStepName === 'basic'" v-model="wizardData" />
      <StepDatasource v-else-if="currentStepName === 'datasource'" v-model="wizardData" />
      <StepSqlEditor v-else-if="currentStepName === 'sql'" v-model="wizardData" />
      <StepTableSelect v-else-if="currentStepName === 'table'" v-model="wizardData" />
      <StepParamParse v-else-if="currentStepName === 'parse'" v-model="wizardData" />
      <StepParamConfig v-else-if="currentStepName === 'config'" v-model="wizardData" />
      <StepQueryCondition v-else-if="currentStepName === 'condition'" v-model="wizardData" />
      <StepResponseField v-else-if="currentStepName === 'response'" v-model="wizardData" />
      <StepConfirm v-else-if="currentStepName === 'confirm'" v-model="wizardData" :is-editing="isEditing" />
    </div>

    <div class="wizard-footer">
      <el-button v-if="currentStep > 0" @click="currentStep--">上一步</el-button>
      <el-button @click="handleSaveDraft">保存草稿</el-button>
      <el-button v-if="currentStep < steps.length - 1" type="primary" @click="handleNext">下一步</el-button>
      <el-button v-if="currentStep === steps.length - 1" type="primary" @click="handleSubmit">确认提交</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StepBasicInfo from './wizard/StepBasicInfo.vue'
import StepDatasource from './wizard/StepDatasource.vue'
import StepSqlEditor from './wizard/StepSqlEditor.vue'
import StepTableSelect from './wizard/StepTableSelect.vue'
import StepParamParse from './wizard/StepParamParse.vue'
import StepParamConfig from './wizard/StepParamConfig.vue'
import StepQueryCondition from './wizard/StepQueryCondition.vue'
import StepResponseField from './wizard/StepResponseField.vue'
import StepConfirm from './wizard/StepConfirm.vue'
import { getApi, createApi, updateApi, renderQueryDesign } from '@/api/api-definition'
import type { ApiUpsertReq, QueryDesign } from '@/types/api-definition'
import type { ApiParamItem, ApiResponseFieldItem } from '@/types/api'
import type { SchemaInfo } from '@/types/datasource'
import { normalizeResponseFieldTypes } from '@/utils/api-field-type'

const route = useRoute()
const router = useRouter()
const isEditing = ref(false)
const showModeDialog = ref(false)
const currentStep = ref(0)

const createEmptyQueryDesign = (): QueryDesign => ({
  datasourceId: 0,
  mainTable: '',
  mainAlias: 't1',
  joins: [],
  selectFields: [],
  filters: [],
  sorts: [],
  pagination: true,
})

const wizardData = reactive({
  createMode: '' as string,
  name: '', groupId: undefined as number | undefined, path: '', method: 'GET' as string,
  datasourceId: 0, datasourceType: '', sqlTemplate: '', timeoutMs: 30000,
  cacheEnable: false, cacheTtl: 60, defaultQpsLimit: 100, description: '',
  params: [] as ApiParamItem[], responseFields: [] as ApiResponseFieldItem[],
  selectedTables: [] as any[], selectedColumns: [] as any[], selectedTableName: '',
  schemas: [] as SchemaInfo[],
  queryDesign: createEmptyQueryDesign(),
  renderWarnings: [] as string[],
  previewRows: [] as Record<string, any>[],
  previewRenderedSql: '',
})

const scriptSteps = ['基础信息', '选数据源', '编写SQL', '解析参数', '参数配置', '返回字段', '保存确认']
const wizardSteps = ['基础信息', '选数据源', '选表与字段', '查询条件', '返回字段', '保存确认']

const isScriptMode = computed(() => wizardData.createMode === 'SCRIPT')
const steps = computed(() => isScriptMode.value ? scriptSteps : wizardSteps)

const scriptStepKeys = ['basic', 'datasource', 'sql', 'parse', 'config', 'response', 'confirm']
const wizardStepKeys = ['basic', 'datasource', 'table', 'condition', 'response', 'confirm']
const currentStepName = computed(() => {
  const keys = isScriptMode.value ? scriptStepKeys : wizardStepKeys
  return keys[currentStep.value] || 'basic'
})

onMounted(async () => {
  const editId = route.query.id as string
  if (editId) {
    isEditing.value = true
    const api = await getApi(Number(editId))
    const queryDesign = api.queryDesign?.mainTable ? api.queryDesign : restoreQueryDesignFromSql(api.sqlTemplate, api.responseFields || [])
    Object.assign(wizardData, {
      createMode: api.createMode, name: api.name, groupId: api.groupId, path: api.path,
      method: api.method, datasourceId: api.datasourceId, sqlTemplate: api.sqlTemplate,
      timeoutMs: api.timeoutMs, cacheEnable: api.cacheEnable, cacheTtl: api.cacheTtl,
      defaultQpsLimit: api.defaultQpsLimit, description: api.description,
      params: api.params || [], responseFields: api.responseFields || [],
      queryDesign,
    })
    wizardData.queryDesign.datasourceId = api.datasourceId
  } else {
    showModeDialog.value = true
  }
})

async function renderLatestQueryDesign() {
  wizardData.queryDesign.datasourceId = wizardData.datasourceId
  if (!wizardData.datasourceId || !wizardData.queryDesign.mainTable || wizardData.queryDesign.selectFields.length === 0) {
    ElMessage.warning('请先完成主表、关联关系和输出字段配置')
    throw new Error('query design is incomplete')
  }
  const rendered = await renderQueryDesign(wizardData.queryDesign)
  wizardData.sqlTemplate = rendered.sqlTemplate
  wizardData.params = rendered.params || []
  wizardData.responseFields = normalizeResponseFieldTypes(rendered.responseFields)
  wizardData.renderWarnings = rendered.warnings || []
  return { ...rendered, responseFields: wizardData.responseFields }
}

async function handleNext() {
  const nextStepName = (isScriptMode.value ? scriptStepKeys : wizardStepKeys)[currentStep.value + 1]
  if (nextStepName === 'confirm' && !isScriptMode.value) {
    try {
      await renderLatestQueryDesign()
    } catch {
      return
    }
  }
  currentStep.value++
}

async function buildRequest(): Promise<ApiUpsertReq> {
  if (!isScriptMode.value) {
    await renderLatestQueryDesign()
  }
  return {
    name: wizardData.name, path: wizardData.path, method: wizardData.method as any,
    createMode: (isScriptMode.value ? 'SCRIPT' : 'JOIN_WIZARD') as any,
    datasourceId: wizardData.datasourceId,
    queryDesign: isScriptMode.value ? undefined : wizardData.queryDesign,
    sqlTemplate: wizardData.sqlTemplate, sqlType: 'SELECT', timeoutMs: wizardData.timeoutMs,
    cacheEnable: wizardData.cacheEnable, cacheTtl: wizardData.cacheTtl,
    defaultQpsLimit: wizardData.defaultQpsLimit, description: wizardData.description,
    groupId: wizardData.groupId, params: wizardData.params, responseFields: normalizeResponseFieldTypes(wizardData.responseFields),
  }
}

function restoreQueryDesignFromSql(sqlTemplate: string, responseFields: ApiResponseFieldItem[]): QueryDesign {
  const design = createEmptyQueryDesign()
  const sql = normalizeSql(sqlTemplate)
  const fromMatch = sql.match(/\bFROM\s+([`"\w.]+)\s+([A-Za-z_][A-Za-z0-9_]*)/i)
  if (!fromMatch) return design

  design.mainTable = stripIdentifierQuotes(fromMatch[1])
  design.mainAlias = fromMatch[2]
  design.pagination = /LIMIT\s+#\{\s*size\s*\}|LIMIT\s+\?/i.test(sqlTemplate)

  const joinRegex = /\b(LEFT|INNER)\s+JOIN\s+([`"\w.]+)\s+([A-Za-z_][A-Za-z0-9_]*)\s+ON\s+(.+?)(?=\s+(?:LEFT|INNER)\s+JOIN\s+|\s+WHERE\b|\s+<where\b|\s+ORDER\s+BY\b|\s+LIMIT\b|$)/gi
  let joinMatch = joinRegex.exec(sql)
  while (joinMatch) {
    const conditions = joinMatch[4]
      .split(/\s+AND\s+/i)
      .map((condition) => {
        const conditionMatch = condition.match(/([A-Za-z_][A-Za-z0-9_]*\.[`"\w]+)\s*=\s*([A-Za-z_][A-Za-z0-9_]*\.[`"\w]+)/)
        if (!conditionMatch) return undefined
        return {
          leftField: stripFieldIdentifierQuotes(conditionMatch[1]),
          operator: '=' as const,
          rightField: stripFieldIdentifierQuotes(conditionMatch[2]),
        }
      })
      .filter(Boolean) as QueryDesign['joins'][number]['conditions']
    design.joins.push({
      type: joinMatch[1].toUpperCase() as any,
      table: stripIdentifierQuotes(joinMatch[2]),
      alias: joinMatch[3],
      conditions: conditions.length ? conditions : [{ leftField: '', operator: '=', rightField: '' }],
    })
    joinMatch = joinRegex.exec(sql)
  }

  design.selectFields = restoreSelectFields(sqlTemplate, responseFields)
  return design
}

function restoreSelectFields(sqlTemplate: string, responseFields: ApiResponseFieldItem[]): QueryDesign['selectFields'] {
  const selectMatch = sqlTemplate.match(/\bSELECT\s+([\s\S]+?)\s+\bFROM\b/i)
  if (!selectMatch) return []
  const responseByAlias = new Map(responseFields.map((field) => [field.alias || field.fieldName, field]))
  return splitSelectItems(selectMatch[1]).map((item) => {
    const fieldMatch = item.match(/([A-Za-z_][A-Za-z0-9_]*)\.[`"]?([A-Za-z_][A-Za-z0-9_]*)[`"]?(?:\s+AS\s+|\s+)([`"]?[A-Za-z_][A-Za-z0-9_]*[`"]?)?$/i)
    if (!fieldMatch) return undefined
    const alias = stripIdentifierQuotes(fieldMatch[3] || fieldMatch[2])
    const responseField = responseByAlias.get(alias)
    return {
      tableAlias: fieldMatch[1],
      field: fieldMatch[2],
      alias,
      description: responseField?.description,
      sensitive: responseField?.sensitive || false,
      sensitiveRule: responseField?.sensitiveRule,
    }
  }).filter(Boolean) as QueryDesign['selectFields']
}

function splitSelectItems(selectClause: string) {
  const items: string[] = []
  let current = ''
  let depth = 0
  for (const char of selectClause) {
    if (char === '(') depth++
    if (char === ')') depth = Math.max(0, depth - 1)
    if (char === ',' && depth === 0) {
      items.push(current.trim())
      current = ''
    } else {
      current += char
    }
  }
  if (current.trim()) items.push(current.trim())
  return items
}

function normalizeSql(sql: string) {
  return sql.replace(/\s+/g, ' ').trim()
}

function stripIdentifierQuotes(value: string) {
  return value.replace(/^[`"]|[`"]$/g, '')
}

function stripFieldIdentifierQuotes(value: string) {
  const [alias, field] = value.split('.')
  return `${stripIdentifierQuotes(alias)}.${stripIdentifierQuotes(field)}`
}

async function handleSubmit() {
  try {
    const req = await buildRequest()
    const editId = route.query.id as string
    if (editId) { await updateApi(Number(editId), req) }
    else { await createApi(req) }
    ElMessage.success(isEditing.value ? '更新成功' : '创建成功')
    router.push('/apis')
  } catch {}
}

async function handleSaveDraft() {
  try {
    const req = await buildRequest()
    const editId = route.query.id as string
    if (editId) { await updateApi(Number(editId), req) }
    else { await createApi(req) }
    ElMessage.success('保存成功')
    router.push('/apis')
  } catch {}
}
</script>

<style lang="scss" scoped>
.wizard-page { padding: 20px; }
.wizard-steps { margin-bottom: 24px; }
.wizard-body { min-height: 400px; background: #fff; border-radius: 8px; padding: 24px; }
.wizard-footer { display: flex; justify-content: center; gap: 12px; margin-top: 20px; padding: 16px; background: #fff; border-radius: 8px; }
.mode-options { display: flex; gap: 16px; }
.mode-card { flex: 1; border: 2px solid #E5E6EB; border-radius: 8px; padding: 20px; cursor: pointer; text-align: center;
  &.active { border-color: #2878FF; background: #F0F5FF; }
  &:hover { border-color: #2878FF; }
  h4 { margin: 0 0 8px; } p { margin: 0; font-size: 13px; color: #86909C; }
}
</style>
