<template>
  <Teleport to="body">
    <transition name="dialog">
      <div
        v-if="open && module"
        class="module-dialog-overlay"
        @click.self="emit('close')"
      >
        <section class="module-dialog">
          <header class="dialog-header">
            <div>
              <p class="eyebrow">Configuring</p>
              <h2>{{ module.title }}</h2>
              <p class="module-description">{{ module.description }}</p>
            </div>

            <div class="dialog-actions">
              <button
                class="favorite-button"
                :class="{ active: isFavorite }"
                @click="toggleFavorite"
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
                {{ module.active ? 'Disable module' : 'Enable module' }}
              </button>
              <button class="ghost-button" @click="emit('close')">Close</button>
            </div>
          </header>

          <div class="dialog-body">
            <aside class="module-summary">
              <div class="summary-block">
                <p class="summary-label">Module ID</p>
                <p class="summary-value">{{ module.name }}</p>
              </div>
              <div class="summary-block">
                <p class="summary-label">Addon</p>
                <p class="summary-value">{{ module.addon }}</p>
              </div>
              <div class="summary-block">
                <p class="summary-label">Setting Groups</p>
                <p class="summary-value">{{ module.settingGroups?.length || 0 }}</p>
              </div>
            </aside>

            <div class="settings-scroll">
              <SettingsPanel :module="module" />
            </div>
          </div>
        </section>
      </div>
    </transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch, onBeforeUnmount, computed } from 'vue'
import type { ModuleInfo } from '../stores/modules'
import { useWebSocketStore } from '../stores/websocket'
import { useModulesStore } from '../stores/modules'
import SettingsPanel from './SettingsPanel.vue'

const props = defineProps<{
  open: boolean
  module: ModuleInfo | null
}>()

const emit = defineEmits<{
  (event: 'close'): void
}>()

const wsStore = useWebSocketStore()
const modulesStore = useModulesStore()
const toggling = ref(false)

const isFavorite = computed(() => (props.module ? modulesStore.isFavorite(props.module.name) : false))

function toggleModule() {
  if (!props.module || toggling.value) return
  toggling.value = true

  wsStore.send({
    type: 'module.toggle',
    data: {
      moduleName: props.module.name
    },
    id: `toggle-${Date.now()}`
  })

  setTimeout(() => (toggling.value = false), 300)
}

function toggleFavorite() {
  if (!props.module) return
  modulesStore.toggleFavorite(props.module.name)
}

function onKeyDown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    emit('close')
  }
}

watch(
  () => props.open,
  (isOpen) => {
    document.body.style.overflow = isOpen ? 'hidden' : ''
    if (isOpen) {
      window.addEventListener('keydown', onKeyDown)
    } else {
      window.removeEventListener('keydown', onKeyDown)
    }
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  document.body.style.overflow = ''
  window.removeEventListener('keydown', onKeyDown)
})
</script>

<style scoped>
.dialog-enter-active,
.dialog-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
}

.module-dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(5, 8, 15, 0.8);
  backdrop-filter: blur(16px);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  z-index: 40;
}

.module-dialog {
  width: min(1200px, 100%);
  max-height: 90vh;
  background: rgba(9, 12, 18, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  box-shadow: 0 40px 120px rgba(5, 7, 12, 0.5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.dialog-header {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  justify-content: space-between;
  gap: 1.25rem;
}

.dialog-header h2 {
  margin: 0;
  font-size: 1.75rem;
}

.module-description {
  color: var(--color-text-muted);
  margin-top: 0.5rem;
}

.dialog-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-items: flex-end;
}

.dialog-body {
  display: grid;
  grid-template-columns: 280px 1fr;
  min-height: 0;
  flex: 1;
}

.module-summary {
  border-right: 1px solid rgba(255, 255, 255, 0.05);
  padding: 1.5rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.summary-block {
  background: rgba(255, 255, 255, 0.02);
  padding: 0.75rem 1rem;
  border-radius: var(--radius-md);
  border: 1px solid rgba(255, 255, 255, 0.03);
}

.summary-label {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.15em;
  color: var(--color-text-muted);
  margin-bottom: 0.35rem;
}

.summary-value {
  font-size: 0.95rem;
}

.settings-scroll {
  padding: 1.5rem;
  overflow-y: auto;
}

.toggle-button {
  border-radius: 999px;
  padding-inline: 1.5rem;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.04);
}

.toggle-button.active {
  background: rgba(74, 222, 128, 0.18);
  border-color: rgba(74, 222, 128, 0.45);
}

.favorite-button {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.04);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.favorite-button svg {
  width: 18px;
  height: 18px;
  fill: rgba(255, 255, 255, 0.45);
}

.favorite-button.active svg {
  fill: #ffc857;
}

.ghost-button {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 999px;
  padding-inline: 1.5rem;
}

@media (max-width: 960px) {
  .dialog-body {
    grid-template-columns: 1fr;
  }

  .module-summary {
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    flex-direction: row;
    flex-wrap: wrap;
    padding: 1.25rem;
  }
}

@media (max-width: 640px) {
  .module-dialog-overlay {
    padding: 0.5rem;
  }

  .dialog-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .dialog-actions {
    width: 100%;
    align-items: stretch;
  }
}
</style>
