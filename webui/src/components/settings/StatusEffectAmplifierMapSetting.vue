<template>
  <div class="setting-item">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
      <span class="count">{{ entries.length }} effects</span>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <!-- Add New Effect -->
    <div v-if="!wsStore.registries?.statusEffects" class="loading">
      Loading status effects...
    </div>
    <div v-else class="add-entry">
      <select v-model="newEffect" class="effect-select">
        <option value="">Select Effect...</option>
        <option v-for="effect in availableEffects" :key="effect.id" :value="effect.id">
          {{ formatEffectName(effect.id) }}
        </option>
      </select>

      <input
        type="number"
        v-model.number="newAmplifier"
        min="0"
        max="255"
        placeholder="Amplifier (0-255)"
        class="amplifier-input"
      />

      <button @click="addEntry" :disabled="!newEffect" class="add-button">
        Add
      </button>
    </div>

    <!-- Effect List -->
    <div v-if="entries.length > 0" class="entry-list">
      <div v-for="(entry, index) in entries" :key="index" class="entry-item">
        <div class="entry-info">
          <span class="effect-name">{{ formatEffectName(entry.effect) }}</span>
          <span class="amplifier-badge">Level {{ entry.amplifier + 1 }}</span>
        </div>
        <button @click="removeEntry(index)" class="remove-button">×</button>
      </div>
    </div>

    <div v-else class="empty-message">No status effects configured</div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { ModuleInfo, SettingMetadata } from '../../stores/modules'
import { useWebSocketStore } from '../../stores/websocket'

const props = defineProps<{
  module: ModuleInfo
  setting: SettingMetadata
}>()

const wsStore = useWebSocketStore()

// Request statusEffects registry on mount if not already loaded
onMounted(() => {
  if (!wsStore.registries?.statusEffects) {
    wsStore.requestRegistry('statusEffects')
  }
})
const newEffect = ref('')
const newAmplifier = ref(0)

const entries = computed(() => props.setting.value.entries || [])

const availableEffects = computed(() => {
  return wsStore.registries?.statusEffects || []
})

function formatEffectName(id: string) {
  const name = id.split(':')[1] || id
  return name.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}

function addEntry() {
  if (!newEffect.value) return

  // Check if effect already exists
  if (entries.value.some(e => e.effect === newEffect.value)) {
    // Update existing
    const updated = entries.value.map(e =>
      e.effect === newEffect.value
        ? { ...e, amplifier: Math.max(0, Math.min(255, newAmplifier.value)) }
        : e
    )
    updateValue(updated)
  } else {
    // Add new
    const updated = [
      ...entries.value,
      {
        effect: newEffect.value,
        amplifier: Math.max(0, Math.min(255, newAmplifier.value))
      }
    ]
    updateValue(updated)
  }

  // Reset
  newEffect.value = ''
  newAmplifier.value = 0
}

function removeEntry(index: number) {
  const updated = entries.value.filter((_, i) => i !== index)
  updateValue(updated)
}

function updateValue(entries: Array<{ effect: string; amplifier: number }>) {
  wsStore.send({
    type: 'setting.update',
    data: {
      moduleName: props.module.name,
      settingName: props.setting.name,
      value: { entries }
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

.add-entry {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.effect-select {
  flex: 1;
  padding: 0.5rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  color: var(--color-text);
  font-size: 0.875rem;
  cursor: pointer;
}

.effect-select:focus {
  outline: none;
  border-color: var(--color-primary);
}

.amplifier-input {
  width: 150px;
  padding: 0.5rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  color: var(--color-text);
  font-size: 0.875rem;
  font-family: monospace;
}

.amplifier-input:focus {
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
}

.add-button:hover:not(:disabled) {
  opacity: 0.9;
}

.add-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.entry-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.entry-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0.75rem;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 4px;
}

.entry-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.effect-name {
  font-weight: 500;
  color: var(--color-text);
}

.amplifier-badge {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  padding: 0.25rem 0.5rem;
  background: var(--color-background-mute);
  border-radius: 4px;
  font-family: monospace;
}

.remove-button {
  background: none;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 1.25rem;
  line-height: 1;
  padding: 0;
  width: 24px;
  height: 24px;
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

.loading {
  padding: 2rem;
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  font-style: italic;
}
</style>
