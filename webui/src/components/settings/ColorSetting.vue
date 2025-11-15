<template>
  <div class="setting-item">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
      <div class="color-preview" :style="{ backgroundColor: rgbaString }"></div>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <div class="color-controls">
      <!-- HTML5 color picker for RGB -->
      <div class="color-picker-wrapper">
        <label>Color</label>
        <input
          type="color"
          :value="hexColor"
          @input="updateFromHex"
          class="color-picker"
        />
        <span class="hex-display">{{ hexColor.toUpperCase() }}</span>
      </div>

      <!-- Alpha slider -->
      <div class="slider-wrapper">
        <label>Alpha</label>
        <div class="slider-container">
          <input
            type="range"
            min="0"
            max="255"
            :value="setting.value.a"
            @input="updateAlpha"
            class="slider"
          />
          <span class="slider-value">{{ setting.value.a }}</span>
        </div>
      </div>

      <!-- RGBA numerical display -->
      <div class="rgba-values">
        <div class="rgba-value">
          <span class="rgba-label">R:</span>
          <input
            type="number"
            min="0"
            max="255"
            :value="setting.value.r"
            @input="updateR"
            class="rgba-input"
          />
        </div>
        <div class="rgba-value">
          <span class="rgba-label">G:</span>
          <input
            type="number"
            min="0"
            max="255"
            :value="setting.value.g"
            @input="updateG"
            class="rgba-input"
          />
        </div>
        <div class="rgba-value">
          <span class="rgba-label">B:</span>
          <input
            type="number"
            min="0"
            max="255"
            :value="setting.value.b"
            @input="updateB"
            class="rgba-input"
          />
        </div>
        <div class="rgba-value">
          <span class="rgba-label">A:</span>
          <input
            type="number"
            min="0"
            max="255"
            :value="setting.value.a"
            @input="updateAlpha"
            class="rgba-input"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ModuleInfo, SettingMetadata } from '../../stores/modules'
import { useWebSocketStore } from '../../stores/websocket'

const props = defineProps<{
  module: ModuleInfo
  setting: SettingMetadata
}>()

const wsStore = useWebSocketStore()

// Convert RGBA to hex string for color picker
const hexColor = computed(() => {
  const r = props.setting.value.r.toString(16).padStart(2, '0')
  const g = props.setting.value.g.toString(16).padStart(2, '0')
  const b = props.setting.value.b.toString(16).padStart(2, '0')
  return `#${r}${g}${b}`
})

// RGBA string for preview with alpha
const rgbaString = computed(() => {
  return `rgba(${props.setting.value.r}, ${props.setting.value.g}, ${props.setting.value.b}, ${props.setting.value.a / 255})`
})

function updateValue(r: number, g: number, b: number, a: number) {
  // Clamp values to 0-255
  r = Math.max(0, Math.min(255, Math.floor(r)))
  g = Math.max(0, Math.min(255, Math.floor(g)))
  b = Math.max(0, Math.min(255, Math.floor(b)))
  a = Math.max(0, Math.min(255, Math.floor(a)))

  wsStore.send({
    type: 'setting.update',
    data: {
      moduleName: props.module.name,
      settingName: props.setting.name,
      value: { r, g, b, a }
    }
  })
}

function updateFromHex(event: Event) {
  const hex = (event.target as HTMLInputElement).value
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  updateValue(r, g, b, props.setting.value.a)
}

function updateAlpha(event: Event) {
  const alpha = parseInt((event.target as HTMLInputElement).value)
  updateValue(
    props.setting.value.r,
    props.setting.value.g,
    props.setting.value.b,
    alpha
  )
}

function updateR(event: Event) {
  const r = parseInt((event.target as HTMLInputElement).value)
  updateValue(
    r,
    props.setting.value.g,
    props.setting.value.b,
    props.setting.value.a
  )
}

function updateG(event: Event) {
  const g = parseInt((event.target as HTMLInputElement).value)
  updateValue(
    props.setting.value.r,
    g,
    props.setting.value.b,
    props.setting.value.a
  )
}

function updateB(event: Event) {
  const b = parseInt((event.target as HTMLInputElement).value)
  updateValue(
    props.setting.value.r,
    props.setting.value.g,
    b,
    props.setting.value.a
  )
}
</script>

<style scoped>
.color-controls {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 0.5rem;
}

.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.color-picker-wrapper label {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
  min-width: 60px;
}

.color-picker {
  width: 60px;
  height: 40px;
  border: 2px solid var(--color-border);
  border-radius: 4px;
  cursor: pointer;
  padding: 2px;
}

.color-picker:hover {
  border-color: var(--color-primary);
}

.hex-display {
  font-family: monospace;
  font-size: 0.875rem;
  color: var(--color-text);
  padding: 0.25rem 0.5rem;
  background: var(--color-background-soft);
  border-radius: 4px;
}

.slider-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.slider-wrapper label {
  font-size: 0.875rem;
  color: var(--color-text-secondary);
}

.slider-container {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.slider {
  flex: 1;
  height: 6px;
  border-radius: 3px;
  background: linear-gradient(to right, transparent, var(--color-text));
  outline: none;
  cursor: pointer;
}

.slider::-webkit-slider-thumb {
  appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-primary);
  cursor: pointer;
  border: 2px solid var(--color-background);
}

.slider::-moz-range-thumb {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--color-primary);
  cursor: pointer;
  border: 2px solid var(--color-background);
}

.slider-value {
  font-family: monospace;
  font-size: 0.875rem;
  color: var(--color-text);
  min-width: 40px;
  text-align: right;
}

.rgba-values {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.75rem;
}

.rgba-value {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.rgba-label {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  font-weight: 600;
}

.rgba-input {
  width: 100%;
  padding: 0.5rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  color: var(--color-text);
  font-size: 0.875rem;
  font-family: monospace;
}

.rgba-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.rgba-input::-webkit-inner-spin-button,
.rgba-input::-webkit-outer-spin-button {
  opacity: 1;
}

.color-preview {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  border: 2px solid var(--color-border);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}
</style>
