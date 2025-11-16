<template>
  <div class="setting-item">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
      <span class="count">{{ colors.length }} colors</span>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <!-- Add New Color -->
    <div class="add-color">
      <div class="color-inputs">
        <div class="color-preview-wrapper">
          <input
            type="color"
            v-model="newColorHex"
            class="color-picker"
          />
          <div class="preview" :style="{ backgroundColor: newColorRgba }"></div>
        </div>

        <div class="rgba-inputs">
          <input type="number" v-model.number="newColor.r" min="0" max="255" placeholder="R" class="rgba-input" />
          <input type="number" v-model.number="newColor.g" min="0" max="255" placeholder="G" class="rgba-input" />
          <input type="number" v-model.number="newColor.b" min="0" max="255" placeholder="B" class="rgba-input" />
          <input type="number" v-model.number="newColor.a" min="0" max="255" placeholder="A" class="rgba-input" />
        </div>

        <label class="rainbow-select">
          <input type="checkbox" v-model="newColor.rainbow" /> Rainbow
        </label>
      </div>

      <button @click="addColor" class="add-button">Add Color</button>
    </div>

    <!-- Color List -->
    <div v-if="colors.length > 0" class="color-list">
      <div v-for="(color, index) in colors" :key="index" class="color-item">
        <div class="color-swatch" :style="{ backgroundColor: colorToRgba(color) }"></div>
        <div class="color-values">
          <span class="rgb">RGB: {{ color.r }}, {{ color.g }}, {{ color.b }}</span>
          <span class="alpha">A: {{ color.a }}</span>
          <span v-if="color.rainbow" class="rainbow-pill">Rainbow</span>
        </div>
        <div class="item-actions">
          <button @click="toggleRainbow(index)" class="toggle-rainbow" title="Toggle rainbow">🌈</button>
          <button @click="removeColor(index)" class="remove-button">×</button>
        </div>
      </div>
    </div>

    <div v-else class="empty-message">No colors added yet</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ModuleInfo, SettingMetadata } from '../../stores/modules'
import { useWebSocketStore } from '../../stores/websocket'

const props = defineProps<{
  module: ModuleInfo
  setting: SettingMetadata
}>()

const wsStore = useWebSocketStore()

const newColor = ref({ r: 255, g: 255, b: 255, a: 255, rainbow: false })

const colors = computed(() => props.setting.value.items || [])

const newColorHex = computed({
  get() {
    const r = newColor.value.r.toString(16).padStart(2, '0')
    const g = newColor.value.g.toString(16).padStart(2, '0')
    const b = newColor.value.b.toString(16).padStart(2, '0')
    return `#${r}${g}${b}`
  },
  set(hex: string) {
    newColor.value.r = parseInt(hex.slice(1, 3), 16)
    newColor.value.g = parseInt(hex.slice(3, 5), 16)
    newColor.value.b = parseInt(hex.slice(5, 7), 16)
  }
})

const newColorRgba = computed(() => {
  return `rgba(${newColor.value.r}, ${newColor.value.g}, ${newColor.value.b}, ${newColor.value.a / 255})`
})

function colorToRgba(color: { r: number; g: number; b: number; a: number }) {
  return `rgba(${color.r}, ${color.g}, ${color.b}, ${color.a / 255})`
}

function addColor() {
  const updated = [
    ...colors.value,
    {
      r: Math.max(0, Math.min(255, newColor.value.r)),
      g: Math.max(0, Math.min(255, newColor.value.g)),
      b: Math.max(0, Math.min(255, newColor.value.b)),
      a: Math.max(0, Math.min(255, newColor.value.a)),
      rainbow: Boolean(newColor.value.rainbow)
    }
  ]
  updateValue(updated)
}

function removeColor(index: number) {
  const updated = colors.value.filter((_, i) => i !== index)
  updateValue(updated)
}

function toggleRainbow(index: number) {
  const updated = colors.value.map((color, i) => {
    if (i !== index) return color
    return { ...color, rainbow: !color.rainbow }
  })
  updateValue(updated)
}

function updateValue(items: Array<{ r: number; g: number; b: number; a: number; rainbow?: boolean }>) {
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

.add-color {
  display: flex;
  gap: 0.75rem;
  margin-top: 0.5rem;
  align-items: flex-end;
}

.color-inputs {
  flex: 1;
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.color-preview-wrapper {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.color-picker {
  width: 50px;
  height: 50px;
  border: 2px solid var(--color-border);
  border-radius: 4px;
  cursor: pointer;
  padding: 2px;
}

.color-picker:hover {
  border-color: var(--color-primary);
}

.preview {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  border: 2px solid var(--color-border);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.rgba-inputs {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.5rem;
  flex: 1;
}

.rainbow-select {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.85rem;
  color: var(--color-text-secondary);
}

.rgba-input {
  padding: 0.5rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  color: var(--color-text);
  font-size: 0.875rem;
  font-family: monospace;
  text-align: center;
}

.rgba-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.add-button {
  padding: 0.5rem 1rem;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  white-space: nowrap;
  height: 50px;
}

.add-button:hover {
  opacity: 0.9;
}

.color-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.color-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 4px;
}

.item-actions {
  display: flex;
  gap: 0.35rem;
  margin-left: auto;
}

.color-swatch {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  border: 2px solid var(--color-border);
  flex-shrink: 0;
}

.color-values {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  font-size: 0.875rem;
  font-family: monospace;
}

.rgb {
  color: var(--color-text);
}

.alpha {
  color: var(--color-text-secondary);
  font-size: 0.75rem;
}

.remove-button {
  background: none;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 1.5rem;
  line-height: 1;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
  flex-shrink: 0;
}

.toggle-rainbow {
  background: none;
  border: none;
  font-size: 1.1rem;
  cursor: pointer;
}

.rainbow-pill {
  font-size: 0.75rem;
  padding: 0.1rem 0.4rem;
  border-radius: 999px;
  background: linear-gradient(90deg, #f97316, #facc15, #34d399, #38bdf8, #a855f7);
  color: #0f172a;
  width: fit-content;
}

.remove-button:hover {
  background: var(--color-danger);
  color: white;
}

.empty-message {
  margin-top: 0.75rem;
  padding: 0.75rem;
  background: var(--color-background-soft);
  border: 1px dashed var(--color-border);
  border-radius: 4px;
  text-align: center;
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}
</style>
