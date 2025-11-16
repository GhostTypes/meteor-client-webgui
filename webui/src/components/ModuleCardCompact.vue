<template>
  <div class="module-card-compact-wrapper" :class="{ active: module.active }">
    <div
      class="module-card-compact"
      role="button"
      tabindex="0"
      @click="handleCardToggle"
      @keydown.enter.prevent="handleCardToggle"
      @keydown.space.prevent="handleCardToggle"
    >
      <div class="compact-header">
        <div class="name-block">
          <h4>{{ module.title }}</h4>
          <span class="chip">{{ module.addon }}</span>
        </div>
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
      </div>

      <div class="compact-body">
        <button class="btn btn-ghost" @click.stop="emit('open-settings', module)">Configure</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ModuleInfo } from '../stores/modules'
import { useModulesStore } from '../stores/modules'
import { useWebSocketStore } from '../stores/websocket'

const props = defineProps<{ module: ModuleInfo }>()
const emit = defineEmits<{ (event: 'open-settings', module: ModuleInfo): void }>()
const modulesStore = useModulesStore()
const wsStore = useWebSocketStore()
const toggling = ref(false)
const isFavorite = computed(() => modulesStore.isFavorite(props.module.name))

function toggleFavorite() {
  modulesStore.toggleFavorite(props.module.name)
}

function toggleModule() {
  if (toggling.value) return
  toggling.value = true
  wsStore.send({
    type: 'module.toggle',
    data: { moduleName: props.module.name },
    id: `toggle-${Date.now()}`
  })
  setTimeout(() => (toggling.value = false), 250)
}

function handleCardToggle() {
  toggleModule()
}
</script>

<style scoped>

.module-card-compact-wrapper.active .module-card-compact {
  border-color: rgba(75, 166, 255, 0.45);
  background: var(--color-surface-2);
}

.module-card-compact {
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: var(--radius-sm);
  background: var(--color-surface-1);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  cursor: pointer;
}

.compact-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.5rem;
}

.name-block {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.name-block h4 {
  margin: 0;
  font-size: 1rem;
}

.compact-body {
  display: flex;
  justify-content: center;
}

.favorite-button {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  border: 1px solid rgba(255, 255, 255, 0.1);
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

.chip {
  align-self: flex-start;
}

.module-card-compact:focus-visible {
  outline: none;
  border-color: rgba(75, 166, 255, 0.55);
}

</style>
