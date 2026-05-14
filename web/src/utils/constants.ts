// 数据源类型
export const DATASOURCE_TYPES = [
  { label: 'MySQL', value: 'MYSQL' },
  { label: 'PostgreSQL', value: 'POSTGRES' },
  { label: 'Oracle', value: 'ORACLE' },
  { label: 'ClickHouse', value: 'CLICKHOUSE' },
] as const

// 类型对应默认端口
export const DATASOURCE_DEFAULT_PORTS: Record<string, number> = {
  MYSQL: 3306,
  POSTGRES: 5432,
  ORACLE: 1521,
  CLICKHOUSE: 9000,
}

// API 状态
export const API_STATUS_MAP: Record<string, { label: string; type: string }> = {
  DRAFT: { label: '草稿', type: 'info' },
  TESTING: { label: '测试中', type: 'warning' },
  PUBLISHED: { label: '已发布', type: 'success' },
  OFFLINE: { label: '已下线', type: 'danger' },
}

// 审批状态
export const APPROVAL_STATUS_MAP: Record<string, { label: string; type: string }> = {
  PENDING: { label: '待审批', type: 'warning' },
  APPROVED: { label: '已通过', type: 'success' },
  REJECTED: { label: '已驳回', type: 'danger' },
}

// HTTP 方法颜色
export const METHOD_COLORS: Record<string, string> = {
  GET: '#52C41A',
  POST: '#2878FF',
  PUT: '#FAAD14',
  DELETE: '#FF4D4F',
}

// 告警指标
export const ALERT_METRICS = [
  { label: '错误率', value: 'ERROR_RATE', unit: '%' },
  { label: '平均延迟', value: 'AVG_LATENCY', unit: 'ms' },
  { label: 'QPS', value: 'QPS', unit: '' },
  { label: '失败次数', value: 'FAIL_COUNT', unit: '次' },
] as const

// 告警操作符
export const ALERT_OPERATORS = [
  { label: '>', value: 'GT' },
  { label: '>=', value: 'GE' },
  { label: '<', value: 'LT' },
  { label: '<=', value: 'LE' },
] as const

// 目标类型
export const ALERT_TARGET_TYPES = [
  { label: 'API', value: 'API' },
  { label: '应用', value: 'APP' },
  { label: '全局', value: 'GLOBAL' },
] as const

// 启用/禁用状态
export const STATUS_MAP: Record<string, { label: string; type: string }> = {
  ENABLED: { label: '启用', type: 'success' },
  DISABLED: { label: '禁用', type: 'info' },
}

// 版本状态
export const VERSION_STATUS_MAP: Record<string, { label: string; type: string }> = {
  EFFECTIVE: { label: '生效中', type: 'success' },
  HISTORY: { label: '历史', type: 'info' },
}

// 授权状态
export const AUTH_STATUS_MAP: Record<string, { label: string; type: string }> = {
  EFFECTIVE: { label: '生效中', type: 'success' },
  EXPIRED: { label: '已过期', type: 'info' },
  REVOKED: { label: '已撤销', type: 'danger' },
}
