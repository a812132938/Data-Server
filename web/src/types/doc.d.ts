import type { ApiParamItem, ApiResponseFieldItem } from './api'

export interface DocApiTreeNode {
  groupId: number
  groupName: string
  apis: Array<{
    id: number
    name: string
    method: string
    path: string
  }>
}

export interface DocApiDetailVO {
  id: number
  name: string
  method: string
  path: string
  description: string
  params: ApiParamItem[]
  responseFields: ApiResponseFieldItem[]
  curlExample: string
  responseExample: any
}
