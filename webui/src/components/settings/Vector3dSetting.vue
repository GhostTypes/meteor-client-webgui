<template>
  <div class="setting-item">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
      <span class="magnitude">|{{ magnitude.toFixed(3) }}|</span>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <div class="coord-inputs">
      <div class="coord-input">
        <label>X</label>
        <input
          type="number"
          step="0.01"
          :value="setting.value.x"
          @input="updateX"
          class="number-input"
        />
      </div>
      <div class="coord-input">
        <label>Y</label>
        <input
          type="number"
          step="0.01"
          :value="setting.value.y"
          @input="updateY"
          class="number-input"
        />
      </div>
      <div class="coord-input">
        <label>Z</label>
        <input
          type="number"
          step="0.01"
          :value="setting.value.z"
          @input="updateZ"
          class="number-input"
        />
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

// Calculate vector magnitude
const magnitude = computed(() => {
  const x = props.setting.value.x || 0
  const y = props.setting.value.y || 0
  const z = props.setting.value.z || 0
  return Math.sqrt(x * x + y * y + z * z)
})

function updateValue(x: number, y: number, z: number) {
  wsStore.send({
    type: 'setting.update',
    data: {
      moduleName: props.module.name,
      settingName: props.setting.name,
      value: { x, y, z }
    }
  })
}

function updateX(event: Event) {
  const x = parseFloat((event.target as HTMLInputElement).value)
  if (!isNaN(x)) {
    updateValue(x, props.setting.value.y, props.setting.value.z)
  }
}

function updateY(event: Event) {
  const y = parseFloat((event.target as HTMLInputElement).value)
  if (!isNaN(y)) {
    updateValue(props.setting.value.x, y, props.setting.value.z)
  }
}

function updateZ(event: Event) {
  const z = parseFloat((event.target as HTMLInputElement).value)
  if (!isNaN(z)) {
    updateValue(props.setting.value.x, props.setting.value.y, z)
  }
}
</script>

<style scoped>
.setting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.magnitude {
  font-family: monospace;
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  padding: 0.25rem 0.5rem;
  background: var(--color-background-soft);
  border-radius: 4px;
}

.coord-inputs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
  margin-top: 0.5rem;
}

.coord-input {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.coord-input label {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  font-weight: 600;
  text-transform: uppercase;
}

.number-input {
  padding: 0.5rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  color: var(--color-text);
  font-size: 0.875rem;
  font-family: monospace;
  width: 100%;
}

.number-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.number-input::-webkit-inner-spin-button,
.number-input::-webkit-outer-spin-button {
  opacity: 1;
}
</style>
