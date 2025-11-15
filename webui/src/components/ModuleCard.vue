<template>
  <div class="module-card" :class="{ active: module.active }">
    <div class="module-header">
      <div class="module-info">
        <h3>{{ module.title }}</h3>
        <p class="description">{{ module.description }}</p>
        <span class="addon-badge">{{ module.addon }}</span>
      </div>
      <button
        class="toggle-button"
        :class="{ active: module.active }"
        @click="toggleModule"
        :disabled="toggling"
      >
        {{ module.active ? 'ON' : 'OFF' }}
      </button>
    </div>

    <div v-if="module.settingGroups && module.settingGroups.length > 0" class="settings-section">
      <button
        class="expand-button"
        @click="showSettings = !showSettings"
      >
        {{ showSettings ? 'Hide' : 'Show' }} Settings
      </button>

      <div v-if="showSettings" class="settings">
        <SettingsPanel :module="module" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { ModuleInfo } from '../stores/modules'
import { useWebSocketStore } from '../stores/websocket'
import SettingsPanel from './SettingsPanel.vue'

const props = defineProps<{
  module: ModuleInfo
}>()

const wsStore = useWebSocketStore()
const showSettings = ref(false)
const toggling = ref(false)

async function toggleModule() {
  toggling.value = true

  wsStore.send({
    type: 'module.toggle',
    data: {
      moduleName: props.module.name
    },
    id: `toggle-${Date.now()}`
  })

  // Reset after a short delay
  setTimeout(() => {
    toggling.value = false
  }, 300)
}
</script>

<style scoped>
.module-card {
  background: #1a1a1a;
  border: 2px solid #333;
  border-radius: 8px;
  padding: 1.25rem;
  transition: all 0.2s;
}

.module-card:hover {
  border-color: #555;
}

.module-card.active {
  border-color: #4ba6ff;
  background: #1a2a3a;
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.module-info {
  flex: 1;
}

.module-info h3 {
  font-size: 1.125rem;
  margin-bottom: 0.5rem;
  color: #fff;
}

.description {
  font-size: 0.875rem;
  color: #aaa;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.addon-badge {
  display: inline-block;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  background: #2a2a2a;
  color: #888;
  border-radius: 4px;
}

.toggle-button {
  padding: 0.5rem 1rem;
  background: #2a2a2a;
  color: #aaa;
  border: 2px solid #444;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.2s;
  min-width: 60px;
}

.toggle-button:hover:not(:disabled) {
  background: #333;
}

.toggle-button.active {
  background: #4ba6ff;
  color: #fff;
  border-color: #4ba6ff;
}

.toggle-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.settings-section {
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #333;
}

.expand-button {
  padding: 0.5rem 1rem;
  background: #2a2a2a;
  color: #aaa;
  border: 1px solid #444;
  border-radius: 4px;
  cursor: pointer;
  width: 100%;
  transition: all 0.2s;
}

.expand-button:hover {
  background: #333;
  color: #fff;
}

.settings {
  margin-top: 1rem;
}
</style>
