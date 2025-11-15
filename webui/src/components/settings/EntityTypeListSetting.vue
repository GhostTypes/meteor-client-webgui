<template>
  <div class="setting-item">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
      <span class="count">{{ selectedEntities.length }} selected</span>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <!-- Search and Filter Controls -->
    <div class="controls">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Search entities..."
        class="search-input"
      />
      <select v-model="selectedNamespace" class="namespace-filter">
        <option value="">All Namespaces</option>
        <option v-for="ns in namespaces" :key="ns" :value="ns">
          {{ ns }}
        </option>
      </select>
    </div>

    <!-- Selected Entities -->
    <div v-if="selectedEntities.length > 0" class="selected-items">
      <div v-for="entityId in selectedEntities" :key="entityId" class="item-chip">
        <span>{{ formatEntityName(entityId) }}</span>
        <button @click="removeEntity(entityId)" class="remove-button">×</button>
      </div>
    </div>

    <!-- Available Entities -->
    <div v-if="!wsStore.registries?.entities" class="loading">
      Loading entities...
    </div>
    <div v-else class="available-items">
      <div class="available-header">Available Entities</div>
      <div class="available-list">
        <div
          v-for="entity in filteredEntities"
          :key="entity.id"
          @click="addEntity(entity.id)"
          class="available-item"
          :class="{ selected: selectedEntities.includes(entity.id) }"
        >
          <span>{{ formatEntityName(entity.id) }}</span>
          <span class="namespace-badge">{{ entity.namespace }}</span>
        </div>
      </div>
      <div v-if="filteredEntities.length === 0" class="no-results">
        No entities found
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { ModuleInfo, SettingMetadata } from '../../stores/modules'
import { useWebSocketStore } from '../../stores/websocket'

const props = defineProps<{
  module: ModuleInfo
  setting: SettingMetadata
}>()

const wsStore = useWebSocketStore()

// Request entities registry on mount if not already loaded
onMounted(() => {
  if (!wsStore.registries?.entities) {
    wsStore.requestRegistry('entities')
  }
})
const searchQuery = ref('')
const selectedNamespace = ref('')

const selectedEntities = computed(() => props.setting.value.items || [])

const allEntities = computed(() => {
  return wsStore.registries?.entities?.entities || []
})

const namespaces = computed(() => {
  const ns = new Set(allEntities.value.map(e => e.namespace))
  return Array.from(ns).sort()
})

const filteredEntities = computed(() => {
  let entities = allEntities.value

  // Filter by namespace
  if (selectedNamespace.value) {
    entities = entities.filter(e => e.namespace === selectedNamespace.value)
  }

  // Filter by search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    entities = entities.filter(e => e.id.toLowerCase().includes(query))
  }

  // Sort: selected first, then alphabetically
  return entities.sort((a, b) => {
    const aSelected = selectedEntities.value.includes(a.id)
    const bSelected = selectedEntities.value.includes(b.id)
    if (aSelected && !bSelected) return -1
    if (!aSelected && bSelected) return 1
    return a.id.localeCompare(b.id)
  })
})

function formatEntityName(id: string) {
  const name = id.split(':')[1] || id
  return name.replace(/_/g, ' ')
}

function addEntity(entityId: string) {
  if (selectedEntities.value.includes(entityId)) return

  const updated = [...selectedEntities.value, entityId]
  updateValue(updated)
}

function removeEntity(entityId: string) {
  const updated = selectedEntities.value.filter(id => id !== entityId)
  updateValue(updated)
}

function updateValue(items: string[]) {
  wsStore.send({
    type: 'setting.update',
    data: {
      moduleName: props.module.name,
      settingName: props.setting.name,
      value: { items }
    }
  })
}
</script>

<style scoped>
.count {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  padding: 0.25rem 0.5rem;
  background: var(--color-background-soft);
  border-radius: 4px;
}

.controls {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.search-input {
  flex: 1;
  padding: 0.5rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  color: var(--color-text);
  font-size: 0.875rem;
}

.search-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.namespace-filter {
  padding: 0.5rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  color: var(--color-text);
  font-size: 0.875rem;
  cursor: pointer;
  min-width: 150px;
}

.namespace-filter:focus {
  outline: none;
  border-color: var(--color-primary);
}

.selected-items {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.75rem;
  padding: 0.75rem;
  background: var(--color-background-soft);
  border-radius: 4px;
  max-height: 150px;
  overflow-y: auto;
}

.item-chip {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem 0.625rem;
  background: var(--color-primary);
  color: white;
  border-radius: 4px;
  font-size: 0.875rem;
}

.remove-button {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  cursor: pointer;
  font-size: 1.25rem;
  line-height: 1;
  padding: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s;
}

.remove-button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.available-items {
  margin-top: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  overflow: hidden;
}

.available-header {
  padding: 0.5rem;
  background: var(--color-background-soft);
  border-bottom: 1px solid var(--color-border);
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text-secondary);
  text-transform: uppercase;
}

.available-list {
  max-height: 300px;
  overflow-y: auto;
}

.available-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  cursor: pointer;
  border-bottom: 1px solid var(--color-border);
  transition: background 0.15s;
}

.available-item:last-child {
  border-bottom: none;
}

.available-item:hover {
  background: var(--color-background-soft);
}

.available-item.selected {
  background: rgba(var(--color-primary-rgb), 0.1);
  color: var(--color-primary);
}

.namespace-badge {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  padding: 0.125rem 0.375rem;
  background: var(--color-background-mute);
  border-radius: 3px;
  font-family: monospace;
}

.no-results {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
}

.loading {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  font-style: italic;
}
</style>
