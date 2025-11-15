<template>
  <div class="setting-item">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
      <span class="count">{{ selectedItems.length }} selected</span>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <!-- Search and Filter Controls -->
    <div class="controls">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Search items..."
        class="search-input"
      />
      <select v-model="selectedNamespace" class="namespace-filter">
        <option value="">All Namespaces</option>
        <option v-for="ns in namespaces" :key="ns" :value="ns">
          {{ ns }}
        </option>
      </select>
    </div>

    <!-- Selected Items -->
    <div v-if="selectedItems.length > 0" class="selected-items">
      <div v-for="itemId in selectedItems" :key="itemId" class="item-chip">
        <span>{{ formatItemName(itemId) }}</span>
        <button @click="removeItem(itemId)" class="remove-button">×</button>
      </div>
    </div>

    <!-- Available Items -->
    <div v-if="!wsStore.registries?.items" class="loading">
      Loading items...
    </div>
    <div v-else class="available-items">
      <div class="available-header">Available Items</div>
      <div class="available-list">
        <div
          v-for="item in filteredItems"
          :key="item.id"
          @click="addItem(item.id)"
          class="available-item"
          :class="{ selected: selectedItems.includes(item.id) }"
        >
          <span>{{ formatItemName(item.id) }}</span>
          <span class="namespace-badge">{{ item.namespace }}</span>
        </div>
      </div>
      <div v-if="filteredItems.length === 0" class="no-results">
        No items found
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

// Request items registry on mount if not already loaded
onMounted(() => {
  if (!wsStore.registries?.items) {
    wsStore.requestRegistry('items')
  }
})
const searchQuery = ref('')
const selectedNamespace = ref('')

const selectedItems = computed(() => props.setting.value.items || [])

const allItems = computed(() => {
  return wsStore.registries?.items?.items || []
})

const namespaces = computed(() => {
  const ns = new Set(allItems.value.map(i => i.namespace))
  return Array.from(ns).sort()
})

const filteredItems = computed(() => {
  let items = allItems.value

  // Filter by namespace
  if (selectedNamespace.value) {
    items = items.filter(i => i.namespace === selectedNamespace.value)
  }

  // Filter by search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    items = items.filter(i => i.id.toLowerCase().includes(query))
  }

  // Sort: selected first, then alphabetically
  return items.sort((a, b) => {
    const aSelected = selectedItems.value.includes(a.id)
    const bSelected = selectedItems.value.includes(b.id)
    if (aSelected && !bSelected) return -1
    if (!aSelected && bSelected) return 1
    return a.id.localeCompare(b.id)
  })
})

function formatItemName(id: string) {
  const name = id.split(':')[1] || id
  return name.replace(/_/g, ' ')
}

function addItem(itemId: string) {
  if (selectedItems.value.includes(itemId)) return

  const updated = [...selectedItems.value, itemId]
  updateValue(updated)
}

function removeItem(itemId: string) {
  const updated = selectedItems.value.filter(id => id !== itemId)
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
