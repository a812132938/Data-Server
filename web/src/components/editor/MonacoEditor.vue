<template>
  <div ref="editorContainer" class="monaco-editor-container" :style="{ height }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as monaco from 'monaco-editor'

const props = withDefaults(defineProps<{
  modelValue: string
  language?: string
  readOnly?: boolean
  height?: string
}>(), {
  language: 'sql',
  readOnly: false,
  height: '400px',
})

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()
const editorContainer = ref<HTMLElement>()
let editor: monaco.editor.IStandaloneCodeEditor | null = null

onMounted(() => {
  if (!editorContainer.value) return
  editor = monaco.editor.create(editorContainer.value, {
    value: props.modelValue,
    language: props.language,
    readOnly: props.readOnly,
    theme: 'vs',
    minimap: { enabled: false },
    wordWrap: 'on',
    scrollBeyondLastLine: false,
    fontSize: 14,
    automaticLayout: true,
  })

  editor.onDidChangeModelContent(() => {
    const value = editor?.getValue() || ''
    if (value !== props.modelValue) {
      emit('update:modelValue', value)
    }
  })
})

watch(() => props.modelValue, (val) => {
  if (editor && val !== editor.getValue()) {
    editor.setValue(val)
  }
})

watch(() => props.readOnly, (val) => {
  editor?.updateOptions({ readOnly: val })
})

onBeforeUnmount(() => {
  editor?.dispose()
})
</script>

<style lang="scss" scoped>
.monaco-editor-container {
  border: 1px solid #E5E6EB;
  border-radius: 4px;
  overflow: hidden;
}
</style>
