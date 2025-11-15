<template>
  <div class="bool-setting">
    <label>
      <input
        type="checkbox"
        :checked="setting.value.value"
        @change="updateValue($event.target.checked)"
      />
      <div class="setting-info">
        <span class="setting-title">{{ setting.title }}</span>
        <span class="setting-description">{{ setting.description }}</span>
      </div>
    </label>
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

function updateValue(value: boolean) {
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
.bool-setting label {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  cursor: pointer;
}

.bool-setting input[type="checkbox"] {
  margin-top: 2px;
  width: 18px;
  height: 18px;
  cursor: pointer;
}

.setting-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.setting-title {
  font-size: 0.9rem;
  color: #fff;
}

.setting-description {
  font-size: 0.8rem;
  color: #888;
}
</style>
