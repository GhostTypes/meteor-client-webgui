<template>
  <div class="setting-item">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
      <span class="current-id">{{ setting.value.id || 'Not set' }}</span>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <div v-if="!registryType" class="info-message">
      No registry metadata available for this setting.
    </div>

    <div v-else class="registry-controls">
      <label>Identifier</label>
      <input
        v-model="inputValue"
        :list="datalistId"
        class="text-input"
        placeholder="minecraft:example"
      />

      <datalist :id="datalistId">
        <option
          v-for="entry in registryEntries"
          :key="entry.id"
          :value="entry.id"
        >
          {{ entry.id }}
        </option>
      </datalist>

      <div class="button-row">
        <button class="apply" @click="applyValue" :disabled="!inputValue">Apply</button>
        <button class="load" v-if="needsRegistryLoad" @click="loadRegistry">Load {{ registryType }}</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import type { ModuleInfo, SettingMetadata } from '../../stores/modules'
import { useWebSocketStore } from '../../stores/websocket'

const props = defineProps<{
  module: ModuleInfo
  setting: SettingMetadata
}>()

const wsStore = useWebSocketStore()

const registryType = computed(() => props.setting.typeMetadata?.registry as string | undefined)
const datalistId = computed(() => `${props.module.name}-${props.setting.name}-registry`)
const inputValue = ref(props.setting.value.id || props.setting.value.value || '')

const registryEntries = computed(() => {
  const registries = wsStore.registries
  if (!registryType.value || !registries) return []

  switch (registryType.value) {
    case 'blocks':
      return registries.blocks?.blocks ?? []
    case 'items':
      return registries.items?.items ?? []
    case 'entities':
      return registries.entities?.entities ?? []
    case 'statusEffects':
      return registries.statusEffects ?? []
    case 'modules':
      return registries.modules?.byCategory
        ? Object.values(registries.modules.byCategory)
            .flat()
            .map(entry => ({ id: entry.name, namespace: entry.category }))
        : []
    case 'potions':
      return registries.potions ?? []
    default:
      return []
  }
})

const needsRegistryLoad = computed(() => registryType.value && registryEntries.value.length === 0)

watch(
  () => props.setting.value,
  () => {
    inputValue.value = props.setting.value.id || props.setting.value.value || ''
  },
  { deep: true }
)

function loadRegistry() {
  if (registryType.value) {
    wsStore.requestRegistry(registryType.value)
  }
}

function applyValue() {
  if (!inputValue.value) return
  wsStore.send({
    type: 'setting.update',
    data: {
      moduleName: props.module.name,
      settingName: props.setting.name,
      value: {
        id: inputValue.value,
        value: inputValue.value
      }
    }
  })
}

onMounted(() => {
  if (needsRegistryLoad.value && registryType.value) {
    loadRegistry()
  }
})
</script>

<style scoped>
.registry-controls {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.text-input {
  width: 100%;
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

.apply,
.load {
  padding: 0.45rem 1rem;
  border-radius: 4px;
  border: none;
  cursor: pointer;
}

.apply {
  background: var(--color-primary);
  color: #fff;
}

.load {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
}

.info-message {
  padding: 0.5rem;
  background: var(--color-background-soft);
  border-radius: 4px;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
}

.current-id {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
}
</style>
