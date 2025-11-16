<template>
  <div class="module-list">
    <div class="list-header">
      <div>
        <p class="eyebrow">Category</p>
        <h2>{{ category }}</h2>
      </div>
      <p class="module-count">{{ modules.length }} modules</p>
    </div>

    <div
      v-if="modules.length"
      class="modules"
      :class="[`density-${density}`]"
    >
      <component
        v-for="module in modules"
        :key="module.name"
        :is="density === 'compact' ? ModuleCardCompact : ModuleCard"
        :module="module"
        @open-settings="emit('open-settings', $event)"
      />
    </div>

    <div v-else class="empty-hint">
      <p>{{ emptyMessage || 'No modules match the current filters.' }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ModuleInfo } from '../stores/modules'
import ModuleCard from './ModuleCard.vue'
import ModuleCardCompact from './ModuleCardCompact.vue'

defineProps<{
  category: string
  modules: ModuleInfo[]
  density: 'comfortable' | 'compact'
  emptyMessage?: string
}>()

const emit = defineEmits<{
  (event: 'open-settings', module: ModuleInfo): void
}>()
</script>

<style scoped>
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 1.5rem;
}

.eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.2em;
  font-size: 0.65rem;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}

.module-list h2 {
  font-size: 1.85rem;
  margin: 0;
  text-transform: capitalize;
}

.module-count {
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

.modules {
  display: grid;
  gap: 1.25rem;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
}

.modules.density-compact {
  gap: 0.75rem;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
}

.empty-hint {
  border: 1px dashed rgba(255, 255, 255, 0.15);
  border-radius: var(--radius-md);
  padding: 2rem;
  text-align: center;
  color: var(--color-text-muted);
}
</style>
