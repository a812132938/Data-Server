<template>
  <div class="page-container">
    <PageHeader :breadcrumb="['首页', '监控仪表盘']" title="监控仪表盘" description="API 网关运行状态总览" />

    <KpiCardRow>
      <KpiCard title="今日调用" :value="overview.todayCount" color="#2878FF" />
      <KpiCard title="成功率" :value="overview.successRate + '%'" color="#52C41A" />
      <KpiCard title="平均延迟" :value="overview.avgCost + 'ms'" color="#FAAD14" />
      <KpiCard title="异常数" :value="overview.errorCount" color="#ff4d4f" />
    </KpiCardRow>

    <div class="chart-grid">
      <!-- Call Trend Chart -->
      <el-card shadow="never" class="chart-card">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span class="card-title">调用趋势</span>
            <el-radio-group v-model="trendRange" size="small" @change="fetchTrend">
              <el-radio-button value="7d">近7天</el-radio-button>
              <el-radio-button value="30d">近30天</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <v-chart :option="trendOption" autoresize style="height: 280px" />
      </el-card>

      <!-- Status Distribution -->
      <el-card shadow="never" class="chart-card">
        <template #header><span class="card-title">状态码分布</span></template>
        <div class="status-dist-list">
          <div v-for="item in statusDist" :key="item.status" class="status-dist-item">
            <div class="status-dist-bar" :style="{ width: item.percent + '%', backgroundColor: getStatusColor(item.status) }" />
            <span class="status-dist-label">{{ item.status }}</span>
            <span class="status-dist-value">{{ item.percent }}%</span>
            <span class="status-dist-count">({{ item.count }})</span>
          </div>
        </div>
      </el-card>

      <!-- Top API -->
      <el-card shadow="never" class="chart-card">
        <template #header><span class="card-title">TOP API 排行</span></template>
        <div class="rank-list">
          <div v-for="(item, i) in topApis" :key="item.apiId" class="rank-item">
            <span class="rank-badge" :class="{ gold: i === 0, silver: i === 1, bronze: i === 2 }">{{ i + 1 }}</span>
            <span class="rank-name">{{ item.apiName }}</span>
            <span class="rank-value">{{ formatNumber(item.callCount) }}</span>
          </div>
          <el-empty v-if="!topApis.length" description="暂无数据" :image-size="60" />
        </div>
      </el-card>

      <!-- Top App -->
      <el-card shadow="never" class="chart-card">
        <template #header><span class="card-title">TOP 应用排行</span></template>
        <div class="rank-list">
          <div v-for="(item, i) in topApps" :key="item.appId" class="rank-item">
            <span class="rank-badge" :class="{ gold: i === 0, silver: i === 1, bronze: i === 2 }">{{ i + 1 }}</span>
            <span class="rank-name">{{ item.appName }}</span>
            <span class="rank-value">{{ formatNumber(item.callCount) }}</span>
          </div>
          <el-empty v-if="!topApps.length" description="暂无数据" :image-size="60" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import PageHeader from '@/components/common/PageHeader.vue'
import KpiCardRow from '@/components/common/KpiCardRow.vue'
import KpiCard from '@/components/common/KpiCard.vue'
import { getStatsOverview } from '@/api/stats'
import { getDashboardTrend, getStatusDist, getTopApi, getTopApp } from '@/api/dashboard'
import { formatNumber } from '@/utils/format'
import type { TrendItem, StatusDistItem, TopApiItem, TopAppItem } from '@/types/dashboard'

use([BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const overview = ref({ todayCount: 0, successRate: '0', avgCost: 0, errorCount: 0 })
const trendRange = ref('7d')
const trendData = ref<TrendItem[]>([])
const statusDist = ref<StatusDistItem[]>([])
const topApis = ref<TopApiItem[]>([])
const topApps = ref<TopAppItem[]>([])

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: trendData.value.map(t => t.date) },
  yAxis: { type: 'value' },
  series: [{
    name: '调用量',
    type: 'bar',
    data: trendData.value.map(t => t.count),
    itemStyle: { color: '#2878FF', borderRadius: [4, 4, 0, 0] },
  }],
  grid: { left: 50, right: 20, top: 20, bottom: 30 },
}))

function getStatusColor(status: string) {
  if (status.startsWith('2')) return '#52c41a'
  if (status.startsWith('3')) return '#2878FF'
  if (status.startsWith('4')) return '#faad14'
  return '#ff4d4f'
}

async function fetchTrend() {
  trendData.value = await getDashboardTrend(trendRange.value)
}

async function fetchAll() {
  const [ov, dist, apis, apps] = await Promise.all([
    getStatsOverview(),
    getStatusDist(),
    getTopApi(),
    getTopApp(),
  ])
  overview.value = {
    todayCount: ov.todayCalls ?? 0,
    successRate: String(ov.successRate ?? 0),
    avgCost: ov.avgCost ?? 0,
    errorCount: ov.errorCount ?? 0,
  }
  statusDist.value = dist
  topApis.value = apis
  topApps.value = apps
}

onMounted(() => { fetchTrend(); fetchAll() })
</script>

<style lang="scss" scoped>
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 20px;
}
.chart-card { min-height: 340px; }
.card-title { font-size: 15px; font-weight: 600; color: #1D2129; }

.status-dist-list { padding: 8px 0; }
.status-dist-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 15px;
}
.status-dist-bar {
  height: 20px;
  border-radius: 4px;
  min-width: 4px;
  transition: width 0.3s;
}
.status-dist-label { font-weight: 600; width: 40px; font-size: 15px; }
.status-dist-value { color: #333; min-width: 48px; text-align: right; font-size: 15px; }
.status-dist-count { color: #999; font-size: 13px; }

.rank-list { padding: 4px 0; }
.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
}
.rank-item:last-child { border-bottom: none; }
.rank-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #e0e0e0;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}
.rank-badge.gold { background: #ffd700; color: #fff; }
.rank-badge.silver { background: #c0c0c0; color: #fff; }
.rank-badge.bronze { background: #cd7f32; color: #fff; }
.rank-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 15px; }
.rank-value { font-weight: 600; color: #333; font-size: 15px; }
</style>
