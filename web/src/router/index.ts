import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/token'

NProgress.configure({ showSpinner: false })

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/datasources',
  },
  // 登录页
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/LoginPage.vue'),
    meta: { title: '登录', public: true },
  },
  // 控制台布局
  {
    path: '/',
    component: () => import('@/components/layout/AppLayout.vue'),
    children: [
      {
        path: 'datasources',
        name: 'DatasourceList',
        component: () => import('@/views/datasource/DatasourceList.vue'),
        meta: { title: '数据源管理' },
      },
      {
        path: 'apis',
        name: 'ApiList',
        component: () => import('@/views/api/ApiList.vue'),
        meta: { title: 'API 定义' },
      },
      {
        path: 'apis/create',
        name: 'ApiCreate',
        component: () => import('@/views/api/ApiCreateWizard.vue'),
        meta: { title: '新建 API' },
      },
      {
        path: 'apis/:id',
        name: 'ApiDetail',
        component: () => import('@/views/api/ApiDetail.vue'),
        meta: { title: 'API 详情' },
      },
      {
        path: 'apps',
        name: 'AppList',
        component: () => import('@/views/app/AppList.vue'),
        meta: { title: '应用管理' },
      },
      {
        path: 'apps/:id',
        name: 'AppDetail',
        component: () => import('@/views/app/AppDetail.vue'),
        meta: { title: '应用详情' },
      },
      {
        path: 'approvals',
        name: 'ApprovalList',
        component: () => import('@/views/approval/ApprovalList.vue'),
        meta: { title: '授权审批' },
      },
      {
        path: 'logs',
        name: 'LogList',
        component: () => import('@/views/log/LogList.vue'),
        meta: { title: '调用日志' },
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardPage.vue'),
        meta: { title: '监控仪表盘' },
      },
      {
        path: 'alerts',
        name: 'AlertPage',
        component: () => import('@/views/alert/AlertPage.vue'),
        meta: { title: '告警规则' },
      },
    ],
  },
  // 文档中心独立布局
  {
    path: '/docs',
    component: () => import('@/components/layout/DocLayout.vue'),
    children: [
      {
        path: '',
        name: 'DocCenter',
        component: () => import('@/views/doc/DocCenter.vue'),
        meta: { title: 'API 文档中心', public: true },
      },
      {
        path: ':id',
        name: 'DocDetail',
        component: () => import('@/views/doc/DocCenter.vue'),
        meta: { title: 'API 文档详情', public: true },
      },
    ],
  },
  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFound.vue'),
    meta: { title: '404', public: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  NProgress.start()
  document.title = `${to.meta.title || ''} - 数据服务平台`
  const token = getToken()
  if (!to.meta.public && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && token) {
    next('/')
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
