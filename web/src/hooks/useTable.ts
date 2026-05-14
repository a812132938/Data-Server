import { ref, reactive, onMounted } from 'vue'
import type { PageResp } from '@/types/api'

interface UseTableOptions<T, Q> {
  api: (params: Q) => Promise<PageResp<T>>
  defaultParams?: Partial<Q>
  immediate?: boolean
}

export function useTable<T = any, Q extends Record<string, any> = any>(options: UseTableOptions<T, Q>) {
  const { api, defaultParams = {}, immediate = true } = options

  const loading = ref(false)
  const records = ref<T[]>([]) as any
  const total = ref(0)
  const page = ref(1)
  const size = ref(10)
  const filters = reactive<Record<string, any>>({ ...defaultParams })

  async function fetchData() {
    loading.value = true
    try {
      const params = { ...filters, page: page.value, size: size.value } as Q
      const data = await api(params)
      records.value = data.records
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  function handleSearch() {
    page.value = 1
    fetchData()
  }

  function handleReset() {
    Object.keys(filters).forEach((key) => {
      filters[key] = (defaultParams as any)[key] ?? undefined
    })
    page.value = 1
    fetchData()
  }

  function handlePageChange(val: number) {
    page.value = val
    fetchData()
  }

  function handleSizeChange(val: number) {
    size.value = val
    page.value = 1
    fetchData()
  }

  if (immediate) {
    onMounted(fetchData)
  }

  return {
    loading,
    records,
    total,
    page,
    size,
    filters,
    fetchData,
    handleSearch,
    handleReset,
    handlePageChange,
    handleSizeChange,
  }
}
