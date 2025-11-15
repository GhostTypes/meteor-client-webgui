import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export interface ModuleInfo {
  name: string
  title: string
  description: string
  category: string
  active: boolean
  addon: string
  settingGroups?: SettingGroup[]
}

export interface SettingGroup {
  name: string
  settings: SettingMetadata[]
}

export interface SettingMetadata {
  name: string
  title: string
  description: string
  type: string
  value: any
  defaultValue: any
  visible: boolean
  typeMetadata?: any
}

export const useModulesStore = defineStore('modules', () => {
  const byCategory = ref<Record<string, ModuleInfo[]>>({})
  const loading = ref(true)
  const error = ref<string | null>(null)
  const favorites = ref<string[]>([])
  const FAVORITES_KEY = 'meteor-client:favorites'

  function hydrateFavorites() {
    if (typeof window === 'undefined') return
    try {
      const stored = window.localStorage.getItem(FAVORITES_KEY)
      if (!stored) return
      const parsed = JSON.parse(stored)
      if (Array.isArray(parsed)) {
        favorites.value = Array.from(
          new Set(parsed.filter((value): value is string => typeof value === 'string'))
        )
      }
    } catch (err) {
      console.warn('Failed to parse favorites from storage', err)
    }
  }

  hydrateFavorites()

  watch(
    favorites,
    (value) => {
      if (typeof window === 'undefined') return
      window.localStorage.setItem(FAVORITES_KEY, JSON.stringify(value))
    },
    { deep: true }
  )

  const activeModules = computed(() => {
    const active: string[] = []
    Object.values(byCategory.value).forEach(modules => {
      modules.forEach(module => {
        if (module.active) {
          active.push(module.name)
        }
      })
    })
    return active
  })

  const categories = computed(() => Object.keys(byCategory.value).sort())

  const favoriteModules = computed(() => {
    if (!favorites.value.length) return []
    const resolved: ModuleInfo[] = []
    for (const name of favorites.value) {
      const module = getModule(name)
      if (module) {
        resolved.push(module)
      }
    }
    return resolved
  })

  function setInitialState(modules: Record<string, ModuleInfo[]>) {
    byCategory.value = modules
    loading.value = false
    error.value = null
  }

  function updateModuleState(moduleName: string, active: boolean) {
    // Find and update the module
    for (const category in byCategory.value) {
      const module = byCategory.value[category].find(m => m.name === moduleName)
      if (module) {
        module.active = active
        return
      }
    }
  }

  function updateSettingValue(moduleName: string, settingName: string, value: any) {
    // Find the module and update the setting
    for (const category in byCategory.value) {
      const module = byCategory.value[category].find(m => m.name === moduleName)
      if (module && module.settingGroups) {
        for (const group of module.settingGroups) {
          const setting = group.settings.find(s => s.name === settingName)
          if (setting) {
            setting.value = value
            return
          }
        }
      }
    }
  }

  function toggleFavorite(moduleName: string) {
    const next = new Set(favorites.value)
    if (next.has(moduleName)) {
      next.delete(moduleName)
    } else {
      next.add(moduleName)
    }
    favorites.value = Array.from(next)
  }

  function isFavorite(moduleName: string) {
    return favorites.value.includes(moduleName)
  }

  function getModule(moduleName: string): ModuleInfo | null {
    for (const category in byCategory.value) {
      const module = byCategory.value[category].find(m => m.name === moduleName)
      if (module) return module
    }
    return null
  }

  return {
    byCategory,
    loading,
    error,
    activeModules,
    categories,
    favoriteModules,
    favorites,
    setInitialState,
    updateModuleState,
    updateSettingValue,
    getModule,
    toggleFavorite,
    isFavorite
  }
})
