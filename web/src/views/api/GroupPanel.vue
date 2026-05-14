<template>
  <div class="group-panel">
    <div class="group-panel__header">
      <span class="group-panel__title">API 分组</span>
      <el-button class="group-panel__add" text @click="startAdd">
        <el-icon :size="16"><Plus /></el-icon>
      </el-button>
    </div>

    <el-input v-model="searchKey" placeholder="搜索分组..." clearable size="small" class="group-panel__search" />

    <!-- 新增分组 -->
    <div v-if="addingNew" class="group-panel__inline-form">
      <el-input v-model="newName" size="small" placeholder="分组名称" @keyup.enter="handleCreate" ref="addInputRef" />
      <el-button size="small" type="primary" @click="handleCreate">确认</el-button>
      <el-button size="small" @click="addingNew = false; newName = ''">取消</el-button>
    </div>

    <div class="group-panel__list" v-loading="loading">
      <!-- 全部 API -->
      <div class="group-item group-item--all" :class="{ active: selectedGroupId == null }" @click="selectGroup(undefined)">
        <span>全部 API</span>
        <span class="group-item__count">{{ totalCount }}</span>
      </div>

      <!-- 树形分组 -->
      <template v-for="group in filteredGroups" :key="group.id">
        <div
          class="group-item"
          :class="{ active: selectedGroupId === group.id }"
          @click="selectGroup(group.id)"
          @contextmenu.prevent="showContextMenu($event, group)"
        >
          <span v-if="group.children?.length" class="group-item__toggle" @click.stop="toggleExpand(group.id)">
            <el-icon :size="12"><CaretBottom v-if="expandedIds.has(group.id)" /><CaretRight v-else /></el-icon>
          </span>
          <span v-else class="group-item__toggle-placeholder" />

          <template v-if="editingId === group.id">
            <el-input v-model="editName" size="small" style="flex:1" @keyup.enter="handleUpdate(group.id)" @blur="editingId = 0" ref="editInputRef" />
          </template>
          <template v-else>
            <span class="group-item__name">{{ group.name }}</span>
            <span class="group-item__count">{{ group.apiCount ?? '' }}</span>
          </template>
        </div>

        <!-- 子分组 -->
        <template v-if="expandedIds.has(group.id) && group.children?.length">
          <div
            v-for="child in filterChildren(group.children)"
            :key="child.id"
            class="group-item group-item--child"
            :class="{ active: selectedGroupId === child.id }"
            @click="selectGroup(child.id)"
            @contextmenu.prevent="showContextMenu($event, child)"
          >
            <span class="group-item__dot">·</span>
            <template v-if="editingId === child.id">
              <el-input v-model="editName" size="small" style="flex:1" @keyup.enter="handleUpdate(child.id)" @blur="editingId = 0" />
            </template>
            <template v-else>
              <span class="group-item__name">{{ child.name }}</span>
              <span class="group-item__count">{{ child.apiCount ?? '' }}</span>
            </template>
          </div>
        </template>
      </template>

      <!-- 未分组 -->
      <div class="group-item group-item--ungrouped" :class="{ active: selectedGroupId === 0 }" @click="selectGroup(0)">
        <span>未分组</span>
        <span class="group-item__count">{{ ungroupedCount }}</span>
      </div>
    </div>

    <!-- 右键菜单 -->
    <teleport to="body">
      <div v-if="contextMenu.visible" class="group-context-menu" :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }" @click="contextMenu.visible = false">
        <div class="context-menu-item" @click="startEdit(contextMenu.group!)">编辑</div>
        <div class="context-menu-item context-menu-item--danger" @click="handleDelete(contextMenu.group!)">删除</div>
      </div>
    </teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, CaretRight, CaretBottom } from '@element-plus/icons-vue'
import { getApiGroups, createApiGroup, updateApiGroup, deleteApiGroup } from '@/api/api-group'
import type { ApiGroupVO } from '@/types/api-definition'

const props = defineProps<{ selectedGroupId?: number }>()
const emit = defineEmits<{
  'update:selectedGroupId': [val: number | undefined]
  'group-changed': []
}>()

const loading = ref(false)
const groups = ref<ApiGroupVO[]>([])
const searchKey = ref('')
const expandedIds = ref<Set<number>>(new Set())

// CRUD state
const addingNew = ref(false)
const newName = ref('')
const editingId = ref(0)
const editName = ref('')
const addInputRef = ref()

// Context menu
const contextMenu = reactive({ visible: false, x: 0, y: 0, group: null as ApiGroupVO | null })

const totalCount = computed(() => {
  let count = 0
  for (const g of groups.value) {
    count += g.apiCount ?? 0
    if (g.children) for (const c of g.children) count += c.apiCount ?? 0
  }
  return count || ''
})

