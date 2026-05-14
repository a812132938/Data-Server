import dayjs from 'dayjs'

export function formatTime(time: string | undefined, format = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!time) return '-'
  return dayjs(time).format(format)
}

export function formatNumber(num: number | undefined): string {
  if (num === undefined || num === null) return '0'
  return num.toLocaleString()
}

export function formatPercent(value: number | undefined, digits = 1): string {
  if (value === undefined || value === null) return '0%'
  return `${(value * 100).toFixed(digits)}%`
}

export function formatCost(ms: number | undefined): string {
  if (ms === undefined || ms === null) return '-'
  if (ms < 1000) return `${ms}ms`
  return `${(ms / 1000).toFixed(2)}s`
}

export function truncateStr(str: string, len = 20): string {
  if (!str) return ''
  return str.length > len ? str.slice(0, len) + '...' : str
}
