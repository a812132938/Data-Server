export interface TrendItem {
  date: string
  total?: number
  count?: number
}

export interface StatusDistItem {
  statusGroup?: string
  status?: string
  count: number
  ratio?: number
  percent?: number
}

export interface TopApiItem {
  apiId: number
  apiName?: string
  name?: string
  total?: number
  count?: number
  rank?: number
}

export interface TopAppItem {
  appId: number
  appName?: string
  name?: string
  total?: number
  count?: number
  rank?: number
}
