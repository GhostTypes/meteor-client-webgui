<template>
  <section class="module-toolbar">
    <div class="category-row" v-if="categories.length">
      <button
        v-for="category in categories"
        :key="category"
        class="pill-button"
        :class="{ active: selectedCategory === category }"
        @click="emit('update:selectedCategory', category)"
      >
        {{ category }}
      </button>
    </div>

    <div class="toolbar-controls">
      <label class="search-field">
        <span class="sr-only">Search modules</span>
        <input
          type="search"
          placeholder="Search modules…"
          :value="searchQuery"
          @input="emit('update:searchQuery', ($event.target as HTMLInputElement).value)"
        />
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path
            d="M15.5 14h-.79l-.28-.27A6 6 0 0 0 16 9a6 6 0 1 0-6 6 6 6 0 0 0 4.73-2.22l.27.28v.79L20 20.49 21.49 19zM10 14a4 4 0 1 1 0-8 4 4 0 0 1 0 8Z"
          />
        </svg>
      </label>

      <label class="toggle-active" :class="{ disabled: activeToggleDisabled }">
        <input
          type="checkbox"
          :checked="showActiveOnly"
          :disabled="activeToggleDisabled"
          @change="emit('update:showActiveOnly', ($event.target as HTMLInputElement).checked)"
        />
        <span>Active only</span>
      </label>

      <div class="density-toggle" role="group" aria-label="Card density">
        <button
          :class="{ active: density === 'comfortable' }"
          @click="emit('update:density', 'comfortable')"
        >
          Comfortable
        </button>
        <button
          :class="{ active: density === 'compact' }"
          @click="emit('update:density', 'compact')"
        >
          Compact
        </button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  categories: string[]
  selectedCategory: string | null
  searchQuery: string
  showActiveOnly: boolean
  density: 'comfortable' | 'compact'
  activeToggleDisabled?: boolean
}>()

const emit = defineEmits<{
  (event: 'update:selectedCategory', value: string | null): void
  (event: 'update:searchQuery', value: string): void
  (event: 'update:showActiveOnly', value: boolean): void
  (event: 'update:density', value: 'comfortable' | 'compact'): void
}>()
</script>

<style scoped>
.module-toolbar {
  background: rgba(16, 20, 26, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: var(--radius-md);
  padding: 1rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  box-shadow: var(--shadow-soft);
}

.category-row {
  display: flex;
  gap: 0.5rem;
  overflow-x: auto;
  padding-bottom: 0.25rem;
}

.category-row::-webkit-scrollbar {
  height: 6px;
}

.toolbar-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  align-items: center;
}

.search-field {
  flex: 1;
  position: relative;
  min-width: 240px;
}

.search-field input {
  padding-left: 2.75rem;
}

.search-field svg {
  position: absolute;
  width: 20px;
  height: 20px;
  left: 0.9rem;
  top: 50%;
  transform: translateY(-50%);
  fill: var(--color-text-muted);
}

.toggle-active {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--color-text-muted);
  font-size: 0.9rem;
  padding: 0.35rem 0.8rem 0.35rem 0.35rem;
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-radius: 999px;
}

.toggle-active.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.toggle-active input:disabled {
  cursor: not-allowed;
}

.density-toggle {
  display: inline-flex;
  padding: 0.25rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  gap: 0.25rem;
}

.density-toggle button {
  background: transparent;
  border-radius: 999px;
  padding: 0.35rem 0.9rem;
  font-size: 0.85rem;
  color: var(--color-text-muted);
}

.density-toggle button.active {
  background: rgba(75, 166, 255, 0.15);
  color: var(--color-text);
  box-shadow: 0 5px 18px rgba(75, 166, 255, 0.2);
}

@media (max-width: 768px) {
  .toolbar-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .toggle-active,
  .density-toggle {
    justify-content: space-between;
  }
}
</style>
