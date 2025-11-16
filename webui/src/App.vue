<template>
  <div class="app">
    <header class="header">
      <div>
        <p class="eyebrow">Meteor Client</p>
        <h1>Meteor WebGUI</h1>
      </div>
      <div class="connection-status">
        <span v-if="wsStore.connected" class="status-dot connected"></span>
        <span v-else-if="wsStore.reconnecting" class="status-dot reconnecting"></span>
        <span v-else class="status-dot disconnected"></span>
        <span class="status-text">
          {{ wsStore.connected ? 'Connected' : wsStore.reconnecting ? 'Reconnecting…' : 'Disconnected' }}
        </span>
        <button v-if="!wsStore.connected" class="btn btn-primary retry-button" @click="wsStore.connect()">
          Retry
        </button>
      </div>
    </header>

    <div v-if="modulesStore.loading" class="state-view">
      <p>Loading modules…</p>
    </div>

    <div v-else-if="wsStore.error" class="state-view error">
      <p>Error: {{ wsStore.error }}</p>
      <button class="btn btn-primary" @click="wsStore.connect()">Retry Connection</button>
    </div>

    <main v-else class="main">
      <div class="main-content">
        <ModuleToolbar
          v-model:selected-category="selectedCategory"
          v-model:search-query="searchQuery"
          v-model:show-active-only="showActiveOnly"
          v-model:density="cardDensity"
          :categories="toolbarCategories"
          :active-toggle-disabled="isFavoritesSelected && favoritesEmpty"
        />

        <div class="module-content">
          <ModuleList
            v-if="selectedCategory && !isHudSelected"
            :category="selectedCategory"
            :modules="filteredModules"
            :density="cardDensity"
            :empty-message="isFavoritesSelected ? favoritesEmptyMessage : undefined"
            @open-settings="openSettings"
          />

          <HudDashboard
            v-else-if="isHudSelected"
            :elements="filteredHudElements"
            :loading="hudStore.loading"
            :hide-non-text="hideDecoratedHud"
            :density="cardDensity"
            @open-settings="openHudSettings"
            @update:hide-non-text="value => (hideDecoratedHud.value = value)"
          />

          <div v-else class="empty-state">
            <p>Select a category to view its modules.</p>
          </div>
        </div>
      </div>
    </main>

    <ModuleSettingsDialog
      :open="Boolean(activeModule)"
      :module="activeModule"
      @close="closeSettings"
    />
    <HudSettingsDialog
      :open="Boolean(activeHudElement)"
      :element="activeHudElement"
      @close="closeHudSettings"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useWebSocketStore } from './stores/websocket'
import { useModulesStore, type ModuleInfo } from './stores/modules'
import { useHudStore, type HudElementState } from './stores/hud'
import ModuleList from './components/ModuleList.vue'
import ModuleToolbar from './components/ModuleToolbar.vue'
import ModuleSettingsDialog from './components/ModuleSettingsDialog.vue'
import HudDashboard from './components/hud/HudDashboard.vue'
import HudSettingsDialog from './components/hud/HudSettingsDialog.vue'

type CardDensity = 'comfortable' | 'compact'
const FAVORITES_CATEGORY = 'Favorites'
const HUD_CATEGORY = 'HUD'

const wsStore = useWebSocketStore()
const modulesStore = useModulesStore()
const hudStore = useHudStore()

const selectedCategory = ref<string | null>(null)
const searchQuery = ref('')
const showActiveOnly = ref(false)
const cardDensity = ref<CardDensity>('comfortable')
const activeModule = ref<ModuleInfo | null>(null)
const activeHudElement = ref<HudElementState | null>(null)
const hideDecoratedHud = ref(false)
const favoritesEmptyMessage = 'No favorites yet. Tap the star icon on a module to save it here.'
const isFavoritesSelected = computed(() => selectedCategory.value === FAVORITES_CATEGORY)
const isHudSelected = computed(() => selectedCategory.value === HUD_CATEGORY)
const toolbarCategories = computed(() => {
  const list = modulesStore.categories as unknown as string[]
  return [...list, FAVORITES_CATEGORY, HUD_CATEGORY]
})
const favoritesEmpty = computed(() => modulesStore.favoriteModules.length === 0)

