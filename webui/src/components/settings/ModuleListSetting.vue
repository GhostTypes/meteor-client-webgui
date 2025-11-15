<template>
  <div class="setting-item">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
      <span class="count">{{ selectedModules.length }} selected</span>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <!-- Search and Category Filter Controls -->
    <div class="controls">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="Search modules..."
        class="search-input"
      />
      <select v-model="selectedCategory" class="category-filter">
        <option value="">All Categories</option>
        <option v-for="cat in categories" :key="cat" :value="cat">
          {{ formatCategoryName(cat) }}
        </option>
      </select>
    </div>

    <!-- Selected Modules -->
    <div v-if="selectedModules.length > 0" class="selected-items">
      <div v-for="moduleName in selectedModules" :key="moduleName" class="item-chip">
        <span>{{ getModuleTitle(moduleName) }}</span>
        <button @click="removeModule(moduleName)" class="remove-button">×</button>
      </div>
    </div>

    <!-- Available Modules -->
    <div v-if="!wsStore.registries?.modules" class="loading">
      Loading modules...
    </div>
    <div v-else class="available-items">
      <div class="available-header">Available Modules</div>
      <div class="available-list">
        <div
          v-for="module in filteredModules"
          :key="module.name"
          @click="addModule(module.name)"
          class="available-item"
          :class="{ selected: selectedModules.includes(module.name) }"
        >
          <div class="module-info">
            <span class="module-title">{{ module.title }}</span>
            <span class="module-category">{{ formatCategoryName(module.category) }}</span>
          </div>
        </div>
      </div>
      <div v-if="filteredModules.length === 0" class="no-results">
        No modules found
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

// Request modules registry on mount if not already loaded
onMounted(() => {
  if (!wsStore.registries?.modules) {
    wsStore.requestRegistry('modules')
  }
})
const searchQuery = ref('')
const selectedCategory = ref('')

const selectedModules = computed(() => props.setting.value.items || [])

const allModules = computed(() => {
  const modulesByCategory = wsStore.registries?.modules?.byCategory || {}
  const modules: Array<{ name: string; title: string; category: string }> = []

  Object.entries(modulesByCategory).forEach(([category, categoryModules]) => {
    categoryModules.forEach(mod => {
      modules.push(mod)
    })
  })

  return modules
})

const categories = computed(() => {
  const cats = new Set(allModules.value.map(m => m.category))
  return Array.from(cats).sort()
})

const filteredModules = computed(() => {
  let modules = allModules.value

  // Filter by category
  if (selectedCategory.value) {
    modules = modules.filter(m => m.category === selectedCategory.value)
  }

  // Filter by search query
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    modules = modules.filter(m =>
      m.name.toLowerCase().includes(query) ||
      m.title.toLowerCase().includes(query)
    )
  }

  // Sort: selected first, then alphabetically
  return modules.sort((a, b) => {
    const aSelected = selectedModules.value.includes(a.name)
    const bSelected = selectedModules.value.includes(b.name)
    if (aSelected && !bSelected) return -1
    if (!aSelected && bSelected) return 1
    return a.title.localeCompare(b.title)
  })
})

function formatCategoryName(category: string) {
  return category.charAt(0).toUpperCase() + category.slice(1)
}

function getModuleTitle(moduleName: string) {
  const module = allModules.value.find(m => m.name === moduleName)
  return module?.title || moduleName
}

function addModule(moduleName: string) {
  if (selectedModules.value.includes(moduleName)) return

  const updated = [...selectedModules.value, moduleName]
  updateValue(updated)
}

function removeModule(moduleName: string) {
  const updated = selectedModules.value.filter(name => name !== moduleName)
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

.category-filter {
  padding: 0.5rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  color: var(--color-text);
  font-size: 0.875rem;
  cursor: pointer;
  min-width: 150px;
}

.category-filter:focus {
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

.module-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.module-title {
  font-weight: 500;
}

.module-category {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  padding: 0.125rem 0.375rem;
  background: var(--color-background-mute);
  border-radius: 3px;
  text-transform: capitalize;
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
