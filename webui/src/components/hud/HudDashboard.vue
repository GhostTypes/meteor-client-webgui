<template>
  <div class="hud-dashboard">
    <div class="list-header">
      <div>
        <p class="eyebrow">HUD</p>
        <h2>Live Previews</h2>
      </div>
      <div class="header-actions">
        <p class="hud-count">{{ elements.length }} elements</p>
        <label class="non-text-toggle">
          <input
            type="checkbox"
            :checked="props.hideNonText"
            @change="updateHideFilter(($event.target as HTMLInputElement).checked)"
          />
          <span>Hide elements with shapes/items</span>
        </label>
      </div>
    </div>

    <div v-if="loading" class="empty-hint">
      <p>Waiting for HUD data…</p>
    </div>

    <div v-else-if="elements.length" :class="['hud-grid', { compact: isCompact }]">
      <article
        v-for="element in elements"
        :key="element.name"
        :class="['hud-card', { compact: isCompact }]"
      >
        <template v-if="isCompact">
          <div class="compact-row">
            <div class="compact-info">
              <h3>{{ element.title }}</h3>
              <p class="hud-meta">{{ element.group }}</p>
            </div>
            <div class="compact-actions">
              <button class="btn btn-ghost" @click="emit('open-settings', element)">Settings</button>
              <button
                class="btn"
                :class="{ active: element.active }"
                @click="toggleElement(element)"
              >
                {{ element.active ? 'Disable' : 'Enable' }}
              </button>
            </div>
          </div>
          <div class="compact-preview" :class="{ placeholder: !element.lines?.length }">
            <template v-if="element.lines?.length">
              <span v-for="(line, index) in element.lines" :key="index" class="preview-line">
                {{ line.text }}
              </span>
            </template>
            <template v-else>
              <span>No text output detected</span>
            </template>
          </div>
        </template>
        <template v-else>
          <header>
            <div>
              <h3>{{ element.title }}</h3>
              <p class="hud-meta">{{ element.group }}</p>
            </div>
            <div class="status-controls">
              <span class="status-pill" :class="{ active: element.active }">
                {{ element.active ? 'Active' : 'Hidden' }}
              </span>
              <div class="card-actions">
                <button class="btn btn-ghost" @click="emit('open-settings', element)">
                  Settings
                </button>
                <button
                  class="btn"
                  :class="{ active: element.active }"
                  @click="toggleElement(element)"
                >
                  {{ element.active ? 'Disable' : 'Enable' }}
                </button>
              </div>
            </div>
          </header>

          <p class="description">{{ element.description || 'No description provided.' }}</p>

          <div class="preview" v-if="element.lines?.length">
            <p v-for="(line, index) in element.lines" :key="index" :style="lineStyle(line)">
              {{ line.text }}
            </p>
          </div>
          <div v-else class="preview placeholder">
            <p>No text output detected{{ element.hasNonText ? ' (renders shapes/items)' : '' }}.</p>
          </div>
        </template>
      </article>
    </div>

    <div v-else class="empty-hint">
      <p>No HUD elements matched the filters.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { HudElementState, HudTextLine } from '../../stores/hud'
import { useWebSocketStore } from '../../stores/websocket'

const props = defineProps<{
  elements: HudElementState[]
  loading: boolean
  hideNonText: boolean
  density: 'comfortable' | 'compact'
}>()

const emit = defineEmits<{
  (event: 'open-settings', element: HudElementState): void
  (event: 'update:hide-non-text', value: boolean): void
}>()

const wsStore = useWebSocketStore()
const isCompact = computed(() => props.density === 'compact')

function lineStyle(line: HudTextLine) {
  return {
    color: line.color,
    opacity: line.shadow ? 0.92 : 1,
    transform: line.scale !== 1 ? `scale(${line.scale})` : undefined,
  }
}

function toggleElement(element: HudElementState) {
  wsStore.send({
    type: 'hud.toggle',
    data: {
      elementName: element.name
    }
  })
}

function updateHideFilter(value: boolean) {
  emit('update:hide-non-text', value)
}
</script>

<style scoped>
.hud-dashboard {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.2em;
  font-size: 0.65rem;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}

.header-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.35rem;
}

.hud-count {
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

.non-text-toggle {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.8rem;
  color: var(--color-text-muted);
}

.non-text-toggle input {
  cursor: pointer;
}

.hud-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.hud-grid.compact {
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 0.75rem;
}

.hud-card {
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: var(--radius-md);
  padding: 1.125rem;
  background: var(--color-surface-1);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.hud-card.compact {
  padding: 0.9rem;
  gap: 0.5rem;
}

.hud-card header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.5rem;
}

.status-controls {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.5rem;
}

.hud-card h3 {
  margin: 0;
  font-size: 1.15rem;
}

.hud-meta {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 0.85rem;
}

.status-pill {
  font-size: 0.8rem;
  padding: 0.2rem 0.6rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: var(--color-text-muted);
}

.status-pill.active {
  color: var(--color-success);
  border-color: rgba(74, 222, 128, 0.4);
}

.card-actions {
  display: flex;
  gap: 0.5rem;
}

.description {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 0.9rem;
  min-height: 2.4rem;
}

.preview {
  border: 1px dashed rgba(255, 255, 255, 0.12);
  border-radius: var(--radius-sm);
  padding: 0.75rem;
  min-height: 4.5rem;
  background: rgba(255, 255, 255, 0.02);
}

.preview.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-muted);
}

.preview p {
  margin: 0;
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.95rem;
}

.empty-hint {
  border: 1px dashed rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-md);
  padding: 2rem;
  text-align: center;
  color: var(--color-text-muted);
}
</style>
.compact-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
}

.compact-info h3 {
  margin: 0;
  font-size: 1rem;
}

.compact-actions {
  display: flex;
  gap: 0.35rem;
}

.compact-actions .btn {
  padding: 0.2rem 0.6rem;
  font-size: 0.8rem;
}

.compact-preview {
  border: 1px dashed rgba(255, 255, 255, 0.12);
  border-radius: var(--radius-sm);
  padding: 0.5rem;
  min-height: 3rem;
  background: rgba(255, 255, 255, 0.02);
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem 0.5rem;
}

.compact-preview.placeholder {
  color: var(--color-text-muted);
  align-items: center;
}

.preview-line {
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.85rem;
}
