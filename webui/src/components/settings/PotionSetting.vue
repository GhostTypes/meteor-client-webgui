<template>
  <div class="setting-item">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <select class="select" :value="setting.value.id" @change="onChange">
      <option v-for="option in options" :key="option.id" :value="option.id">
        {{ option.label }}
      </option>
    </select>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ModuleInfo, SettingMetadata } from '../../stores/modules'
import { useWebSocketStore } from '../../stores/websocket'

const props = defineProps<{
  module: ModuleInfo
  setting: SettingMetadata
}>()

const wsStore = useWebSocketStore()

const options = computed(() => {
  return props.setting.typeMetadata?.values ?? []
})

function onChange(event: Event) {
  const select = event.target as HTMLSelectElement
  wsStore.send({
    type: 'setting.update',
    data: {
      moduleName: props.module.name,
      settingName: props.setting.name,
      value: {
        id: select.value
      }
    }
  })
}
</script>

<style scoped>
.select {
  width: 100%;
  padding: 0.5rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: var(--color-surface-2);
  color: var(--color-text);
}
</style>
