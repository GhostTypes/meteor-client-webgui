<template>
  <div class="module-list">
    <h2>{{ category }}</h2>
    <div class="modules">
      <ModuleCard
        v-for="module in modules"
        :key="module.name"
        :module="module"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ModuleInfo } from '../stores/modules'
import ModuleCard from './ModuleCard.vue'

defineProps<{
  category: string
  modules: ModuleInfo[]
}>()
</script>

<style scoped>
.module-list {
  max-width: 1200px;
}

.module-list h2 {
  font-size: 1.75rem;
  margin-bottom: 1.5rem;
  color: #4ba6ff;
  text-transform: capitalize;
}

.modules {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}
</style>
