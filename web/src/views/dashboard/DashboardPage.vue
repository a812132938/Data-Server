<template>
  <div class="page-container">
    <PageHeader
      :breadcrumbs="[{ label: '首页', path: '/' }, { label: '监控仪表盘' }]"
      title="监控仪表盘"
      description="API 网关运行状态总览"
    />

    <KpiCardRow>
      <KpiCard title="今日调用" :value="overview.todayCount" color="#2878FF" />
      <KpiCard title="成功率" :value="overview.successRate + '%'" color="#52C41A" />
      <KpiCard title="平均延迟" :value="overview.avgCost + 'ms'" color="#FAAD14" />
      <KpiCard title="异常数" :value="overview.errorCount" color="#ff4d4f" />
    </KpiCardRow>

    <div class="dashboard-grid">
      <el-card shadow="never" class="chart-card trend-card" v-loading="trendLoading">
        <template #header>
          <div class="card-header">
            <div>
              <span class="card-title">调用趋势</span>
              <span class="card-subtitle">近 {{ trendDays }} 天网关调用量</span>
            </div>
            <el-radio-group v-model="trendRange" size="small" @change="handleRangeChange">
              <el-radio-button value="7d">近7天</el-radio-button>
              <el-radio-button value="30d">近30天</el-radio-button>
            </el-radio-group>
          </div>
        </template>

        <div class="trend-summary">
          <div class="summary-item">
            <span class="summary-label">区间总量</span>
            <strong>{{ formatNumber(trendStats.total) }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-label">日均调用</span>
            <strong>{{ formatNumber(trendStats.average) }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-label">峰值日期</span>
            <strong>{{ trendStats.peakDate }}</strong>
          </div>
        </div>

        <div class="chart-shell">
          <v-chart v-if="hasTrendData" :option="trendOption" autoresize class="trend-chart" />
          <el-empty v-else description="暂无调用趋势数据" :image-size="72" />
        </div>
      </el-card>

      <el-card shadow="never" class="chart-card status-card" v-loading="dashboardLoading">
        <template #header>
          <div class="card-header compact">
            <span class="card-title">状态码分布</span>
            <span class="card-subtitle">按响应状态聚合</span>
          </div>
        </template>

        <div v-if="statusRows.length" class="status-dist-list">
          <div v-for="item in statusRows" :key="item.statusGroup" class="status-dist-item">
            <div class="status-meta">
              <span class="status-label">
                <i :style="{ backgroundColor: getStatusColor(item.statusGroup) }" />
                {{ item.statusGroup }}
                <em>{{ getStatusName(item.statusGroup) }}</em>
              </span>
              <span class="status-count">{{ formatNumber(item.count) }} 次</span>
            </div>
            <div class="status-track">
              <div
                class="status-dist-bar"
                :style="{ width: item.percent + '%', backgroundColor: getStatusColor(item.statusGroup) }"
              />
            </div>
            <div class="status-ratio">{{ formatPercentValue(item.percent) }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无状态码分布数据" :image-size="72" />
      </el-card>

      <el-card shadow="never" class="chart-card" v-loading="dashboardLoading">
        <template #header>
          <div class="card-header compact">
            <span class="card-title">TOP API 排行</span>
            <span class="card-subtitle">按调用量排序</span>
          </div>
        </template>

        <div v-if="apiRankRows.length" class="rank-list">
          <div v-for="item in apiRankRows" :key="item.apiId" class="rank-item">
            <span class="rank-badge" :class="getRankClass(item.rank)">{{ item.rank }}</span>
            <div class="rank-main">
              <span class="rank-name" :title="item.apiName">{{ item.apiName }}</span>
              <div class="rank-track">
                <div class="rank-bar" :style="{ width: item.percent + '%' }" />
              </div>
            </div>
            <span class="rank-value">{{ formatNumber(item.total) }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无 TOP API 数据" :image-size="72" />
      </el-card>

      <el-card shadow="never" class="chart-card" v-loading="dashboardLoading">
        <template #header>
          <div class="card-header compact">
            <span class="card-title">TOP 应用排行</span>
            <span class="card-subtitle">按应用调用量排序</span>
          </div>
        </template>

        <div v-if="appRankRows.length" class="rank-list">
          <div v-for="item in appRankRows" :key="item.appId" class="rank-item">
            <span class="rank-badge" :class="getRankClass(item.rank)">{{ item.rank }}</span>
            <div class="rank-main">
              <span class="rank-name" :title="item.appName">{{ item.appName }}</span>
              <div class="rank-track">
                <div class="rank-bar app" :style="{ width: item.percent + '%' }" />
              </div>
            </div>
            <span class="rank-value">{{ formatNumber(item.total) }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无 TOP 应用数据" :image-size="72" />
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import PageHeader from '@/components/common/PageHeader.vue'
import KpiCardRow from '@/components/common/KpiCardRow.vue'
import KpiCard from '@/components/common/KpiCard.vue'
import { getStatsOverview } from '@/api/stats'
import { getDashboardTrend, getStatusDist, getTopApi, getTopApp } from '@/api/dashboard'
import { formatNumber } from '@/utils/format'
import type { TrendItem, StatusDistItem, TopApiItem, TopAppItem } from '@/types/dashboard'

use([BarChart, LineChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

type RecordsPayload<T> = T[] | { records?: T[]; items?: T[] } | null | undefined
type TrendPayload = RecordsPayload<TrendItem> | {
  dates?: string[]
  totalCounts?: Array<number | string>
} | null | undefined
type TrendPoint = { date: string; total: number }
type StatusRow = { statusGroup: string; count: number; percent: number }
type ApiRankRow = { apiId: number; apiName: string; total: number; rank: number; percent: number }
type AppRankRow = { appId: number; appName: string; total: number; rank: number; percent: number }

const overview = ref({ todayCount: 0, successRate: '0', avgCost: 0, errorCount: 0 })
const trendRange = ref<'7d' | '30d'>('7d')
const trendData = ref<TrendItem[]>([])
const statusDist = ref<StatusDistItem[]>([])
const topApis = ref<TopApiItem[]>([])
const topApps = ref<TopAppItem[]>([])
const trendLoading = ref(false)
const dashboardLoading = ref(false)

const trendDays = computed(() => (trendRange.value === '30d' ? 30 : 7))
const hasTrendData = computed(() => trendData.value.length > 0)

const filledTrendData = computed<TrendPoint[]>(() => {
  if (!hasTrendData.value) return []

  const totalsByDate = new Map(
    trendData.value
      .map(item => ({ date: normalizeDate(item.date), total: getTotal(item) }))
      .filter(item => item.date)
      .map(item => [item.date, item.total])
  )

  return buildDateRange(trendDays.value).map(date => ({
    date,
    total: totalsByDate.get(date) ?? 0,
  }))
})

const trendStats = computed(() => {
  const rows = filledTrendData.value
  const total = rows.reduce((sum, item) => sum + item.total, 0)
  const peak = rows.reduce<TrendPoint | null>((current, item) => {
    if (!current || item.total > current.total) return item
    return current
  }, null)

  return {
    total,
    average: rows.length ? Math.round(total / rows.length) : 0,
    peakDate: peak && peak.total > 0 ? dayjs(peak.date).format('MM-DD') : '-',
  }
})

const trendOption = computed(() => {
  const labels = filledTrendData.value.map(item => dayjs(item.date).format('MM-DD'))
  const totals = filledTrendData.value.map(item => item.total)

  return {
    color: ['#2878FF', '#52C41A'],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter(params: any[]) {
        const date = params?.[0]?.axisValueLabel ?? ''
        const value = params?.[0]?.value ?? 0
        return `${date}<br/>调用量：${formatNumber(Number(value))}`
      },
    },
    legend: {
      top: 0,
      right: 0,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: '#86909C' },
    },
    grid: { left: 44, right: 18, top: 42, bottom: 28 },
    xAxis: {
      type: 'category',
      data: labels,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#E5E6EB' } },
      axisLabel: { color: '#86909C', hideOverlap: true },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#86909C', formatter: (value: number) => formatNumber(value) },
      splitLine: { lineStyle: { color: '#F2F3F5' } },
    },
    series: [
      {
        name: '调用量',
        type: 'bar',
        barMaxWidth: 32,
        data: totals,
        itemStyle: { color: '#2878FF', borderRadius: [4, 4, 0, 0] },
      },
      {
        name: '趋势线',
        type: 'line',
        data: totals,
        smooth: totals.length > 2,
        showSymbol: true,
        symbolSize: totals.length === 1 ? 10 : 6,
        lineStyle: { width: 2, color: '#52C41A' },
        itemStyle: { color: '#52C41A' },
      },
    ],
  }
})

const statusRows = computed<StatusRow[]>(() => {
  const total = statusDist.value.reduce((sum, item) => sum + toNumber(item.count), 0)

  return statusDist.value
    .map(item => ({
      statusGroup: getStatusGroup(item),
      count: toNumber(item.count),
      percent: normalizeRatio(item.percent ?? item.ratio, item.count, total),
    }))
    .sort((a, b) => a.statusGroup.localeCompare(b.statusGroup))
})

const apiRankRows = computed<ApiRankRow[]>(() => {
  const max = Math.max(...topApis.value.map(item => getTotal(item)), 0)
  return topApis.value.map((item, index) => ({
    apiId: item.apiId,
    apiName: item.apiName || item.name || `API #${item.apiId}`,
    total: getTotal(item),
    rank: item.rank ?? index + 1,
    percent: getRankPercent(getTotal(item), max),
  }))
})

const appRankRows = computed<AppRankRow[]>(() => {
  const max = Math.max(...topApps.value.map(item => getTotal(item)), 0)
  return topApps.value.map((item, index) => ({
    appId: item.appId,
    appName: item.appName || item.name || `应用 #${item.appId}`,
    total: getTotal(item),
    rank: item.rank ?? index + 1,
    percent: getRankPercent(getTotal(item), max),
  }))
})

function getStatusColor(statusGroup: string) {
  if (statusGroup.startsWith('2')) return '#52c41a'
  if (statusGroup.startsWith('3')) return '#2878FF'
  if (statusGroup.startsWith('4')) return '#faad14'
  return '#ff4d4f'
}

function getStatusName(statusGroup: string) {
  if (statusGroup.startsWith('2')) return '成功'
  if (statusGroup.startsWith('3')) return '重定向'
  if (statusGroup.startsWith('4')) return '客户端异常'
  if (statusGroup.startsWith('5')) return '服务异常'
  return '其他'
}

function getRankClass(rank: number) {
  if (rank === 1) return 'gold'
  if (rank === 2) return 'silver'
  if (rank === 3) return 'bronze'
  return ''
}

function getRankPercent(value: number, max: number) {
  if (max <= 0) return 0
  return Math.max(6, Math.round((toNumber(value) / max) * 100))
}

function formatPercentValue(value: number) {
  if (value >= 10 || Number.isInteger(value)) return `${value.toFixed(0)}%`
  return `${value.toFixed(1)}%`
}

function normalizeRatio(ratio: number | undefined, count: number, total: number) {
  if (Number.isFinite(ratio)) {
    return clampPercent(toNumber(ratio) > 1 ? toNumber(ratio) : toNumber(ratio) * 100)
  }
  if (total <= 0) return 0
  return clampPercent((toNumber(count) / total) * 100)
}

function getTotal(item: { total?: number | string; count?: number | string }) {
  return toNumber(item.total ?? item.count)
}

function getStatusGroup(item: StatusDistItem) {
  return item.statusGroup || item.status || '其他'
}

function clampPercent(value: number) {
  return Math.max(0, Math.min(100, Number(value.toFixed(1))))
}

function toNumber(value: number | string | undefined | null) {
  const num = Number(value)
  return Number.isFinite(num) ? num : 0
}

function normalizeDate(date: string) {
  const parsed = dayjs(date)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD') : ''
}

function buildDateRange(days: number) {
  const end = dayjs()
  return Array.from({ length: days }, (_, index) =>
    end.subtract(days - index - 1, 'day').format('YYYY-MM-DD')
  )
}

function extractRecords<T>(payload: RecordsPayload<T>): T[] {
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.records)) return payload.records
  if (Array.isArray(payload?.items)) return payload.items
  return []
}

function normalizeTrendPayload(payload: TrendPayload): TrendItem[] {
  if (payload && !Array.isArray(payload) && 'dates' in payload && Array.isArray(payload.dates)) {
    return payload.dates.map((date: string, index: number) => ({
      date,
      total: toNumber(payload.totalCounts?.[index]),
    }))
  }
  return extractRecords(payload as RecordsPayload<TrendItem>)
}

async function fetchTrend() {
  trendLoading.value = true
  try {
    const res = await getDashboardTrend(trendRange.value)
    trendData.value = normalizeTrendPayload(res)
  } finally {
    trendLoading.value = false
  }
}

async function fetchAll() {
  dashboardLoading.value = true
  try {
    const [ovResult, distResult, apisResult, appsResult] = await Promise.allSettled([
      getStatsOverview(),
      getStatusDist({ timeRange: trendRange.value }),
      getTopApi({ timeRange: trendRange.value, limit: 10 }),
      getTopApp({ timeRange: trendRange.value, limit: 10 }),
    ])

    if (ovResult.status === 'fulfilled') {
      const ov = ovResult.value
      overview.value = {
        todayCount: ov.todayCount ?? ov.todayCalls ?? 0,
        successRate: String(ov.successRate ?? 0),
        avgCost: ov.avgCost ?? 0,
        errorCount: ov.errorCount ?? 0,
      }
    }
    if (distResult.status === 'fulfilled') statusDist.value = extractRecords(distResult.value)
    if (apisResult.status === 'fulfilled') topApis.value = extractRecords(apisResult.value)
    if (appsResult.status === 'fulfilled') topApps.value = extractRecords(appsResult.value)
  } finally {
    dashboardLoading.value = false
  }
}

function handleRangeChange() {
  fetchTrend()
  fetchAll()
}

onMounted(() => {
  fetchTrend()
  fetchAll()
})
</script>

<style lang="scss" scoped>
.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.65fr);
  gap: 16px;
  margin-top: 20px;
}

.chart-card {
  min-height: 340px;
  border-radius: 8px;
}

.trend-card {
  grid-column: 1 / -1;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.card-header.compact {
  align-items: flex-start;
  flex-direction: column;
  gap: 4px;
}

.card-title {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #1D2129;
}

.card-subtitle {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #86909C;
}

.trend-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.summary-item {
  padding: 12px 14px;
  border: 1px solid #E5E6EB;
  border-radius: 6px;
  background: #FAFBFC;
}

.summary-label {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: #86909C;
}

.summary-item strong {
  font-size: 20px;
  line-height: 1;
  color: #1D2129;
}

.chart-shell {
  height: 280px;
}

.trend-chart {
  width: 100%;
  height: 280px;
}

.chart-shell :deep(.el-empty) {
  height: 100%;
}

.status-dist-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 4px 0;
}

.status-dist-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 58px;
  gap: 8px 12px;
  align-items: center;
}

