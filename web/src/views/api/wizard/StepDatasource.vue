<template>
  <div>
    <el-input v-model="keyword" placeholder="搜索数据源" clearable style="width:300px;margin-bottom:16px" />
    <div class="ds-grid">
      <div v-for="ds in filtered" :key="ds.id" class="ds-card"
        :class="{ selected: modelValue.datasourceId === ds.id }"
        @click="selectDatasource(ds.id)">
        <div class="ds-card-header">
          <span class="ds-name">{{ ds.name }}</span>
          <el-tag size="small">{{ ds.type }}</el-tag>
        </div>
        <div class="ds-info">{{ ds.host }}:{{ ds.port }}</div>
        <div class="ds-info">引用 {{ ds.referencedApiCount }} 个 API</div>
        <el-icon v-if="modelValue.datasourceId === ds.id" class="check-icon"><CircleCheckFilled /></el-icon>
      </div>
    </div>
    <EmptyState v-if="filtered.length === 0" description="暂无可用数据源" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getDatasourceList, getDatasourceSchemas } from '@/api/datasource'
import EmptyState from '@/components/common/EmptyState.vue'
import type { DatasourceVO } from '@/types/datasource'

const props = defineProps<{ modelValue: any }>()
const keyword = ref('')
const datasources = ref<DatasourceVO[]>([])
const schemaLoading = ref(false)

const filtered = computed(() => {
  if (!keyword.value) return datasources.value
  return datasources.value.filter(d => d.name.toLowerCase().includes(keyword.value.toLowerCase()))
})

onMounted(async () => {
  const data = await getDatasourceList({ status: 'ENABLED', size: 100 } as any)
  datasources.value = data.records
  syncDatasourceType(props.modelValue.datasourceId)
  if (props.modelValue.datasourceId) await loadSchemas(props.modelValue.datasourceId, false)
})

async function selectDatasource(id: number) {
  if (props.modelValue.datasourceId === id) return
  props.modelValue.datasourceId = id
  syncDatasourceType(id)
  props.modelValue.queryDesign = {
    datasourceId: id,
    mainTable: '',
    mainAlias: 't1',
    joins: [],
    selectFields: [],
    filters: [],
    sorts: [],
    pagination: true,
  }
  props.modelValue.selectedColumns = []
  props.modelValue.selectedTableName = ''
  props.modelValue.params = []
  props.modelValue.responseFields = []
  props.modelValue.sqlTemplate = ''
  await loadSchemas(id, true)
}

function syncDatasourceType(id: number) {
  props.modelValue.datasourceType = datasources.value.find((item) => item.id === id)?.type || ''
}

async function loadSchemas(id: number, force: boolean) {
  if (!id || (!force && props.modelValue.schemas?.length)) return
  schemaLoading.value = true
  try {
    const data = await getDatasourceSchemas(id)
    props.modelValue.schemas = data.schemas || []
  } finally {
    schemaLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.ds-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.ds-card {
  position: relative; border: 2px solid #E5E6EB; border-radius: 8px; padding: 16px; cursor: pointer;
  &:hover { border-color: #2878FF; }
  &.selected { border-color: #2878FF; background: #F0F5FF; }
  .ds-card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
  .ds-name { font-weight: 600; }
  .ds-info { font-size: 13px; color: #86909C; margin-top: 4px; }
  .check-icon { position: absolute; top: 8px; right: 8px; color: #2878FF; font-size: 20px; }
}
</style>
