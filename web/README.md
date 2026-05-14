# 数据服务平台前端

这是数据服务平台的前端工程，用于管理数据源、配置数据 API、维护应用授权、查看调用日志和监控告警。项目基于 Vue 3、TypeScript、Vite 和 Element Plus 构建，面向内部数据服务管理场景。

## 功能概览

| 模块 | 说明 |
| --- | --- |
| 数据源管理 | 维护数据库连接，查看库表结构和字段信息。 |
| API 定义 | 支持 API 分组、基础信息、参数、返回字段、SQL 模板和版本信息管理。 |
| API 创建向导 | 支持脚本模式和向导模式。向导模式可以选择多表、字段、JOIN 关系、查询条件并生成 SQL。 |
| API 详情 | 展示接口基础信息、参数、返回字段、版本、测试和实际访问地址。 |
| 应用管理 | 维护调用方应用、凭证和 API 授权申请。 |
| 授权审批 | 处理应用接口授权审批流程。 |
| 调用日志 | 查看接口调用记录和请求详情。 |
| 监控告警 | 查看统计面板，维护告警规则。 |
| 文档中心 | 面向调用方展示 API 文档。 |

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 前端框架 | Vue 3 |
| 开发语言 | TypeScript |
| 构建工具 | Vite |
| 路由 | Vue Router |
| 状态管理 | Pinia |
| UI 组件库 | Element Plus |
| HTTP 请求 | Axios |
| 图表 | ECharts / vue-echarts |
| SQL 编辑器 | Monaco Editor |
| SQL 格式化 | sql-formatter |

## 目录结构

```text
web/
├── src/
│   ├── api/          # 后端接口请求封装
│   ├── components/   # 通用组件、布局组件、编辑器组件
│   ├── hooks/        # 复用组合式逻辑
│   ├── router/       # 前端路由和登录拦截
│   ├── stores/       # Pinia 状态
│   ├── styles/       # 全局样式和变量
│   ├── types/        # 业务类型声明
│   ├── utils/        # token、格式化、字段类型处理等工具
│   └── views/        # 页面模块
├── public/           # 静态资源
├── .env.development  # 开发环境配置
├── .env.production   # 生产环境配置
├── vite.config.ts    # Vite 配置和本地代理
└── package.json
```

## 环境变量

```env
VITE_API_BASE_URL=
VITE_GATEWAY_BASE_URL=http://172.30.103.105:8081
```

| 变量 | 说明 | 开发环境示例 |
| --- | --- | --- |
| `VITE_API_BASE_URL` | 管理端后端接口地址。为空时，前端通过 Vite 代理访问 `/api/`。 | 空 |
| `VITE_GATEWAY_BASE_URL` | 网关服务地址，用于 API 详情页拼接实际访问地址，也用于本地 `/gateway` 代理。 | `http://172.30.103.105:8081` |

实际访问地址拼接规则：

```text
VITE_GATEWAY_BASE_URL + /gateway + API路径
```

例如 API 路径为 `users/list` 时，开发环境显示：

```text
http://172.30.103.105:8081/gateway/users/list
```

如果 `VITE_GATEWAY_BASE_URL` 已经包含 `/gateway`，页面会避免重复拼接。

## 本地运行

| 场景 | 命令 | 说明 |
| --- | --- | --- |
| 安装依赖 | `npm install` | 首次拉取项目后执行。 |
| 启动开发服务 | `npm run dev` | 默认端口为 `3000`，并允许通过 `172.30.103.105` 访问。 |
| 构建 | `npm run build` | 执行 TypeScript 检查并生成生产构建。 |
| 预览构建产物 | `npm run preview` | 本地预览 `dist` 构建结果。 |

## 本地代理

开发环境下，Vite 代理配置如下：

| 路径 | 转发目标 | 兜底规则 |
| --- | --- | --- |
| `/api/` | `VITE_API_BASE_URL` | 未配置时默认转发到 `http://172.30.103.105:8080`。 |
| `/gateway` | `VITE_GATEWAY_BASE_URL` | 未配置时回退到管理端后端地址。 |

修改 `.env.development` 后需要重启 Vite 开发服务。

## 主要页面

| 路由 | 页面 |
| --- | --- |
| `/login` | 登录页 |
| `/datasources` | 数据源管理 |
| `/apis` | API 定义列表 |
| `/apis/create` | 新建 API 向导 |
| `/apis/:id` | API 详情 |
| `/apps` | 应用管理 |
| `/apps/:id` | 应用详情 |
| `/approvals` | 授权审批 |
| `/logs` | 调用日志 |
| `/dashboard` | 监控仪表盘 |
| `/alerts` | 告警规则 |
| `/docs` | API 文档中心 |
