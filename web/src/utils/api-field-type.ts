import type { ApiResponseFieldItem } from '@/types/api'

export function normalizeApiFieldType(type?: string | null) {
  const raw = (type || '').trim()
  if (!raw) return 'STRING'

  const normalized = raw.toLowerCase()
  if (normalized.includes('bigint') || normalized.includes('bigserial')) return 'LONG'
  if (normalized.includes('int') || normalized.includes('serial')) return 'INT'
  if (
    normalized.includes('decimal') ||
    normalized.includes('numeric') ||
    normalized.includes('double') ||
    normalized.includes('float') ||
    normalized.includes('real')
  ) return 'DECIMAL'
  if (normalized.includes('bool')) return 'BOOL'
  if (normalized.includes('date') || normalized.includes('time')) return 'DATETIME'
  if (
    normalized.includes('char') ||
    normalized.includes('text') ||
    normalized.includes('uuid') ||
    normalized.includes('json')
  ) return 'STRING'

  return raw.length > 16 ? raw.slice(0, 16) : raw
}

export function normalizeResponseFieldTypes(fields?: ApiResponseFieldItem[]) {
  return (fields || []).map((field) => ({
    ...field,
    dataType: normalizeApiFieldType(field.dataType),
  }))
}
