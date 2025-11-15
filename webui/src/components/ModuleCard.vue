<template>
  <article class="module-card" :class="{ active: module.active }">
    <header class="module-header">
      <div class="module-info">
        <p class="module-name">{{ module.name }}</p>
        <h3>{{ module.title }}</h3>
      </div>
      <div class="module-actions">
        <button
          class="favorite-button"
          :class="{ active: isFavorite }"
          @click.stop="toggleFavorite"
          :aria-pressed="isFavorite"
          :title="isFavorite ? 'Remove from favorites' : 'Add to favorites'"
        >
          <svg viewBox="0 0 20 20" aria-hidden="true">
            <path
              d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 0 0 .95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.802 2.036a1 1 0 0 0-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.539 1.118l-2.802-2.036a1 1 0 0 0-1.176 0l-2.802 2.036c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 0 0-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81H7.03a1 1 0 0 0 .95-.69l1.07-3.292Z"
            />
          </svg>
        </button>
        <button
          class="toggle-button"
          :class="{ active: module.active }"
          @click="toggleModule"
          :disabled="toggling"
        >
          {{ module.active ? 'Disable' : 'Enable' }}
        </button>
      </div>
    </header>

    <p class="description">
      {{ module.description }}
    </p>

    <div class="module-meta">
      <span class="addon-badge">{{ module.addon }}</span>
      <span class="settings-count">
        {{ module.settingGroups?.length || 0 }} setting groups
      </span>
    </div>

    <footer class="card-footer">
      <button class="ghost-button" @click="emit('open-settings', module)">
        Configure
      </button>
    </footer>
  </article>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ModuleInfo } from '../stores/modules'
import { useWebSocketStore } from '../stores/websocket'
import { useModulesStore } from '../stores/modules'

const props = defineProps<{
  module: ModuleInfo
}>()

const emit = defineEmits<{
  (event: 'open-settings', module: ModuleInfo): void
}>()

const wsStore = useWebSocketStore()
const modulesStore = useModulesStore()
const toggling = ref(false)

const isFavorite = computed(() => modulesStore.isFavorite(props.module.name))

async function toggleModule() {
  if (toggling.value) return
  toggling.value = true

  wsStore.send({
    type: 'module.toggle',
    data: {
      moduleName: props.module.name
    },
    id: `toggle-${Date.now()}`
  })

  setTimeout(() => {
    toggling.value = false
  }, 300)
}

function toggleFavorite() {
  modulesStore.toggleFavorite(props.module.name)
}
</script>

<style scoped>
.module-card {
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: var(--radius-md);
  padding: 1.1rem;
  background: linear-gradient(180deg, rgba(16, 20, 26, 0.85), rgba(9, 11, 15, 0.9));
  box-shadow: 0 20px 55px rgba(5, 5, 5, 0.35);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  transition: transform var(--transition-base), border var(--transition-base), box-shadow var(--transition-base);
  min-height: 220px;
}

.module-card:hover {
  transform: translateY(-2px);
  border-color: rgba(75, 166, 255, 0.4);
  box-shadow: 0 25px 60px rgba(12, 16, 24, 0.45);
}

.module-card.active {
  border-color: rgba(75, 166, 255, 0.65);
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.75rem;
}

.module-info {
  flex: 1;
}

.module-actions {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.module-name {
  text-transform: uppercase;
  font-size: 0.7rem;
  letter-spacing: 0.2em;
  color: var(--color-text-muted);
  margin-bottom: 0.2rem;
}

.module-info h3 {
  margin: 0;
  font-size: 1.05rem;
}

.description {
  font-size: 0.85rem;
  color: var(--color-text-muted);
  flex: 1;
  min-height: 45px;
}

.module-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: var(--color-text-muted);
}

.addon-badge {
  border-radius: 999px;
  padding: 0.2rem 0.75rem;
  background: rgba(255, 255, 255, 0.08);
}

.settings-count {
  font-size: 0.75rem;
}

.favorite-button {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.favorite-button svg {
  width: 16px;
  height: 16px;
  fill: rgba(255, 255, 255, 0.4);
  transition: fill var(--transition-base);
}

.favorite-button.active svg {
  fill: #ffc857;
}

.favorite-button:hover svg {
  fill: #ffd479;
}

.toggle-button {
  min-width: 110px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  font-size: 0.85rem;
}

.toggle-button.active {
  background: rgba(74, 222, 128, 0.12);
  border-color: rgba(74, 222, 128, 0.4);
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: auto;
}

.ghost-button {
  border-radius: 999px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.12);
  padding: 0.4rem 1.2rem;
  font-size: 0.85rem;
  color: var(--color-text);
}

.ghost-button:hover {
  border-color: rgba(75, 166, 255, 0.6);
  color: var(--color-accent);
}
</style>
