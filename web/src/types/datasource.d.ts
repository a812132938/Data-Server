import type { PageParams } from './api'

export type DatasourceType = 'MYSQL' | 'POSTGRES' | 'ORACLE' | 'CLICKHOUSE'
export type DatasourceStatus = 'ENABLED' | 'DISABLED'

export interface DatasourceUpsertReq {
  name: string
  type: DatasourceType
  host: string
  port: number
  databaseName: string
  username: string
  password?: string
  jdbcParams?: string
  poolConfig?: Record<string, any>
  status?: DatasourceStatus
  description?: string
}

export interface DatasourceQuery extends PageParams {
  keyword?: string
  type?: DatasourceType
  status?: DatasourceStatus
  createdBy?: number
}

export interface DatasourceVO {
  id: number
  name: string
  type: DatasourceType
  host: string
  port: number
  databaseName: string
  username: string
  jdbcParams: string
  status: DatasourceStatus
  description: string
  referencedApiCount: number
  createdBy: number
  createdByName: string
  createdAt: string
  updatedAt: string
}

export interface DatasourceSummary {
  total: number
  enabled: number
  disabled: number
  referencedApiCount: number
}

export interface TestConnectionResult {
  success: boolean
  message: string
  costMs: number
}

export interface SchemaColumn {
  name: string
  type: string
  pk?: boolean
  primaryKey: boolean
  nullable: boolean
  comment?: string
}

export interface SchemaForeignKey {
  name: string
  tableName: string
  columnName: string
  referencedSchema?: string
  referencedTableName: string
  referencedColumnName: string
}

export interface SchemaIndex {
  name: string
  columnName: string
  unique: boolean
  primary: boolean
  seq: number
}

export interface SchemaTable {
  name: string
  comment?: string
  columns: SchemaColumn[]
  foreignKeys?: SchemaForeignKey[]
  indexes?: SchemaIndex[]
}

export interface SchemaInfo {
  name: string
  tables: SchemaTable[]
}
