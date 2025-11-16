<template>
  <div class="module-card-wrapper" :class="{ active: module.active }">
    <article
      class="module-card"
      role="button"
      tabindex="0"
      @click="handleCardToggle"
      @keydown.enter.prevent="handleCardToggle"
      @keydown.space.prevent="handleCardToggle"
    >
      <header class="module-header">
        <button
          class="favorite-button"
          :class="{ active: isFavorite }"
          @click.stop="toggleFavorite"
          :title="isFavorite ? 'Remove from favorites' : 'Add to favorites'"
        >
          <svg viewBox="0 0 20 20" aria-hidden="true">
            <path
              d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 0 0 .95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.802 2.036a1 1 0 0 0-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.539 1.118l-2.802-2.036a1 1 0 0 0-1.176 0l-2.802 2.036c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 0 0-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81H7.03a1 1 0 0 0 .95-.69l1.07-3.292Z"
            />
          </svg>
        </button>

        <div class="module-title">
          <p class="module-name">{{ module.name }}</p>
          <h3>{{ module.title }}</h3>
        </div>
      </header>

      <p class="description">
        {{ module.description }}
      </p>

      <div class="module-meta">
        <span class="chip">{{ module.addon }}</span>
        <span class="chip muted">{{ module.settingGroups?.length || 0 }} setting groups</span>
      </div>

      <footer class="card-footer">
        <button class="btn btn-ghost" @click.stop="emit('open-settings', module)">
          Configure
        </button>
      </footer>
    </article>
  </div>
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

function handleCardToggle() {
  toggleModule()
}

function toggleFavorite() {
  modulesStore.toggleFavorite(props.module.name)
}
</script>

<style scoped>
.module-card-wrapper {
  position: relative;
  border-radius: var(--radius-md);
}

.module-card-wrapper.active .module-card {
  border-color: rgba(75, 166, 255, 0.5);
  background: var(--color-surface-2);
  box-shadow: 0 20px 50px rgba(40, 90, 150, 0.35);
}

.module-card {
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: var(--radius-md);
  padding: 1.25rem;
  background: var(--color-surface-1);
  display: flex;
  flex-direction: column;
  gap: 1rem;
  min-height: 230px;
  transition: border var(--transition-base), box-shadow var(--transition-base);
  box-shadow: var(--shadow-soft);
  cursor: pointer;
}

.module-card:hover {
  border-color: rgba(75, 166, 255, 0.35);
}

.module-card:focus-visible {
  outline: none;
  border-color: rgba(75, 166, 255, 0.6);
  box-shadow: 0 0 0 2px rgba(75, 166, 255, 0.3);
}

.module-header {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.favorite-button {
  position: absolute;
  left: 0;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: var(--color-surface-2);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.favorite-button svg {
  width: 20px;
  height: 20px;
  fill: rgba(255, 255, 255, 0.45);
}

.favorite-button.active svg {
  fill: #ffc857;
}

.module-title {
  text-align: center;
}

.module-name {
  text-transform: uppercase;
  letter-spacing: 0.2em;
  font-size: 0.7rem;
  color: var(--color-text-muted);
  margin: 0 0 0.25rem;
}

.module-title h3 {
  margin: 0;
  font-size: 1.2rem;
}

.description {
  color: var(--color-text-muted);
  font-size: 0.9rem;
  margin: 0;
  text-align: center;
  min-height: 48px;
}

.module-meta {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.chip.muted {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.08);
  color: var(--color-text-muted);
}

.card-footer {
  display: flex;
  justify-content: center;
}

.card-footer .btn {
  min-width: 140px;
}
</style>
