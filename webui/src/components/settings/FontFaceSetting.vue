<template>
  <div class="setting-item">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
      <span class="current-font">{{ setting.value.label || 'Default' }}</span>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <div v-if="families.length === 0" class="info-message">
      Font metadata unavailable.
    </div>

    <div v-else class="font-controls">
      <label>Family</label>
      <select v-model="selectedFamily" class="select-input">
        <option v-for="family in families" :key="family.name" :value="family.name">
          {{ family.name }}
        </option>
      </select>

      <label>Style</label>
      <select v-model="selectedType" class="select-input">
        <option v-for="type in availableTypes" :key="type" :value="type">
          {{ type }}
        </option>
      </select>

      <div class="button-row">
        <button class="apply" @click="applyFont" :disabled="!selectedFamily || !selectedType">
          Apply
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ModuleInfo, SettingMetadata } from '../../stores/modules'
import { useWebSocketStore } from '../../stores/websocket'

const props = defineProps<{
  module: ModuleInfo
  setting: SettingMetadata
}>()

const wsStore = useWebSocketStore()

type FontFamilyMeta = { name: string; types: string[] }

const families = computed<FontFamilyMeta[]>(() => props.setting.typeMetadata?.families ?? [])
const selectedFamily = ref<string>('')
const selectedType = ref<string>('')

const availableTypes = computed(() => {
  const found = families.value.find(family => family.name === selectedFamily.value)
  return found?.types ?? []
})

watch(
  () => props.setting.value,
  () => hydrateFromValue(),
  { deep: true }
)

watch(selectedFamily, family => {
  if (family && !availableTypes.value.includes(selectedType.value)) {
    selectedType.value = availableTypes.value[0] ?? ''
  }
})

function hydrateFromValue() {
  selectedFamily.value = props.setting.value.family || families.value[0]?.name || ''
  selectedType.value = props.setting.value.type || availableTypes.value[0] || ''
}

hydrateFromValue()

function applyFont() {
  if (!selectedFamily.value || !selectedType.value) return
  wsStore.send({
    type: 'setting.update',
    data: {
      moduleName: props.module.name,
      settingName: props.setting.name,
      value: {
        family: selectedFamily.value,
        type: selectedType.value
      }
    }
  })
}
</script>

<style scoped>
.font-controls {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.select-input {
  padding: 0.5rem;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background: var(--color-background-soft);
  color: var(--color-text);
}

.button-row {
  display: flex;
  gap: 0.5rem;
}

.apply {
  padding: 0.45rem 1rem;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  background: var(--color-primary);
  color: #fff;
}

.info-message {
  padding: 0.5rem;
  background: var(--color-background-soft);
  border-radius: 4px;
  color: var(--color-text-secondary);
  font-size: 0.85rem;
}

.current-font {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
}
</style>
