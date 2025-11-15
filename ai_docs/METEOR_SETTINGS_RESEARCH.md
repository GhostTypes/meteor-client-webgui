# Meteor Client Settings & Module System Research

## Overview
This document details the research on Meteor Client's module and settings system for the WebGUI addon.

## Module System

### Module Class Structure
- **Location**: `meteordevelopment.meteorclient.systems.modules.Module`
- **Key Properties**:
  - `name`: Internal module name
  - `title`: Display title (generated from name)
  - `description`: Module description
  - `category`: Module category
  - `active`: Boolean state (enabled/disabled)
  - `settings`: Settings collection
  - `keybind`: Module keybinding
  - `color`: Module color (auto-generated from HSV)

### Modules System (Registry)
- **Location**: `meteordevelopment.meteorclient.systems.modules.Modules`
- **Access Pattern**: `Modules.get()` - Singleton instance
- **Key Methods**:
  - `getAll()`: Returns all module instances
  - `getGroup(Category)`: Returns modules by category
  - `get(Class<T>)`: Get specific module by class
  - `get(String)`: Get module by name
  - `getActive()`: Returns list of active modules

### Categories
- Categories are enum-like objects registered via `Modules.registerCategory()`
- Default categories: Combat, Player, Movement, Render, World, Misc
- Addons can register custom categories via `onRegisterCategories()`

## Settings System

### Base Setting Class
- **Location**: `meteordevelopment.meteorclient.settings.Setting<T>`
- **Generic Type**: Each setting is typed (e.g., `Setting<Boolean>`, `Setting<Integer>`)
- **Key Properties**:
  - `name`: Internal setting name
  - `title`: Display title
  - `description`: Setting description
  - `value`: Current value
  - `defaultValue`: Default value
  - `visible`: Visibility predicate (IVisible)
  - `onChanged`: Callback when value changes
  - `module`: Parent module reference

### Setting Types

#### Primitive Types
1. **BoolSetting**
   - Type: `Boolean`
   - Value: true/false
   - Parse: "true", "false", "toggle", "1", "0"

2. **IntSetting**
   - Type: `Integer`
   - Properties: `min`, `max`, `sliderMin`, `sliderMax`, `noSlider`
   - Validation: Value must be between min and max
   - UI: Can have slider or input field

3. **DoubleSetting**
   - Type: `Double`
   - Properties: `min`, `max`, `sliderMin`, `sliderMax`, `noSlider`, `decimalPlaces`
   - Similar to IntSetting but for decimals

4. **StringSetting**
   - Type: `String`
   - Simple text input

5. **EnumSetting<T>**
   - Type: Generic enum
   - Values: Auto-populated from enum constants
   - Parse: Case-insensitive string matching

#### Complex Types
6. **ColorSetting**
   - Type: `SettingColor` (RGBA)
   - Format: "R G B A" (0-255 each)
   - Has built-in validation

7. **KeybindSetting**
   - Type: `Keybind`
   - Stores key combinations

8. **BlockSetting**
   - Type: `Block`
   - Single block selector

9. **ItemSetting**
   - Type: `Item`
   - Single item selector

10. **PotionSetting**
    - Type: Potion/StatusEffect
    - Potion selector

#### List/Collection Types
11. **BlockListSetting**
    - Type: `List<Block>`
    - Multiple block selection

12. **ItemListSetting**
    - Type: `List<Item>`
    - Multiple item selection

13. **EntityTypeListSetting**
    - Type: `List<EntityType>`
    - Entity type selection

14. **ModuleListSetting**
    - Type: `List<Module>`
    - Module selection list

15. **EnchantmentListSetting**
    - Type: `List<Enchantment>`
    - Enchantment selection

16. **ParticleTypeListSetting**
    - Type: `List<ParticleType>`
    - Particle type selection

17. **SoundEventListSetting**
    - Type: `List<SoundEvent>`
    - Sound event selection

18. **StatusEffectListSetting**
    - Type: `List<StatusEffect>`
    - Status effect selection

19. **StringListSetting**
    - Type: `List<String>`
    - List of strings

20. **ColorListSetting**
    - Type: `List<SettingColor>`
    - List of colors

21. **PacketListSetting**
    - Type: Packet boolean map
    - Packet filtering

#### Map Types
22. **StatusEffectAmplifierMapSetting**
    - Type: `Map<StatusEffect, Integer>`
    - Maps status effects to amplifier levels

23. **BlockDataSetting**
    - Type: Block data with NBT
    - Complex block data storage

#### Other Specialized Types
24. **BlockPosSetting**
    - Type: `BlockPos` (3D coordinates)
    - X, Y, Z position

25. **Vector3dSetting**
    - Type: `Vec3d`
    - 3D vector

26. **FontFaceSetting**
    - Type: Font configuration
    - Font family selection

27. **ProvidedStringSetting**
    - Type: `String`
    - String with provided options

28. **GenericSetting<T>**
    - Type: Generic
    - Base for custom settings

29. **ScreenHandlerListSetting**
    - Type: Screen handler list
    - GUI screen type filtering

30. **StorageBlockListSetting**
    - Type: Storage blocks
    - Container block selection

## Setting Groups
- Settings are organized into `SettingGroup` objects
- Each module has a `Settings` collection containing multiple `SettingGroup`s
- Groups provide logical organization in the GUI

## Serialization
- All settings implement `ISerializable<T>`
- Use NBT (Named Binary Tag) format for save/load
- Methods: `toTag()`, `fromTag(NbtCompound)`

## Key Implementation Patterns

### Getting All Settings from a Module
```java
Module module = Modules.get().get("module-name");
for (SettingGroup group : module.settings) {
    for (Setting<?> setting : group) {
        // Access setting properties
        String name = setting.name;
        Object value = setting.get();
        Object defaultValue = setting.getDefaultValue();
    }
}
```

### Setting Values
```java
Setting<?> setting = // ... get setting
setting.set(newValue); // Type-safe, validates value
setting.parse("string-value"); // Parse from string
```

### Module Toggle
```java
Module module = Modules.get().get("module-name");
module.toggle(); // Toggles on/off
boolean isActive = module.isActive();
```

## WebGUI Implementation Considerations

### Type Mapping
Each setting type needs a corresponding WebUI component:
- **Bool** → Checkbox/Toggle
- **Int/Double** → Slider + Number input
- **String** → Text input
- **Enum** → Dropdown/Select
- **Color** → Color picker
- **List Types** → Multi-select with search
- **Map Types** → Key-value editor
- **Vector/BlockPos** → Multi-input (X, Y, Z)

### Reflection Requirements
To build a generic system that works with any setting:
1. Use `setting.getClass()` to determine setting type
2. Access type-specific properties (e.g., `IntSetting.min`, `IntSetting.max`)
3. Use generic `setting.get()` and `setting.set(Object)` for value access
4. Handle type casting based on runtime type detection

### Real-time Sync Challenges
1. **Setting Changes**: Need to listen for `onChanged` callbacks
2. **Module Toggles**: Monitor `ActiveModulesChangedEvent`
3. **Visibility**: Settings can be conditionally visible (IVisible)
4. **Validation**: Values must pass `isValueValid()` check

### Security Considerations
- All setting changes should validate via `isValueValid()`
- Type safety must be maintained (can't set wrong type)
- Some settings may have side effects via `onChanged` callbacks