.status-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  grid-column: 1 / -1;
}

.status-label {
  display: inline-flex;
  align-items: center;
  min-width: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
}

.status-label i {
  width: 8px;
  height: 8px;
  margin-right: 8px;
  border-radius: 50%;
}

.status-label em {
  margin-left: 8px;
  font-style: normal;
  font-size: 12px;
  font-weight: 400;
  color: #86909C;
}

.status-count {
  flex-shrink: 0;
  font-size: 13px;
  color: #4E5969;
}

.status-track,
.rank-track {
  overflow: hidden;
  height: 8px;
  border-radius: 999px;
  background: #F2F3F5;
}

.status-dist-bar,
.rank-bar {
  height: 100%;
  border-radius: inherit;
  transition: width 0.3s ease;
}

.status-ratio {
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: #1D2129;
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 2px 0;
}

.rank-item {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #F2F3F5;
}

.rank-item:last-child {
  border-bottom: none;
}

.rank-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #F2F3F5;
  color: #4E5969;
  font-size: 12px;
  font-weight: 600;
}

.rank-badge.gold {
  background: #FFF7E6;
  color: #D48806;
}

.rank-badge.silver {
  background: #F2F3F5;
  color: #4E5969;
}

.rank-badge.bronze {
  background: #FFF1F0;
  color: #CF1322;
}

.rank-main {
  min-width: 0;
}

.rank-name {
  display: block;
  overflow: hidden;
  margin-bottom: 8px;
  color: #1D2129;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-bar {
  background: #2878FF;
}

.rank-bar.app {
  background: #52C41A;
}

.rank-value {
  font-size: 14px;
  font-weight: 600;
  color: #1D2129;
}

@media (max-width: 960px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .trend-summary {
    grid-template-columns: 1fr;
  }

  .card-header {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