const filteredModules = computed(() => {
  if (!selectedCategory.value || selectedCategory.value === HUD_CATEGORY) return []
  const modules = isFavoritesSelected.value
    ? modulesStore.favoriteModules
    : modulesStore.byCategory[selectedCategory.value] || []
  return modules.filter(module => {
    if (showActiveOnly.value && !module.active) return false
    if (!searchQuery.value) return true
    const term = searchQuery.value.toLowerCase()
    return (
      module.title.toLowerCase().includes(term) ||
      module.description.toLowerCase().includes(term) ||
      module.name.toLowerCase().includes(term)
    )
  })
})

const filteredHudElements = computed(() => {
  const list = hudStore.orderedElements || []
  return list.filter(element => {
    if (!element.active) return false
    const hasTextOutput = Array.isArray(element.lines) && element.lines.length > 0
    if (!hasTextOutput) return false
    if (hideDecoratedHud.value && element.hasNonText) return false
    if (showActiveOnly.value && !element.active) return false
    if (!searchQuery.value) return true
    const term = searchQuery.value.toLowerCase()
    return (
      element.title.toLowerCase().includes(term) ||
      element.description.toLowerCase().includes(term) ||
      element.name.toLowerCase().includes(term) ||
      element.group.toLowerCase().includes(term)
    )
  })
})

onMounted(() => {
  wsStore.connect()
})

watch(
  () => modulesStore.categories,
  categories => {
    if (!categories.length) {
      if (
        selectedCategory.value !== FAVORITES_CATEGORY &&
        selectedCategory.value !== HUD_CATEGORY
      ) {
        selectedCategory.value = null
      }
      return
    }
    if (!selectedCategory.value) {
      selectedCategory.value = categories[0]
      return
    }
    if (
      selectedCategory.value !== FAVORITES_CATEGORY &&
      selectedCategory.value !== HUD_CATEGORY &&
      !categories.includes(selectedCategory.value)
    ) {
      selectedCategory.value = categories[0]
    }
  },
  { immediate: true }
)

function openSettings(module: ModuleInfo) {
  activeModule.value = module
}

function closeSettings() {
  activeModule.value = null
}

function openHudSettings(element: HudElementState) {
  activeHudElement.value = element
}

function closeHudSettings() {
  activeHudElement.value = null
}
</script>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(5, 5, 5, 0.8);
  backdrop-filter: blur(6px);
}

.eyebrow {
  letter-spacing: 0.18em;
  text-transform: uppercase;
  font-size: 0.7rem;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}

.header h1 {
  margin: 0;
  font-size: 1.75rem;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
}

.status-dot.connected {
  background: var(--color-success);
  box-shadow: 0 0 12px rgba(74, 222, 128, 0.6);
}

.status-dot.reconnecting {
  background: var(--color-warning);
  animation: pulse 1s infinite;
}

.status-dot.disconnected {
  background: var(--color-danger);
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.status-text {
  color: var(--color-text-muted);
  font-size: 0.85rem;
}

.retry-button {
  padding-inline: 1.5rem;
}

.state-view {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  font-size: 1rem;
  color: var(--color-text-muted);
}

.state-view.error {
  flex-direction: column;
}

.main {
  flex: 1;
  padding: 1.5rem;
}

.main-content {
  max-width: 1600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.module-content {
  background: rgba(16, 20, 26, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: var(--radius-md);
  padding: 1.5rem;
  box-shadow: var(--shadow-soft);
  min-height: 400px;
}

.empty-state {
  text-align: center;
  color: var(--color-text-muted);
  padding: 3rem 1rem;
}
</style>
