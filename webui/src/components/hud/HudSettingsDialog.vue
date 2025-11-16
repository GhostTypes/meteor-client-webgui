<template>
  <Teleport to="body">
    <transition name="dialog">
      <div
        v-if="open && element"
        class="hud-dialog-overlay"
        @click.self="emit('close')"
      >
        <section class="hud-dialog">
          <header class="dialog-header">
            <div>
              <p class="eyebrow">Configuring HUD</p>
              <h2>{{ element.title }}</h2>
              <p class="hud-description">{{ element.description || 'No description provided.' }}</p>
            </div>

            <div class="dialog-actions">
              <button
                class="btn"
                :class="{ active: element.active }"
                @click="toggleElement"
                :disabled="toggling"
              >
                {{ element.active ? 'Disable element' : 'Enable element' }}
              </button>
              <button class="btn btn-ghost" @click="emit('close')">Close</button>
            </div>
          </header>

          <div class="dialog-body">
            <aside class="hud-summary">
              <div class="summary-block">
                <p class="summary-label">Element ID</p>
                <p class="summary-value">{{ formatId(element.name) }}</p>
              </div>
              <div class="summary-block">
                <p class="summary-label">Group</p>
                <p class="summary-value">{{ element.group }}</p>
              </div>
              <div class="summary-block">
                <p class="summary-label">Position</p>
                <p class="summary-value">X: {{ element.x }}, Y: {{ element.y }}</p>
              </div>
            </aside>

            <div class="settings-scroll">
              <SettingsPanel :module="element" />
            </div>
          </div>
        </section>
      </div>
    </transition>
  </Teleport>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'
import { useWebSocketStore } from '../../stores/websocket'
import type { HudElementState } from '../../stores/hud'
import SettingsPanel from '../SettingsPanel.vue'

const props = defineProps<{
  open: boolean
  element: HudElementState | null
}>()

const emit = defineEmits<{
  (event: 'close'): void
}>()

const wsStore = useWebSocketStore()
const toggling = ref(false)

function formatId(name: string) {
  return name.replace(/^hud::/i, '').replace(/#.+$/, '')
}

function toggleElement() {
  if (!props.element || toggling.value) return
  toggling.value = true
  wsStore.send({
    type: 'hud.toggle',
    data: {
      elementName: props.element.name
    }
  })
  setTimeout(() => (toggling.value = false), 300)
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

.hud-dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(5, 8, 15, 0.8);
  backdrop-filter: blur(16px);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2rem;
  z-index: 50;
}

.hud-dialog {
  width: min(1000px, 100%);
  max-height: 90vh;
  background: var(--color-surface-1);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-xl);
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
  position: sticky;
  top: 0;
  background: linear-gradient(180deg, rgba(9, 12, 18, 0.98), rgba(9, 12, 18, 0.9));
  z-index: 2;
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
  gap: 1.5rem;
  padding: 1.5rem 2rem;
  overflow: hidden;
}

.hud-summary {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.summary-block {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-sm);
  padding: 0.85rem;
}

.summary-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}

.summary-value {
  margin: 0;
  font-size: 0.95rem;
}

.settings-scroll {
  overflow-y: auto;
  padding-right: 0.5rem;
}

.hud-description {
  color: var(--color-text-muted);
  margin-top: 0.5rem;
}

@media (max-width: 960px) {
  .dialog-body {
    grid-template-columns: 1fr;
  }

  .dialog-actions {
    flex-direction: row;
  }
}
</style>
