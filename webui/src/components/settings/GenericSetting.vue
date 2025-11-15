<template>
  <div class="generic-setting">
    <div class="setting-info">
      <span class="setting-title">{{ setting.title }}</span>
      <p class="setting-description">{{ setting.description }}</p>
      <span class="setting-type-badge">{{ setting.type }}</span>
      <pre class="setting-value">{{ JSON.stringify(setting.value, null, 2) }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ModuleInfo, SettingMetadata } from '../../stores/modules'

defineProps<{
  module: ModuleInfo
  setting: SettingMetadata
}>()
</script>

<style scoped>
.generic-setting {
  padding: 0.75rem;
  background: #2a2a2a;
  border: 1px solid #444;
  border-radius: 4px;
}

.setting-info {
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

.setting-type-badge {
  display: inline-block;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  background: #1a1a1a;
  color: #666;
  border-radius: 4px;
  align-self: flex-start;
}

.setting-value {
  font-size: 0.75rem;
  color: #999;
  background: #1a1a1a;
  padding: 0.5rem;
  border-radius: 4px;
  overflow-x: auto;
}
</style>
