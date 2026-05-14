<template>
  <el-drawer :model-value="visible" @update:model-value="emit('update:visible', $event)"
    :title="`Schema · ${datasource.name}`" direction="rtl" size="520px">
    <div class="schema-content" v-loading="loading">
      <div class="schema-toolbar">
        <el-input v-model="keyword" placeholder="搜索表名" clearable prefix-icon="Search" />
        <el-tag type="info" size="small">{{ filteredTables.length }} 张表</el-tag>
      </div>
      <EmptyState v-if="filteredTables.length === 0" description="未找到表" />
      <div v-for="table in filteredTables" :key="table.name" class="table-item">
        <div class="table-header" @click="toggleTable(table.name)">
          <el-icon><CaretRight v-if="!expandedTables.has(table.name)" /><CaretBottom v-else /></el-icon>
          <span class="table-name">{{ table.name }}</span>
          <span v-if="table.comment" class="table-comment">{{ table.comment }}</span>
        </div>
        <div v-if="expandedTables.has(table.name)" class="columns-list">
          <div v-for="col in table.columns" :key="col.name" class="column-item">
            <span class="col-name">{{ col.name }}</span>
            <el-tag size="small" type="info">{{ col.type }}</el-tag>
            <el-tag v-if="col.primaryKey" size="small" type="warning">PK</el-tag>
            <span class="col-null">{{ col.nullable ? 'NULL' : 'NOT NULL' }}</span>
          </div>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { getDatasourceSchemas } from '@/api/datasource'
import EmptyState from '@/components/common/EmptyState.vue'
import type { SchemaTable } from '@/types/datasource'

const props = defineProps<{ visible: boolean; datasource: { id: number; name: string } }>()
const emit = defineEmits<{ 'update:visible': [val: boolean] }>()

const loading = ref(false)
const keyword = ref('')
const tables = ref<SchemaTable[]>([])
const expandedTables = ref(new Set<string>())

const filteredTables = computed(() => {
  if (!keyword.value) return tables.value
  return tables.value.filter(t => t.name.toLowerCase().includes(keyword.value.toLowerCase()))
})

function toggleTable(name: string) {
  if (expandedTables.value.has(name)) expandedTables.value.delete(name)
  else expandedTables.value.add(name)
}

watch(() => props.visible, async (val) => {
  if (val && props.datasource.id) {
    loading.value = true
    try {
      const data = await getDatasourceSchemas(props.datasource.id)
      tables.value = data.schemas?.[0]?.tables || []
    } finally { loading.value = false }
  }
})
</script>

<style lang="scss" scoped>
.schema-toolbar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
.table-item {
  margin-bottom: 4px;
  .table-header {
    display: flex; align-items: center; gap: 8px; padding: 10px 8px;
    cursor: pointer; border-radius: 4px;
    &:hover { background: #f7f8fa; }
    .table-name { font-weight: 600; font-size: 15px; }
    .table-comment { font-size: 13px; color: #86909C; }
  }
  .columns-list {
    padding-left: 28px;
    .column-item {
      display: flex; align-items: center; gap: 8px;
      padding: 6px 0; font-size: 14px;
      .col-name { min-width: 120px; }
      .col-null { font-size: 12px; color: #C9CDD4; }
    }
  }
}
</style>
