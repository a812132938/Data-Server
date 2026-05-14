<template>
  <div class="doc-center">
    <!-- Doc TopBar -->
    <div class="doc-topbar">
      <div class="doc-topbar__left">
        <div class="doc-logo">D</div>
        <span class="doc-title">API 文档中心</span>
      </div>
      <router-link to="/datasources" class="doc-back">返回控制台</router-link>
    </div>

    <div class="doc-body">
      <!-- Left: API Nav Tree -->
      <div class="doc-nav">
        <el-input v-model="searchKey" placeholder="搜索 API" clearable size="small" style="margin-bottom: 12px" />
        <div class="doc-nav-tree">
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
                <el-tag :type="METHOD_COLORS[api.method] || 'info'" size="small" class="doc-nav-method">{{ api.method }}</el-tag>
                <span class="doc-nav-name">{{ api.name }}</span>
              </div>
            </template>
          </template>
        </div>
      </div>

      <!-- Middle: Doc Content -->
      <div class="doc-content" v-if="apiDetail">
        <div class="doc-content-header">
          <el-tag :type="METHOD_COLORS[apiDetail.method] || 'info'" size="large">{{ apiDetail.method }}</el-tag>
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
import { CaretRight, CaretBottom } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getDocApiTree, getDocApiDetail, tryDocApi } from '@/api/doc'
import { METHOD_COLORS } from '@/utils/constants'
import type { DocApiTreeNode, DocApiDetailVO } from '@/types/doc'

const route = useRoute()
const router = useRouter()
const searchKey = ref('')
const tree = ref<any[]>([])
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
      apis: g.apis.filter((a: any) => a.name.toLowerCase().includes(key) || a.path?.toLowerCase().includes(key)),
    }))
    .filter(g => g.apis.length > 0)
})

const curlExample = computed(() => {
  if (!apiDetail.value) return ''
  const d = apiDetail.value
  const paramStr = (d.params || []).map(p => `${p.name}=${p.example || '{value}'}`).join('&')
  const url = `http://localhost:8080${d.path}${paramStr ? '?' + paramStr : ''}`
  return `curl -X ${d.method} '${url}' \\\n  -H 'X-App-Code: {your-app-code}'`
})

const responseExample = computed(() => {
  if (!apiDetail.value) return ''
  const fields: Record<string, any> = {}
  ;(apiDetail.value.responseFields || []).forEach(f => {
    fields[f.alias || f.fieldName] = `{${f.dataType}}`
  })
  return JSON.stringify({
    code: 0,
    message: 'success',
    data: { records: [fields], total: 1 },
  }, null, 2)
})

async function fetchTree() {
  const data = await getDocApiTree()
  tree.value = data.map((g: any) => ({ ...g, _collapsed: false }))
  // auto-select from route
  if (route.params.id) {
    selectApi(Number(route.params.id))
  }
}

async function selectApi(id: number) {
  selectedApiId.value = id
  tryExpanded.value = false
  tryResponse.value = ''
  apiDetail.value = await getDocApiDetail(id)
  tryParams.value = {}
  ;(apiDetail.value?.params || []).forEach(p => {
    tryParams.value[p.name] = p.example || p.defaultValue || ''
  })
  router.replace(`/docs/${id}`)
}

async function handleTry() {
  if (!tryAppCode.value) {
    ElMessage.warning('请输入 AppCode')
    return
  }
  trying.value = true
  try {
    const res = await tryDocApi(selectedApiId.value, tryParams.value, tryAppCode.value)
    tryResponse.value = typeof res === 'string' ? res : JSON.stringify(res, null, 2)
  } catch (e: any) {
    tryResponse.value = e.message || '请求失败'
  } finally {
    trying.value = false
  }
}

watch(() => route.params.id, (newId) => {
  if (newId && Number(newId) !== selectedApiId.value) {
    selectApi(Number(newId))
  }
})

onMounted(fetchTree)
</script>

<style scoped>
.doc-center { display: flex; flex-direction: column; height: 100vh; background: #fff; }
.doc-topbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;
}
.doc-topbar__left { display: flex; align-items: center; gap: 12px; }
.doc-logo {
  width: 32px; height: 32px; background: #2878FF; color: #fff;
  border-radius: 6px; display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 18px;
}
.doc-title { font-size: 16px; font-weight: 600; }
.doc-back { color: #2878FF; text-decoration: none; font-size: 14px; }

.doc-body { display: flex; flex: 1; overflow: hidden; }

.doc-nav {
  width: 220px; flex-shrink: 0; border-right: 1px solid #e8e8e8;
  padding: 16px; overflow-y: auto;
}
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
