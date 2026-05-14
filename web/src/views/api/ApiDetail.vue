<template>
  <div class="api-detail">
    <PageHeader :breadcrumb="['首页', 'API 定义', detail?.name || '']" :title="detail?.name || ''" :description="detail?.path || ''">
      <template #extra>
        <StatusTag :status="detail?.status || ''" :status-map="API_STATUS_MAP" />
      </template>
    </PageHeader>

    <el-tabs v-model="activeTab" style="margin-top: 16px">
      <el-tab-pane label="基本信息" name="info">
        <ApiDetailInfo v-if="detail" :detail="detail" @refresh="fetchDetail" />
      </el-tab-pane>
      <el-tab-pane v-if="detail?.status !== 'DRAFT'" label="测试" name="test">
        <ApiTestPanel v-if="detail" :api-id="id" :detail="detail" />
      </el-tab-pane>
      <el-tab-pane v-if="detail?.status !== 'DRAFT'" label="版本管理" name="versions">
        <VersionPanel v-if="detail" :api-id="id" :detail="detail" @refresh="fetchDetail" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getApi } from '@/api/api-definition'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import ApiDetailInfo from './ApiDetailInfo.vue'
import ApiTestPanel from '@/views/test/ApiTestPanel.vue'
import VersionPanel from '@/views/version/VersionPanel.vue'
import { API_STATUS_MAP } from '@/utils/constants'
import type { ApiVO } from '@/types/api-definition'

const route = useRoute()
const id = ref(Number(route.params.id))
const activeTab = ref((route.query.tab as string) || 'info')
const detail = ref<ApiVO | null>(null)

async function fetchDetail() {
  detail.value = await getApi(id.value)
}

onMounted(fetchDetail)

watch(() => route.params.id, (newId) => {
  if (newId) {
    id.value = Number(newId)
    fetchDetail()
  }
})
</script>

<style scoped>
.api-detail { padding: 0; }
</style>
