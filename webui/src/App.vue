<template>
  <div class="app">
    <header class="header">
      <h1>Meteor WebGUI</h1>
      <div class="connection-status">
        <span v-if="wsStore.connected" class="status-dot connected"></span>
        <span v-else-if="wsStore.reconnecting" class="status-dot reconnecting"></span>
        <span v-else class="status-dot disconnected"></span>
        <span class="status-text">
          {{ wsStore.connected ? 'Connected' : wsStore.reconnecting ? 'Reconnecting...' : 'Disconnected' }}
        </span>
      </div>
    </header>

    <div v-if="modulesStore.loading" class="loading">
      <p>Loading modules...</p>
    </div>

    <div v-else-if="wsStore.error" class="error">
      <p>Error: {{ wsStore.error }}</p>
      <button @click="wsStore.connect()">Retry Connection</button>
    </div>

    <main v-else class="main">
      <aside class="sidebar">
        <div class="category-filter">
          <button
            v-for="category in modulesStore.categories"
            :key="category"
            :class="{ active: selectedCategory === category }"
            @click="selectedCategory = category"
          >
            {{ category }}
          </button>
        </div>
      </aside>

      <div class="content">
        <ModuleList
          v-if="selectedCategory"
          :category="selectedCategory"
          :modules="modulesStore.byCategory[selectedCategory] || []"
        />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useWebSocketStore } from './stores/websocket'
import { useModulesStore } from './stores/modules'
import ModuleList from './components/ModuleList.vue'

const wsStore = useWebSocketStore()
const modulesStore = useModulesStore()
const selectedCategory = ref<string | null>(null)

onMounted(() => {
  wsStore.connect()

  // Auto-select first category when loaded
  setTimeout(() => {
    if (modulesStore.categories.length > 0 && !selectedCategory.value) {
      selectedCategory.value = modulesStore.categories[0]
    }
  }, 500)
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  background: #0a0a0a;
  color: #ffffff;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #1a1a1a;
  padding: 1rem 2rem;
  border-bottom: 1px solid #333;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  font-size: 1.5rem;
  color: #4ba6ff;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.status-dot.connected {
  background: #4ade80;
  box-shadow: 0 0 10px #4ade80;
}

.status-dot.reconnecting {
  background: #fbbf24;
  animation: pulse 1s infinite;
}

.status-dot.disconnected {
  background: #ef4444;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.status-text {
  font-size: 0.875rem;
  color: #aaa;
}

.loading,
.error {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
}

.error button {
  padding: 0.5rem 1rem;
  background: #4ba6ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.error button:hover {
  background: #3b96ef;
}

.main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 200px;
  background: #1a1a1a;
  border-right: 1px solid #333;
  padding: 1rem;
  overflow-y: auto;
}

.category-filter {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.category-filter button {
  padding: 0.75rem;
  background: #2a2a2a;
  color: #aaa;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s;
}

.category-filter button:hover {
  background: #333;
  color: #fff;
}

.category-filter button.active {
  background: #4ba6ff;
  color: #fff;
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 2rem;
}
</style>
