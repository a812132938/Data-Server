import type { PageParams, ApiParamItem, ApiResponseFieldItem } from './api'

export type ApiStatus = 'DRAFT' | 'TESTING' | 'PUBLISHED' | 'OFFLINE'
export type ApiMethod = 'GET' | 'POST'
export type ApiCreateMode = 'WIZARD' | 'SCRIPT' | 'JOIN_WIZARD'
export type JoinType = 'LEFT' | 'INNER'
export type FilterOperator = 'EQ' | 'NE' | 'GT' | 'GE' | 'LT' | 'LE' | 'LIKE' | 'IN'
export type SortDirection = 'ASC' | 'DESC'

export interface QueryDesignJoinCondition {
  leftField: string
  operator: '='
  rightField: string
}

export interface QueryDesignJoin {
  type: JoinType
  table: string
  alias: string
  conditions: QueryDesignJoinCondition[]
}

export interface QueryDesignSelectField {
  tableAlias: string
  field: string
  alias: string
  description?: string
  sensitive?: boolean
  sensitiveRule?: string
}

export interface QueryDesignFilter {
  tableAlias: string
  field: string
  operator: FilterOperator
  paramName: string
  required: boolean
  defaultValue?: string
  example?: string
  description?: string
}

export interface QueryDesignSort {
  tableAlias: string
  field: string
  direction: SortDirection
}

export interface QueryDesign {
  datasourceId: number
  mainTable: string
  mainAlias: string
  joins: QueryDesignJoin[]
  selectFields: QueryDesignSelectField[]
  filters: QueryDesignFilter[]
  sorts: QueryDesignSort[]
  pagination: boolean
}

export interface ApiUpsertReq {
  groupId?: number
  name: string
  path: string
  method: ApiMethod
  createMode: ApiCreateMode
  datasourceId: number
  queryDesign?: QueryDesign
  sqlTemplate: string
  sqlType?: 'SELECT'
  timeoutMs: number
  cacheEnable: boolean
  cacheTtl?: number
  defaultQpsLimit: number
  description?: string
  params?: ApiParamItem[]
  responseFields?: ApiResponseFieldItem[]
}

export interface ApiQuery extends PageParams {
  keyword?: string
  status?: ApiStatus
  groupId?: number
}

export interface ApiVO {
  id: number
  groupId: number
  groupName: string
  name: string
  code: string
  path: string
  method: ApiMethod
  version: string
  currentVersionId?: number
  createMode: ApiCreateMode
  datasourceId: number
  queryDesign?: QueryDesign
  datasourceName: string
  sqlTemplate: string
  sqlType: string
  timeoutMs: number
  cacheEnable: boolean
  cacheTtl: number
  defaultQpsLimit: number
  status: ApiStatus
  description: string
  owner: number
  ownerName: string
  params: ApiParamItem[]
  responseFields: ApiResponseFieldItem[]
  createdAt: string
  updatedAt: string
}

export interface ApiSummary {
  total: number
  draft: number
  testing: number
  published: number
  offline: number
}

/** API 分组 */
export interface ApiGroupReq {
  name: string
  parentId?: number
  sort?: number
  description?: string
}

export interface ApiGroupVO {
  id: number
  name: string
  parentId: number
  sort: number
  description: string
  apiCount?: number
  children?: ApiGroupVO[]
}

/** M3 参数解析 */
export interface ParseParamsReq {
  sqlTemplate?: string
}

export interface ParseParamsResp {
  params: ApiParamItem[]
  warnings: string[]
}

export interface QueryDesignRenderResp {
  sqlTemplate: string
  params: ApiParamItem[]
  responseFields: ApiResponseFieldItem[]
  warnings: string[]
}

export interface SqlPreviewReq {
  datasourceId: number
  sqlTemplate: string
  params?: Record<string, any>
  timeoutMs?: number
  maxRows?: number
}

export interface SqlPreviewResp {
  renderedSql: string
  parameterMappings: Array<{ property: string; javaType: string }>
  responseFields: ApiResponseFieldItem[]
  rows: Record<string, any>[]
  warnings: string[]
}

/** M4 测试 */
export interface RunTestReq {
  params: Record<string, any>
}

export interface RunTestResp {
  httpStatus: number
  bizCode: number
  costMs: number
  data: any
  masked: boolean
}

export interface TestCaseReq {
  name: string
  inputParams: Record<string, any>
  assertions?: Record<string, any>
  enabled: boolean
}

export interface TestCaseVO extends TestCaseReq {
  id: number
  lastRunStatus?: string
}

export interface RunAllResult {
  passed: number
  total: number
  results: Array<{
    caseId: number
    status: string
    costMs: number
    errorMsg?: string
  }>
}

/** M5 版本 */
export interface PublishReq {
  changeLog?: string
}

export interface RollbackReq {
  versionId: number
  reason?: string
}

export interface ApiVersionVO {
  id: number
  apiId: number
  versionNo: string
  status: string
  isCurrent: boolean
  changeLog: string
  publishedBy: string
  publishedByName?: string
  publishedAt: string
  paramSummary?: Array<{ name: string; dataType: string; required: number; operator: string; defaultValue: string; location: string }>
  sqlTemplate?: string
  paramCount?: number
  responseFieldCount?: number
  datasourceId?: number
  timeoutMs?: number
  cacheEnable?: boolean
  cacheTtl?: number
  defaultQpsLimit?: number
}
