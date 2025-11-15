<template>
  <div class="setting-item">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
      <span class="count">{{ items.length }} items</span>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <!-- Add new string input -->
    <div class="add-input">
      <input
        v-model="newItem"
        type="text"
        placeholder="Enter text..."
        @keyup.enter="addItem"
        class="text-input"
      />
      <button @click="addItem" class="add-button" :disabled="!newItem.trim()">
        Add
      </button>
    </div>

    <!-- Selected strings as chips -->
    <div v-if="items.length > 0" class="items-list">
      <div v-for="(item, index) in items" :key="index" class="item-chip">
        <span>{{ item }}</span>
        <button @click="removeItem(index)" class="remove-button">×</button>
      </div>
    </div>

    <div v-else class="empty-message">No items added yet</div>
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
const newItem = ref('')

const items = computed(() => props.setting.value.items || [])

function addItem() {
  const trimmed = newItem.value.trim()
  if (!trimmed) return

  // Check for duplicates
  if (items.value.includes(trimmed)) {
    newItem.value = ''
    return
  }

  const updated = [...items.value, trimmed]
  updateValue(updated)
  newItem.value = ''
}

function removeItem(index: number) {
  const updated = items.value.filter((_, i) => i !== index)
  updateValue(updated)
}

function updateValue(items: string[]) {
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

.add-input {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.text-input {
  flex: 1;
  padding: 0.5rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  color: var(--color-text);
  font-size: 0.875rem;
}

.text-input:focus {
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
  transition: opacity 0.2s;
}

.add-button:hover:not(:disabled) {
  opacity: 0.9;
}

.add-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.items-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.item-chip {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem 0.625rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  font-size: 0.875rem;
  color: var(--color-text);
}

.remove-button {
  background: none;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 1.25rem;
  line-height: 1;
  padding: 0;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
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
