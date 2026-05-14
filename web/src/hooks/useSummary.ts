import { ref, onMounted } from 'vue'

export function useSummary<T>(api: () => Promise<T>) {
  const summary = ref<T | null>(null)
  const loading = ref(false)

  async function fetchSummary() {
    loading.value = true
    try {
      summary.value = await api()
    } finally {
      loading.value = false
    }
  }

  onMounted(fetchSummary)

  return { summary, loading, fetchSummary }
}
