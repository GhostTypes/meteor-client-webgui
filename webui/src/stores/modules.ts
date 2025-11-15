import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

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
    setInitialState,
    updateModuleState,
    updateSettingValue,
    getModule
  }
})
