<template>
  <div>
    <h4 style="margin-bottom:12px">参数校验配置</h4>
    <el-table :data="modelValue.params" border>
      <el-table-column prop="name" label="参数名" width="120" />
      <el-table-column prop="dataType" label="类型" width="100" />
      <el-table-column label="必填" width="70">
        <template #default="{ row }"><el-switch v-model="row.required" size="small" /></template>
      </el-table-column>
      <el-table-column label="校验正则" min-width="140">
        <template #default="{ row }"><el-input v-model="row.validateRule" size="small" placeholder="正则表达式" /></template>
      </el-table-column>
      <el-table-column label="最小值" width="100">
        <template #default="{ row }"><el-input v-model="row.validateMin" size="small" /></template>
      </el-table-column>
      <el-table-column label="最大值" width="100">
        <template #default="{ row }"><el-input v-model="row.validateMax" size="small" /></template>
      </el-table-column>
      <el-table-column label="格式说明" min-width="140">
        <template #default="{ row }"><el-input v-model="row.formatDesc" size="small" /></template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
defineProps<{ modelValue: any }>()
</script>
