<template>
  <div class="setting-item">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <div class="coord-inputs">
      <div class="coord-input">
        <label>X</label>
        <input
          type="number"
          :value="setting.value.x"
          @input="updateX"
          class="number-input"
        />
      </div>
      <div class="coord-input">
        <label>Y</label>
        <input
          type="number"
          :value="setting.value.y"
          @input="updateY"
          :min="-64"
          :max="319"
          class="number-input"
          :class="{ 'out-of-range': isYOutOfRange }"
        />
      </div>
      <div class="coord-input">
        <label>Z</label>
        <input
          type="number"
          :value="setting.value.z"
          @input="updateZ"
          class="number-input"
        />
      </div>
    </div>

    <div v-if="isYOutOfRange" class="warning">
      ⚠️ Y coordinate is outside world height (-64 to 319)
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

const isYOutOfRange = computed(() => {
  const y = props.setting.value.y
  return y < -64 || y > 319
})

function updateValue(x: number, y: number, z: number) {
  wsStore.send({
    type: 'setting.update',
    data: {
      moduleName: props.module.name,
      settingName: props.setting.name,
      value: {
        x: Math.floor(x),
        y: Math.floor(y),
        z: Math.floor(z)
      }
    }
  })
}

function updateX(event: Event) {
  const x = parseInt((event.target as HTMLInputElement).value)
  if (!isNaN(x)) {
    updateValue(x, props.setting.value.y, props.setting.value.z)
  }
}

function updateY(event: Event) {
  const y = parseInt((event.target as HTMLInputElement).value)
  if (!isNaN(y)) {
    updateValue(props.setting.value.x, y, props.setting.value.z)
  }
}

function updateZ(event: Event) {
  const z = parseInt((event.target as HTMLInputElement).value)
  if (!isNaN(z)) {
    updateValue(props.setting.value.x, props.setting.value.y, z)
  }
}
</script>

<style scoped>
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

.number-input.out-of-range {
  border-color: #f59e0b;
}

.number-input::-webkit-inner-spin-button,
.number-input::-webkit-outer-spin-button {
  opacity: 1;
}

.warning {
  margin-top: 0.5rem;
  padding: 0.5rem;
  background: rgba(245, 158, 11, 0.1);
  border: 1px solid #f59e0b;
  border-radius: 4px;
  font-size: 0.75rem;
  color: #f59e0b;
}
</style>
