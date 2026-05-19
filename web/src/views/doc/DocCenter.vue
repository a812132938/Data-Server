<template>
  <div class="doc-center">
    <div class="doc-body">
      <!-- Left: API Nav Tree -->
      <div class="doc-nav">
        <el-input v-model="searchKey" placeholder="搜索 API" clearable size="small" style="margin-bottom: 12px" />
        <div v-if="loadingTree" class="doc-nav-state">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中</span>
        </div>
        <el-alert
          v-else-if="treeError"
          :title="treeError"
          type="error"
          :closable="false"
          show-icon
          class="doc-nav-alert"
        />
        <el-empty v-else-if="filteredTree.length === 0" description="暂无已发布 API" :image-size="80" />
        <div v-else class="doc-nav-tree">
          <template v-for="group in filteredTree" :key="group.groupId">
            <div class="doc-nav-group" @click="group._collapsed = !group._collapsed">
              <el-icon><CaretRight v-if="group._collapsed" /><CaretBottom v-else /></el-icon>
              <span>{{ group.groupName }}</span>
              <span class="doc-nav-count">({{ group.apis.length }})</span>
            </div>
            <template v-if="!group._collapsed">
              <div
                v-for="api in group.apis"
                :key="api.id"
                class="doc-nav-item"
                :class="{ active: selectedApiId === api.id }"
                @click="selectApi(api.id)"
              >
                <el-tag :color="METHOD_COLORS[api.method]" effect="dark" size="small" class="doc-nav-method">{{ api.method }}</el-tag>
                <span class="doc-nav-name">{{ api.name }}</span>
              </div>
            </template>
          </template>
        </div>
      </div>

      <!-- Middle: Doc Content -->
      <div class="doc-content" v-if="apiDetail">
        <div class="doc-content-header">
          <el-tag :color="METHOD_COLORS[apiDetail.method]" effect="dark" size="large">{{ apiDetail.method }}</el-tag>
          <h2 style="margin-left: 12px">{{ apiDetail.name }}</h2>
        </div>
        <div class="doc-path">{{ apiDetail.path }}</div>
        <p v-if="apiDetail.description" class="doc-desc">{{ apiDetail.description }}</p>

        <h3 style="margin-top: 24px">请求参数</h3>
        <el-table :data="apiDetail.params || []" border size="small" style="margin-top: 8px">
          <el-table-column prop="name" label="参数名" width="120" />
          <el-table-column prop="location" label="来源" width="80" />
          <el-table-column prop="dataType" label="类型" width="80" />
          <el-table-column label="必填" width="60">
            <template #default="{ row }">{{ row.required ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column prop="defaultValue" label="默认值" width="100" />
          <el-table-column prop="example" label="示例" width="100" />
          <el-table-column prop="description" label="描述" />
        </el-table>

        <!-- Try It Out -->
        <div style="margin-top: 24px">
          <el-button type="primary" @click="tryExpanded = !tryExpanded">
            {{ tryExpanded ? '收起' : 'Try it out' }}
          </el-button>
        </div>
        <div v-if="tryExpanded" class="try-panel">
          <el-form label-position="left" label-width="100px" size="small">
            <el-form-item label="AppCode">
              <el-input v-model="tryAppCode" placeholder="输入 AppCode" />
            </el-form-item>
            <el-form-item v-for="p in apiDetail.params" :key="p.name" :label="p.name">
              <el-input v-model="tryParams[p.name]" :placeholder="p.example || ''" />
            </el-form-item>
          </el-form>
          <el-button type="primary" :loading="trying" @click="handleTry" style="margin-top: 8px">发送请求</el-button>
          <div v-if="tryResponse" style="margin-top: 12px">
            <h4>响应</h4>
            <pre class="try-response">{{ tryResponse }}</pre>
          </div>
        </div>
      </div>
      <div v-else class="doc-content doc-empty">
        <el-empty description="请从左侧选择一个 API" />
      </div>

      <!-- Right: Code Panel -->
      <div class="doc-code-panel" v-if="apiDetail">
        <h4>cURL 示例</h4>
        <pre class="code-block">{{ curlExample }}</pre>

        <h4 style="margin-top: 16px">Response 示例</h4>
        <pre class="code-block">{{ responseExample }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CaretRight, CaretBottom, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { callGatewayApi, getDocApiTree, getDocApiDetail } from '@/api/doc'
import { METHOD_COLORS } from '@/utils/constants'
import type { DocApiTreeNode, DocApiDetailVO } from '@/types/doc'

type DocNavGroup = DocApiTreeNode & { _collapsed: boolean }
type RawDocTreeNode = {
  id?: string | number
  apiId?: number
  groupId?: number
  name?: string
  groupName?: string
  method?: string
  path?: string
  type?: string
  apis?: DocApiTreeNode['apis']
  children?: RawDocTreeNode[]
}

const route = useRoute()
const router = useRouter()
const searchKey = ref('')
const tree = ref<DocNavGroup[]>([])
const loadingTree = ref(false)
const treeError = ref('')
const selectedApiId = ref(0)
const apiDetail = ref<DocApiDetailVO | null>(null)
const tryExpanded = ref(false)
const tryAppCode = ref('')
const tryParams = ref<Record<string, string>>({})
const trying = ref(false)
const tryResponse = ref('')

const filteredTree = computed(() => {
  if (!searchKey.value) return tree.value
  const key = searchKey.value.toLowerCase()
  return tree.value
    .map(g => ({
      ...g,
      apis: g.apis.filter(a => a.name.toLowerCase().includes(key) || a.path?.toLowerCase().includes(key)),
    }))
    .filter(g => g.apis.length > 0)
})

const curlExample = computed(() => {
  if (!apiDetail.value) return ''
  const d = apiDetail.value
  const queryParams = (d.params || []).filter(p => p.location === 'QUERY')
  const paramStr = queryParams
    .map(p => `${encodeURIComponent(p.name)}=${formatQueryValue(p.example || p.defaultValue || '{value}')}`)
    .join('&')
  const url = `${actualApiUrl.value}${paramStr ? '?' + paramStr : ''}`
  return `curl -X ${d.method} '${url}' \\\n  -H 'X-App-Code: {your-app-code}'`
})

const actualApiUrl = computed(() => {
  if (!apiDetail.value) return ''
  const gatewayBaseUrl = getGatewayBaseUrl()
  return joinUrl(gatewayBaseUrl, getGatewayPath(apiDetail.value.path, gatewayBaseUrl))
})

const responseExample = computed(() => {
  if (!apiDetail.value) return ''
  const fields: Record<string, any> = {}
  ;(apiDetail.value.responseFields || []).forEach(f => {
    fields[f.alias || f.fieldName] = `{${f.dataType}}`
  })
  return JSON.stringify({
    code: 0,
    message: 'ok',
    data: [fields],
    traceId: 'tr_1779154000832_v6i2z9',
  }, null, 2)
})

async function fetchTree() {
  loadingTree.value = true
  treeError.value = ''
  try {
    const data = await getDocApiTree()
    tree.value = normalizeDocTree(data)
    // auto-select from route
    if (route.params.id) {
      await selectApi(Number(route.params.id))
    }
  } catch (e: any) {
    tree.value = []
    treeError.value = e.response?.data?.message || e.message || '文档列表加载失败'
  } finally {
    loadingTree.value = false
  }
}

async function selectApi(id: number) {
  selectedApiId.value = id
  tryExpanded.value = false
  tryResponse.value = ''
  try {
    apiDetail.value = await getDocApiDetail(id)
    tryParams.value = {}
    ;(apiDetail.value?.params || []).forEach(p => {
      tryParams.value[p.name] = p.example || p.defaultValue || ''
    })
    router.replace(`/docs/${id}`)
  } catch (e: any) {
    apiDetail.value = null
    ElMessage.error(e.response?.data?.message || e.message || '文档详情加载失败')
  }
}

async function handleTry() {
  if (!tryAppCode.value) {
    ElMessage.warning('请输入 AppCode')
    return
  }
  trying.value = true
  try {
    const requestOptions = buildGatewayTryRequest()
    const res = await callGatewayApi(requestOptions)
    tryResponse.value = typeof res === 'string' ? res : JSON.stringify(res, null, 2)
  } catch (e: any) {
    tryResponse.value = JSON.stringify(e.response?.data || { message: e.message || '请求失败' }, null, 2)
  } finally {
    trying.value = false
  }
}

watch(() => route.params.id, (newId) => {
  if (newId && Number(newId) !== selectedApiId.value) {
    selectApi(Number(newId))
  }
})

function normalizeDocTree(data: unknown): DocNavGroup[] {
  if (!Array.isArray(data)) return []
  const normalized: DocNavGroup[] = []
  const ungroupedApis: DocApiTreeNode['apis'] = []

  const appendGroup = (node: RawDocTreeNode, fallbackIndex: number) => {
    const apis: DocApiTreeNode['apis'] = []
    const children = Array.isArray(node.children) ? node.children : []

    children.forEach(child => {
      if (child.type === 'api' || child.apiId) {
        const apiId = child.apiId ?? Number(String(child.id || '').replace(/^api_/, ''))
        if (Number.isFinite(apiId)) {
          apis.push({
            id: apiId,
            name: child.name || '',
            method: child.method || 'GET',
            path: child.path || '',
          })
        }
      } else {
        appendGroup(child, normalized.length)
      }
    })

    if (Array.isArray(node.apis)) {
      apis.push(...node.apis)
    }

    if (apis.length > 0) {
      normalized.push({
        groupId: node.groupId ?? Number(String(node.id || fallbackIndex).replace(/^group_/, '')),
        groupName: node.groupName || node.name || '未分组',
        apis,
        _collapsed: false,
      })
    }
  }

  ;(data as RawDocTreeNode[]).forEach((node, index) => {
    if (node.type === 'api' || node.apiId) {
      const apiId = node.apiId ?? Number(String(node.id || '').replace(/^api_/, ''))
      if (Number.isFinite(apiId)) {
        ungroupedApis.push({
          id: apiId,
          name: node.name || '',
          method: node.method || 'GET',
          path: node.path || '',
        })
      }
    } else {
      appendGroup(node, index)
    }
  })

  if (ungroupedApis.length > 0) {
    normalized.unshift({
      groupId: 0,
      groupName: '未分组',
      apis: ungroupedApis,
      _collapsed: false,
    })
  }

  return normalized
}

function buildGatewayTryRequest() {
  if (!apiDetail.value) {
    throw new Error('请先选择 API')
  }

  let url = actualApiUrl.value
  const method = apiDetail.value.method.toUpperCase()
  const query: Record<string, any> = {}
  const body: Record<string, any> = {}
  const headers: Record<string, string> = { 'X-App-Code': tryAppCode.value }

  ;(apiDetail.value.params || []).forEach(param => {
    const value = tryParams.value[param.name]
    if (value === undefined || value === '') return

    if (param.location === 'PATH') {
      url = replacePathParam(url, param.name, value)
    } else if (param.location === 'HEADER') {
      headers[param.name] = value
    } else if (param.location === 'BODY') {
      body[param.name] = value
    } else {
      query[param.name] = value
    }
  })

  return {
    url,
    method,
    query,
    body: Object.keys(body).length > 0 ? body : undefined,
    headers,
  }
}

function replacePathParam(url: string, name: string, value: string) {
  const encodedValue = encodeURIComponent(value)
  return url
    .replace(new RegExp(`:${name}(?=/|$)`, 'g'), encodedValue)
    .replace(new RegExp(`\\{${name}\\}`, 'g'), encodedValue)
}

function getGatewayBaseUrl() {
  const gatewayBaseUrl = String(import.meta.env.VITE_GATEWAY_BASE_URL || '').trim()
  if (gatewayBaseUrl) return gatewayBaseUrl
  const apiBaseUrl = String(import.meta.env.VITE_API_BASE_URL || '').trim()
  if (apiBaseUrl) return apiBaseUrl
  return window.location.origin
}

function getGatewayPath(path: string, baseUrl: string) {
  const cleanPath = String(path || '').trim().replace(/^\/+/, '')
  if (!cleanPath) return '/gateway'
  if (baseUrlHasGatewayPrefix(baseUrl)) {
    return `/${cleanPath.replace(/^gateway\/?/i, '')}`
  }
  return cleanPath.startsWith('gateway/') ? `/${cleanPath}` : `/gateway/${cleanPath}`
}

function baseUrlHasGatewayPrefix(baseUrl: string) {
  try {
    return new URL(baseUrl).pathname.replace(/\/+$/, '').endsWith('/gateway')
  } catch {
    return baseUrl.replace(/\/+$/, '').endsWith('/gateway')
  }
}

function joinUrl(baseUrl: string, path: string) {
  return `${baseUrl.replace(/\/+$/, '')}/${path.replace(/^\/+/, '')}`
}

function formatQueryValue(value: string) {
  return value === '{value}' ? value : encodeURIComponent(value)
}

onMounted(fetchTree)
</script>

<style scoped>
.doc-center { display: flex; flex-direction: column; height: 100%; background: #fff; }
.doc-body { display: flex; flex: 1; overflow: hidden; }

.doc-nav {
  width: 220px; flex-shrink: 0; border-right: 1px solid #e8e8e8;
  padding: 16px; overflow-y: auto;
}
.doc-nav-state {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  color: #909399; font-size: 13px; padding: 32px 0;
}
.doc-nav-alert { margin-top: 8px; }
.doc-nav-group {
  display: flex; align-items: center; gap: 4px; padding: 8px 0;
  font-weight: 600; font-size: 13px; cursor: pointer; color: #333;
}
.doc-nav-count { color: #999; font-weight: 400; font-size: 12px; }
.doc-nav-item {
  display: flex; align-items: center; gap: 6px; padding: 6px 8px 6px 20px;
  cursor: pointer; border-radius: 4px; font-size: 13px;
}
.doc-nav-item:hover { background: #f5f7fa; }
.doc-nav-item.active { background: #e6f0ff; color: #2878FF; }
.doc-nav-method { flex-shrink: 0; }
.doc-nav-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.doc-content {
  flex: 1; padding: 24px 32px; overflow-y: auto;
}
.doc-empty { display: flex; align-items: center; justify-content: center; }
.doc-content-header { display: flex; align-items: center; }
.doc-path { color: #666; font-family: monospace; margin-top: 8px; font-size: 14px; }
.doc-desc { color: #999; margin-top: 8px; }

.try-panel {
  margin-top: 16px; padding: 16px; background: #fafafa;
  border: 1px solid #e8e8e8; border-radius: 6px;
}
.try-response {
  background: #1e1e1e; color: #d4d4d4; padding: 16px;
  border-radius: 6px; font-size: 13px; overflow-x: auto;
  white-space: pre-wrap; word-break: break-all; max-height: 300px; overflow-y: auto;
}

.doc-code-panel {
  width: 320px; flex-shrink: 0; background: #1e1e1e;
  color: #d4d4d4; padding: 24px; overflow-y: auto;
}
.doc-code-panel h4 { color: #ccc; font-size: 13px; margin-bottom: 8px; }
.code-block {
  background: #2d2d2d; padding: 12px; border-radius: 6px;
  font-size: 12px; font-family: 'Consolas', monospace;
  white-space: pre-wrap; word-break: break-all;
  line-height: 1.6;
}
</style>
