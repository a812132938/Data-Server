<template>
  <div>
    <h4>确认信息</h4>
    <el-descriptions :column="2" border style="margin-top:12px">
      <el-descriptions-item label="API 名称">{{ modelValue.name }}</el-descriptions-item>
      <el-descriptions-item label="请求方法">{{ modelValue.method }}</el-descriptions-item>
      <el-descriptions-item label="请求路径">{{ modelValue.path }}</el-descriptions-item>
      <el-descriptions-item label="创建模式">{{ modelValue.createMode === 'SCRIPT' ? '脚本模式' : '向导模式' }}</el-descriptions-item>
      <el-descriptions-item label="超时">{{ modelValue.timeoutMs }}ms</el-descriptions-item>
      <el-descriptions-item label="QPS 限制">{{ modelValue.defaultQpsLimit }}</el-descriptions-item>
      <el-descriptions-item label="缓存">{{ modelValue.cacheEnable ? `开启(${modelValue.cacheTtl}s)` : '关闭' }}</el-descriptions-item>
      <el-descriptions-item label="描述" :span="2">{{ modelValue.description || '-' }}</el-descriptions-item>
    </el-descriptions>

    <h4 style="margin-top:20px">请求参数 ({{ modelValue.params.length }})</h4>
    <el-table :data="modelValue.params" border size="small" style="margin-top:8px">
      <el-table-column prop="name" label="参数名" />
      <el-table-column prop="location" label="来源" width="80" />
      <el-table-column prop="dataType" label="类型" width="80" />
      <el-table-column label="必填" width="60">
        <template #default="{ row }">{{ row.required ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column prop="description" label="描述" />
    </el-table>

    <template v-if="modelValue.createMode !== 'SCRIPT'">
      <h4 style="margin-top:20px">JOIN 配置</h4>
      <el-descriptions :column="2" border style="margin-top:8px">
        <el-descriptions-item label="主表">{{ modelValue.queryDesign.mainTable || '-' }}</el-descriptions-item>
        <el-descriptions-item label="主表别名">{{ modelValue.queryDesign.mainAlias || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分页">{{ modelValue.queryDesign.pagination ? '开启' : '关闭' }}</el-descriptions-item>
        <el-descriptions-item label="输出字段">{{ modelValue.queryDesign.selectFields.length }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="modelValue.queryDesign.joins" border size="small" style="margin-top:8px">
        <el-table-column prop="type" label="JOIN 类型" width="100" />
        <el-table-column prop="table" label="关联表" />
        <el-table-column prop="alias" label="别名" width="80" />
        <el-table-column label="条件">
          <template #default="{ row }">
            <span>{{ formatConditions(row.conditions) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-alert
        v-for="(warning, index) in modelValue.renderWarnings"
        :key="index"
        :title="warning"
        type="warning"
        :closable="false"
        show-icon
        style="margin-top:8px"
      />
    </template>

    <h4 style="margin-top:20px">返回字段 ({{ modelValue.responseFields.length }})</h4>
    <el-table :data="modelValue.responseFields" border size="small" style="margin-top:8px">
      <el-table-column v-if="modelValue.createMode !== 'SCRIPT'" prop="sourceAlias" label="来源别名" width="80" />
      <el-table-column prop="fieldName" label="字段名" />
      <el-table-column prop="alias" label="别名" />
      <el-table-column prop="dataType" label="类型" width="80" />
      <el-table-column label="脱敏" width="60">
        <template #default="{ row }">{{ row.sensitive ? '是' : '否' }}</template>
      </el-table-column>
    </el-table>

    <el-collapse v-if="modelValue.sqlTemplate" style="margin-top:16px">
      <el-collapse-item title="查看生成的 SQL">
        <MonacoEditor :model-value="modelValue.sqlTemplate" read-only height="200px" />
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script setup lang="ts">
import MonacoEditor from '@/components/editor/MonacoEditor.vue'
defineProps<{ modelValue: any; isEditing: boolean }>()

function formatConditions(conditions: any[]) {
  return conditions?.map((condition) => `${condition.leftField} = ${condition.rightField}`).join(' AND ') || '-'
}
</script>
