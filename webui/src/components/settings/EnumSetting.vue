<template>
  <div class="enum-setting">
    <label>
      <span class="setting-title">{{ setting.title }}</span>
      <p class="setting-description">{{ setting.description }}</p>
      <select
        :value="setting.value.value"
        @change="updateValue($event.target.value)"
      >
        <option
          v-for="option in setting.typeMetadata?.values || []"
          :key="option"
          :value="option"
        >
          {{ option }}
        </option>
      </select>
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

function updateValue(value: string) {
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
.enum-setting label {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.setting-title {
  font-size: 0.9rem;
  color: #fff;
}

.setting-description {
  font-size: 0.8rem;
  color: #888;
}

select {
  padding: 0.5rem;
  background: #2a2a2a;
  color: #fff;
  border: 1px solid #444;
  border-radius: 4px;
  font-size: 0.875rem;
  cursor: pointer;
}

select:focus {
  outline: none;
  border-color: #4ba6ff;
}
</style>
