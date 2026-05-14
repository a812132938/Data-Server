export interface TrendItem {
  date: string
  total: number
}

export interface StatusDistItem {
  statusGroup: string
  count: number
  ratio: number
}

export interface TopApiItem {
  apiId: number
  apiName: string
  total: number
  rank: number
}

export interface TopAppItem {
  appId: number
  appName: string
  total: number
  rank: number
}
