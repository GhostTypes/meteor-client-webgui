<template>
  <div class="settings-panel">
    <div
      v-for="group in module.settingGroups"
      :key="group.name"
      class="setting-group"
    >
      <h4 v-if="group.name">{{ group.name }}</h4>
      <div
        v-for="setting in group.settings"
        :key="setting.name"
        class="setting-item"
        v-show="setting.visible"
      >
        <component
          :is="getSettingComponent(setting.type)"
          :module="module"
          :setting="setting"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ModuleInfo } from '../stores/modules'
import BoolSetting from './settings/BoolSetting.vue'
import IntSetting from './settings/IntSetting.vue'
import DoubleSetting from './settings/DoubleSetting.vue'
import StringSetting from './settings/StringSetting.vue'
import EnumSetting from './settings/EnumSetting.vue'
import GenericSetting from './settings/GenericSetting.vue'

defineProps<{
  module: ModuleInfo
}>()

function getSettingComponent(type: string) {
  switch (type) {
    case 'BOOL': return BoolSetting
    case 'INT': return IntSetting
    case 'DOUBLE': return DoubleSetting
    case 'STRING':
    case 'PROVIDED_STRING': return StringSetting
    case 'ENUM': return EnumSetting
    default: return GenericSetting
  }
}
</script>

<style scoped>
.settings-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.setting-group h4 {
  font-size: 0.875rem;
  color: #888;
  text-transform: uppercase;
  margin-bottom: 0.75rem;
  letter-spacing: 0.5px;
}

.setting-item {
  margin-bottom: 0.75rem;
}
</style>