const ungroupedCount = computed(() => {
  // 如果后端没返回 ungrouped count，显示空
  return ''
})

const filteredGroups = computed(() => {
  if (!searchKey.value) return groups.value
  const key = searchKey.value.toLowerCase()
  return groups.value.filter(g => {
    if (g.name.toLowerCase().includes(key)) return true
    if (g.children?.some(c => c.name.toLowerCase().includes(key))) return true
    return false
  })
})

function filterChildren(children: ApiGroupVO[]) {
  if (!searchKey.value) return children
  const key = searchKey.value.toLowerCase()
  return children.filter(c => c.name.toLowerCase().includes(key))
}

async function fetchGroups() {
  loading.value = true
  try {
    const data = await getApiGroups()
    groups.value = Array.isArray(data) ? data : []
    // 默认展开所有有子节点的分组
    for (const g of groups.value) {
      if (g.children?.length) expandedIds.value.add(g.id)
    }
  } finally {
    loading.value = false
  }
}

function selectGroup(id: number | undefined) {
  emit('update:selectedGroupId', id)
}

function toggleExpand(id: number) {
  if (expandedIds.value.has(id)) {
    expandedIds.value.delete(id)
  } else {
    expandedIds.value.add(id)
  }
}

function startAdd() {
  addingNew.value = true
  newName.value = ''
  nextTick(() => addInputRef.value?.focus())
}

async function handleCreate() {
  const name = newName.value.trim()
  if (!name) return
  await createApiGroup({ name })
  ElMessage.success('创建成功')
  newName.value = ''
  addingNew.value = false
  fetchGroups()
  emit('group-changed')
}

function startEdit(group: ApiGroupVO) {
  editingId.value = group.id
  editName.value = group.name
}

async function handleUpdate(id: number) {
  const name = editName.value.trim()
  if (!name) return
  await updateApiGroup(id, { name })
  ElMessage.success('更新成功')
  editingId.value = 0
  fetchGroups()
  emit('group-changed')
}

async function handleDelete(group: ApiGroupVO) {
  await ElMessageBox.confirm(`确定删除分组 "${group.name}"？`, '确认删除', { type: 'warning' })
  await deleteApiGroup(group.id)
  ElMessage.success('删除成功')
  if (props.selectedGroupId === group.id) {
    emit('update:selectedGroupId', undefined)
  }
  fetchGroups()
  emit('group-changed')
}

function showContextMenu(e: MouseEvent, group: ApiGroupVO) {
  contextMenu.visible = true
  contextMenu.x = e.clientX
  contextMenu.y = e.clientY
  contextMenu.group = group
}

function hideContextMenu() {
  contextMenu.visible = false
}

onMounted(() => {
  fetchGroups()
  document.addEventListener('click', hideContextMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', hideContextMenu)
})

defineExpose({ fetchGroups })
</script>

<style lang="scss" scoped>
.group-panel {
  width: 200px;
  flex-shrink: 0;
  border-right: 1px solid #E5E6EB;
  background: #fff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.group-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 12px 8px;
}

.group-panel__title {
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
}

.group-panel__add {
  padding: 4px;
  height: auto;
}

.group-panel__search {
  margin: 0 12px 8px;
}

.group-panel__inline-form {
  display: flex;
  gap: 4px;
  padding: 0 12px 8px;
}

.group-panel__list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px 12px;
}

.group-item {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  color: #4E5969;
  gap: 4px;

  &:hover { background: #f5f7fa; }
  &.active { background: #e6f0ff; color: #2878FF; font-weight: 500; }
}

.group-item--all {
  font-weight: 500;
  color: #1D2129;
  margin-bottom: 4px;
}

.group-item--child {
  padding-left: 28px;
}

.group-item--ungrouped {
  color: #86909C;
  margin-top: 4px;
}

.group-item__toggle {
  display: flex;
  align-items: center;
  width: 16px;
  flex-shrink: 0;
}

.group-item__toggle-placeholder {
  width: 16px;
  flex-shrink: 0;
}

.group-item__dot {
  font-size: 16px;
  line-height: 1;
  margin-right: 2px;
}

.group-item__name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-item__count {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
  margin-left: auto;
}

.group-context-menu {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border: 1px solid #E5E6EB;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  padding: 4px 0;
  min-width: 80px;
}

.context-menu-item {
  padding: 6px 16px;
  font-size: 13px;
  cursor: pointer;
  color: #4E5969;
  &:hover { background: #f5f7fa; }
}

.context-menu-item--danger {
  color: #F53F3F;
}
</style>
