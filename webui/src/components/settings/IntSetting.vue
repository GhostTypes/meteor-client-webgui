<template>
  <div class="int-setting">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
      <span class="setting-value">{{ setting.value.value }}</span>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <input
      v-if="!setting.typeMetadata?.noSlider"
      type="range"
      :min="setting.typeMetadata?.sliderMin || setting.typeMetadata?.min || 0"
      :max="setting.typeMetadata?.sliderMax || setting.typeMetadata?.max || 100"
      :value="setting.value.value"
      @input="updateValue(parseInt($event.target.value))"
    />

    <input
      type="number"
      :min="setting.typeMetadata?.min"
      :max="setting.typeMetadata?.max"
      :value="setting.value.value"
      @input="updateValue(parseInt($event.target.value))"
      class="number-input"
    />
  </div>
</template>

<script setup lang="ts">
import type { ModuleInfo, SettingMetadata } from '../../stores/modules'
import { useWebSocketStore } from '../../stores/websocket'

const props = defineProps<{
  module: ModuleInfo
  setting: SettingMetadata
}>()

const wsStore = useWebSocketStore()

function updateValue(value: number) {
  if (isNaN(value)) return

  wsStore.send({
    type: 'setting.update',
    data: {
      moduleName: props.module.name,
      settingName: props.setting.name,
      value: { value }
    }
  })
}
</script>

<style scoped>
.int-setting {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.setting-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.setting-title {
  font-size: 0.9rem;
  color: #fff;
}

.setting-value {
  font-size: 0.875rem;
  color: #4ba6ff;
  font-weight: bold;
}

.setting-description {
  font-size: 0.8rem;
  color: #888;
}

input[type="range"] {
  width: 100%;
  height: 4px;
  background: #333;
  border-radius: 2px;
  outline: none;
}

input[type="range"]::-webkit-slider-thumb {
  appearance: none;
  width: 16px;
  height: 16px;
  background: #4ba6ff;
  border-radius: 50%;
  cursor: pointer;
}

.number-input {
  padding: 0.5rem;
  background: #2a2a2a;
  color: #fff;
  border: 1px solid #444;
  border-radius: 4px;
  font-size: 0.875rem;
}
</style>
