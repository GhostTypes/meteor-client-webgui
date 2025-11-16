<template>
  <div class="setting-item">
    <div class="setting-header">
      <span class="setting-title">{{ setting.title }}</span>
      <span class="current-value">{{ setting.value.label || 'None' }}</span>
    </div>
    <p class="setting-description">{{ setting.description }}</p>

    <div class="control-grid">
      <label class="input-label">Input Type</label>
      <select v-model="inputType" class="select-input">
        <option value="key">Keyboard</option>
        <option value="mouse">Mouse</option>
      </select>

      <template v-if="inputType === 'key'">
        <label class="input-label">Key</label>
        <select v-model.number="selectedKey" class="select-input">
          <option disabled :value="undefined">Select key…</option>
          <option v-for="option in keyOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </template>
      <template v-else>
        <label class="input-label">Mouse Button</label>
        <select v-model.number="selectedMouse" class="select-input">
          <option disabled :value="undefined">Select button…</option>
          <option v-for="option in mouseOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </template>

      <label class="input-label">Custom Code</label>
      <input
        type="number"
        class="text-input"
        v-model.number="customValue"
        placeholder="GLFW code"
      />
    </div>

    <div class="modifiers">
      <label><input type="checkbox" v-model="modifiers.ctrl" /> Ctrl</label>
      <label><input type="checkbox" v-model="modifiers.alt" /> Alt</label>
      <label><input type="checkbox" v-model="modifiers.shift" /> Shift</label>
      <label><input type="checkbox" v-model="modifiers.super" /> Super</label>
    </div>

    <div class="actions">
      <button class="apply" @click="applyBinding">Apply</button>
      <button class="clear" @click="clearBinding">Clear</button>
    </div>

    <p class="helper-text">
      Values map to GLFW key codes. Use the dropdown for common inputs or enter a custom code if needed.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { ModuleInfo, SettingMetadata } from '../../stores/modules'
import { useWebSocketStore } from '../../stores/websocket'

const props = defineProps<{
  module: ModuleInfo
  setting: SettingMetadata
}>()

const wsStore = useWebSocketStore()

const KEY_FLAGS = {
  SHIFT: 0x0001,
  CTRL: 0x0002,
  ALT: 0x0004,
  SUPER: 0x0008
}

const letters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('').map(letter => ({
  label: letter,
  value: letter.charCodeAt(0)
}))

const digits = '0123456789'.split('').map(number => ({
  label: number,
  value: number.charCodeAt(0)
}))

const functionKeys = Array.from({ length: 12 }, (_, index) => ({
  label: `F${index + 1}`,
  value: 290 + index
}))

const miscKeys = [
  { label: 'Space', value: 32 },
  { label: 'Tab', value: 258 },
  { label: 'Enter', value: 257 },
  { label: 'Backspace', value: 259 },
  { label: 'Insert', value: 260 },
  { label: 'Delete', value: 261 },
  { label: 'Home', value: 268 },
  { label: 'End', value: 269 },
  { label: 'Page Up', value: 266 },
  { label: 'Page Down', value: 267 },
  { label: 'Up Arrow', value: 265 },
  { label: 'Down Arrow', value: 264 },
  { label: 'Left Arrow', value: 263 },
  { label: 'Right Arrow', value: 262 }
]

const keyOptions = [...letters, ...digits, ...functionKeys, ...miscKeys]

const mouseOptions = [
  { label: 'Left Button', value: 0 },
  { label: 'Right Button', value: 1 },
  { label: 'Middle Button', value: 2 },
  { label: 'Button 4', value: 3 },
  { label: 'Button 5', value: 4 }
]

const inputType = ref<'key' | 'mouse'>(props.setting.value.isKey ? 'key' : 'mouse')
const selectedKey = ref<number | undefined>()
const selectedMouse = ref<number | undefined>()
const customValue = ref<number>(props.setting.value.value ?? -1)

const modifiers = reactive({
  shift: false,
  ctrl: false,
  alt: false,
  super: false
})

function hydrateFromValue() {
  inputType.value = props.setting.value.isKey ? 'key' : 'mouse'
  customValue.value = Number(props.setting.value.value ?? -1)
  selectedKey.value = props.setting.value.isKey ? customValue.value : undefined
  selectedMouse.value = !props.setting.value.isKey ? customValue.value : undefined

  const currentModifiers = props.setting.value.modifiers ?? 0
  modifiers.shift = (currentModifiers & KEY_FLAGS.SHIFT) !== 0
  modifiers.ctrl = (currentModifiers & KEY_FLAGS.CTRL) !== 0
  modifiers.alt = (currentModifiers & KEY_FLAGS.ALT) !== 0
  modifiers.super = (currentModifiers & KEY_FLAGS.SUPER) !== 0
}

hydrateFromValue()

watch(
  () => props.setting.value,
  () => hydrateFromValue(),
  { deep: true }
)

watch(selectedKey, value => {
  if (inputType.value === 'key' && typeof value === 'number') {
    customValue.value = value
  }
})

watch(selectedMouse, value => {
  if (inputType.value === 'mouse' && typeof value === 'number') {
    customValue.value = value
  }
})

function currentModifiers() {
  let result = 0
  if (modifiers.shift) result |= KEY_FLAGS.SHIFT
  if (modifiers.ctrl) result |= KEY_FLAGS.CTRL
  if (modifiers.alt) result |= KEY_FLAGS.ALT
  if (modifiers.super) result |= KEY_FLAGS.SUPER
  return result
}

function applyBinding() {
  if (Number.isNaN(customValue.value)) return

  wsStore.send({
    type: 'setting.update',
    data: {
      moduleName: props.module.name,
      settingName: props.setting.name,
      value: {
        isKey: inputType.value === 'key',
        value: customValue.value,
        modifiers: currentModifiers()
      }
    }
  })
}

function clearBinding() {
  modifiers.shift = modifiers.ctrl = modifiers.alt = modifiers.super = false
  customValue.value = -1
  selectedKey.value = undefined
  selectedMouse.value = undefined
  wsStore.send({
    type: 'setting.update',
    data: {
      moduleName: props.module.name,
      settingName: props.setting.name,
      value: {
        isKey: true,
        value: -1,
        modifiers: 0
      }
    }
  })
}
</script>

<style scoped>
.setting-item {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.current-value {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
}

.control-grid {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 0.5rem 1rem;
  align-items: center;
}

.input-label {
  font-size: 0.85rem;
  color: var(--color-text-secondary);
}

.select-input,
.text-input {
  width: 100%;
  padding: 0.45rem 0.5rem;
  border: 1px solid var(--color-border);
  border-radius: 4px;
  background: var(--color-background-soft);
  color: var(--color-text);
}

.modifiers {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  font-size: 0.85rem;
}

.modifiers label {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.actions {
  display: flex;
  gap: 0.5rem;
}

.apply,
.clear {
  padding: 0.45rem 1rem;
  border-radius: 4px;
  border: none;
  cursor: pointer;
}

.apply {
  background: var(--color-primary);
  color: #fff;
}

.clear {
  background: transparent;
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
}

.helper-text {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
}
</style>
