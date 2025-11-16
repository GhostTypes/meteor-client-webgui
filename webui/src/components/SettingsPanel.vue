<template>
  <div class="settings-panel">
    <div
      v-for="group in module.settingGroups"
      :key="group.name"
      class="setting-group"
    >
      <div v-if="group.name" class="group-header">
        <span>{{ group.name }}</span>
      </div>
      <div
        v-for="setting in group.settings"
        :key="setting.name"
        class="setting-item setting-field"
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
// Basic Settings
import BoolSetting from './settings/BoolSetting.vue'
import IntSetting from './settings/IntSetting.vue'
import DoubleSetting from './settings/DoubleSetting.vue'
import StringSetting from './settings/StringSetting.vue'
import EnumSetting from './settings/EnumSetting.vue'
import GenericSetting from './settings/GenericSetting.vue'
import KeybindSetting from './settings/KeybindSetting.vue'
import RegistryValueSetting from './settings/RegistryValueSetting.vue'
import FontFaceSetting from './settings/FontFaceSetting.vue'
import PotionSetting from './settings/PotionSetting.vue'

// Phase 3: Advanced Settings
import ColorSetting from './settings/ColorSetting.vue'
import BlockPosSetting from './settings/BlockPosSetting.vue'
import Vector3dSetting from './settings/Vector3dSetting.vue'
import StringListSetting from './settings/StringListSetting.vue'
import BlockListSetting from './settings/BlockListSetting.vue'
import ItemListSetting from './settings/ItemListSetting.vue'
import EntityTypeListSetting from './settings/EntityTypeListSetting.vue'
import ModuleListSetting from './settings/ModuleListSetting.vue'
import ColorListSetting from './settings/ColorListSetting.vue'
import StatusEffectAmplifierMapSetting from './settings/StatusEffectAmplifierMapSetting.vue'
import GenericListSetting from './settings/GenericListSetting.vue'

defineProps<{
  module: ModuleInfo
}>()

function getSettingComponent(type: string) {
  switch (type) {
    // Basic types
    case 'BOOL': return BoolSetting
    case 'INT': return IntSetting
    case 'DOUBLE': return DoubleSetting
    case 'STRING':
    case 'PROVIDED_STRING': return StringSetting
    case 'ENUM': return EnumSetting
    case 'KEYBIND': return KeybindSetting
    case 'BLOCK':
    case 'ITEM': return RegistryValueSetting
    case 'POTION': return PotionSetting
    case 'FONT_FACE': return FontFaceSetting

    // Phase 3: Advanced types
    case 'COLOR': return ColorSetting
    case 'BLOCK_POS': return BlockPosSetting
    case 'VECTOR3D': return Vector3dSetting

    // List types
    case 'STRING_LIST': return StringListSetting
    case 'BLOCK_LIST': return BlockListSetting
    case 'STORAGE_BLOCK_LIST': return BlockListSetting // Same as BLOCK_LIST
    case 'ITEM_LIST': return ItemListSetting
    case 'ENTITY_TYPE_LIST': return EntityTypeListSetting
    case 'MODULE_LIST': return ModuleListSetting
    case 'COLOR_LIST': return ColorListSetting

    // Map types
    case 'STATUS_EFFECT_AMPLIFIER_MAP': return StatusEffectAmplifierMapSetting

    // Generic fallback for other list types
    case 'ENCHANTMENT_LIST':
    case 'PARTICLE_TYPE_LIST':
    case 'SOUND_EVENT_LIST':
    case 'STATUS_EFFECT_LIST':
    case 'SCREEN_HANDLER_LIST':
    case 'PACKET_LIST':
      return GenericListSetting

    default: return GenericSetting
  }
}
</script>

<style scoped>
.settings-panel {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.group-header {
  text-transform: uppercase;
  font-size: 0.75rem;
  letter-spacing: 0.3em;
  color: var(--color-text-muted);
  margin-bottom: 0.85rem;
  position: sticky;
  top: 0;
  z-index: 1;
  background: linear-gradient(180deg, rgba(9, 12, 18, 0.98), rgba(9, 12, 18, 0.85));
  padding: 0.35rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.setting-item {
  margin-bottom: 0.75rem;
}
</style>
