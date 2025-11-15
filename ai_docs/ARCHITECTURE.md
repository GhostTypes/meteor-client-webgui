# Meteor WebGUI Architecture

## Project Overview
A web-based GUI addon for Meteor Client featuring:
- **Bi-directional communication** between Java addon and web interface
- **Real-time synchronization** of module states and settings
- **Dynamic mapping** of all modules and settings from Meteor Client and addons
- **Highly configurable** theming and customization

## Technology Stack

### Backend (Java Addon)
- **Minecraft Version**: 1.21.8
- **Fabric Loader**: 0.16.14
- **Meteor Client**: 1.21.8-56
- **WebSocket Library**: Java-WebSocket 1.5.7
- **JSON Library**: Gson 2.11.0

### Frontend (WebUI)
- **Framework**: Vue.js 3 with TypeScript
- **Build Tool**: Vite
- **Styling**: TailwindCSS (for rapid theming)
- **WebSocket Client**: Native WebSocket API
- **State Management**: Pinia (Vue 3 store)

## System Architecture

### High-Level Architecture
```
┌─────────────────────────────────────────────┐
│         Minecraft + Meteor Client           │
│  ┌───────────────────────────────────────┐  │
│  │     Meteor WebGUI Addon (Java)        │  │
│  │                                       │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │   Module Mapping System         │  │  │
│  │  │   - Scan all modules            │  │  │
│  │  │   - Scan all settings           │  │  │
│  │  │   - Category organization       │  │  │
│  │  └─────────────────────────────────┘  │  │
│  │                                       │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │   Settings Reflector            │  │  │
│  │  │   - Type detection              │  │  │
│  │  │   - Value reading/writing       │  │  │
│  │  │   - Validation                  │  │  │
│  │  └─────────────────────────────────┘  │  │
│  │                                       │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │   Event Monitoring System       │  │  │
│  │  │   - Module toggle events        │  │  │
│  │  │   - Setting change events       │  │  │
│  │  │   - Real-time broadcasting      │  │  │
│  │  └─────────────────────────────────┘  │  │
│  │                                       │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │   WebSocket Server              │  │  │
│  │  │   - localhost:8080              │  │  │
│  │  │   - Message routing             │  │  │
│  │  │   - Client management           │  │  │
│  │  └─────────────────────────────────┘  │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
                     ↕ WebSocket
┌─────────────────────────────────────────────┐
│         Web Browser (localhost:8080)        │
│  ┌───────────────────────────────────────┐  │
│  │        Vue.js WebUI Frontend          │  │
│  │                                       │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │   WebSocket Client              │  │  │
│  │  │   - Auto-reconnect              │  │  │
│  │  │   - Message handling            │  │  │
│  │  └─────────────────────────────────┘  │  │
│  │                                       │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │   Pinia State Store             │  │  │
│  │  │   - Modules state               │  │  │
│  │  │   - Settings state              │  │  │
│  │  │   - UI preferences              │  │  │
│  │  └─────────────────────────────────┘  │  │
│  │                                       │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │   UI Components                 │  │  │
│  │  │   - Module List (by category)   │  │  │
│  │  │   - Settings Panel              │  │  │
│  │  │   - Theme Configurator          │  │  │
│  │  └─────────────────────────────────┘  │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```

## Core Systems

### 1. Module Mapping System
**Purpose**: Dynamically discover and map all modules from Meteor Client and installed addons

**Implementation**:
```java
class ModuleMapper {
    // Scan all modules on addon initialization
    Map<String, List<ModuleInfo>> mapModulesByCategory();

    // Create serializable module info
    ModuleInfo createModuleInfo(Module module);
}

class ModuleInfo {
    String name;
    String title;
    String description;
    String category;
    boolean active;
    String addonName; // "Meteor Client" or addon name
    List<SettingGroupInfo> settingGroups;
}
```

**Key Features**:
- Iterate through `Modules.get().getAll()`
- Group by category using `module.category`
- Include addon information via `module.addon`
- Exclude HUD modules (check `category.name != "hud"`)

### 2. Settings Reflection System
**Purpose**: Generic system to read/write any setting type via reflection

**Implementation**:
```java
class SettingsReflector {
    // Get setting metadata including type-specific properties
    SettingMetadata getSettingMetadata(Setting<?> setting);

    // Get current value in JSON-serializable format
    Object getSettingValue(Setting<?> setting);

    // Set value from JSON (handles type conversion)
    boolean setSettingValue(Setting<?> setting, Object value);

    // Detect setting type for UI component mapping
    SettingType detectSettingType(Setting<?> setting);
}

enum SettingType {
    BOOL, INT, DOUBLE, STRING, ENUM,
    COLOR, KEYBIND, BLOCK, ITEM,
    BLOCK_LIST, ITEM_LIST, ENTITY_TYPE_LIST,
    MODULE_LIST, STRING_LIST, COLOR_LIST,
    BLOCK_POS, VECTOR3D,
    // ... other types
}

class SettingMetadata {
    String name;
    String title;
    String description;
    SettingType type;
    Object value;
    Object defaultValue;
    boolean visible;

    // Type-specific metadata
    Map<String, Object> typeMetadata; // e.g., min/max for Int, enum values, etc.
}
```

**Type-Specific Metadata Examples**:
- **IntSetting**: `{min: -100, max: 100, sliderMin: 0, sliderMax: 50, noSlider: false}`
- **EnumSetting**: `{values: ["Option1", "Option2", "Option3"]}`
- **ColorSetting**: `{r: 255, g: 100, b: 50, a: 255}`

**Reflection Strategy**:
1. Use `setting.getClass().getSimpleName()` for type detection
2. Cast to specific types to access properties (e.g., `((IntSetting)setting).min`)
3. Use `setting.get()` for value, `setting.set(T)` for updates
4. Handle `IVisible.isVisible()` for conditional visibility

### 3. Event Monitoring System
**Purpose**: Real-time event broadcasting to keep WebUI in sync

**Implementation**:
```java
class EventMonitor {
    @EventHandler
    void onModuleToggle(ActiveModulesChangedEvent event) {
        // Broadcast module state change to all WebSocket clients
        broadcastModuleStateChange(event.module);
    }

    // Custom event listener for setting changes
    void monitorSettingChanges(Module module) {
        for (SettingGroup group : module.settings) {
            for (Setting<?> setting : group) {
                // Wrap existing onChanged callback
                wrapOnChangedCallback(setting);
            }
        }
    }
}
```

**Events to Monitor**:
- `ActiveModulesChangedEvent`: Module toggle
- Setting `onChanged` callbacks: Setting value changes
- Custom event for module/setting visibility changes

### 4. WebSocket Communication Protocol
**Purpose**: Define message format and commands

**Message Format**:
```typescript
interface WSMessage {
    type: string;  // Message type
    data: any;     // Message payload
    id?: string;   // Request ID for request/response pattern
}
```

**Message Types**:

**Server → Client (Events)**:
- `module.state.changed`: Module toggled on/off
- `setting.value.changed`: Setting value changed
- `initial.state`: Full state on connection
- `error`: Error message

**Client → Server (Commands)**:
- `module.toggle`: Toggle module on/off
- `module.list`: Request module list
- `setting.update`: Update setting value
- `setting.get`: Get setting value

**Example Messages**:
```json
// Initial State (Server → Client)
{
    "type": "initial.state",
    "data": {
        "modules": {
            "Combat": [
                {
                    "name": "auto-crystal",
                    "title": "Auto Crystal",
                    "description": "Automatically places and breaks crystals",
                    "active": true,
                    "addon": "Meteor Client",
                    "settingGroups": [...]
                }
            ]
        }
    }
}

// Module Toggle (Client → Server)
{
    "type": "module.toggle",
    "data": {
        "moduleName": "auto-crystal"
    },
    "id": "req-123"
}

// Module State Changed (Server → Client)
{
    "type": "module.state.changed",
    "data": {
        "moduleName": "auto-crystal",
        "active": false
    }
}

// Setting Update (Client → Server)
{
    "type": "setting.update",
    "data": {
        "moduleName": "auto-crystal",
        "settingName": "place-delay",
        "value": 5
    },
    "id": "req-124"
}

// Setting Value Changed (Server → Client)
{
    "type": "setting.value.changed",
    "data": {
        "moduleName": "auto-crystal",
        "settingName": "place-delay",
        "value": 5
    }
}
```

### 5. WebSocket Server Implementation
**Technology**: Java-WebSocket library

**Features**:
- Listen on `localhost:8080`
- Serve static HTML/CSS/JS for WebUI
- Handle multiple simultaneous connections
- Broadcast events to all connected clients
- Request/response pattern for commands

**Implementation**:
```java
class MeteorWebServer extends WebSocketServer {
    void onOpen(WebSocket conn, ClientHandshake handshake);
    void onClose(WebSocket conn, int code, String reason, boolean remote);
    void onMessage(WebSocket conn, String message);
    void onError(WebSocket conn, Exception ex);

    // Broadcast to all clients
    void broadcastEvent(WSMessage message);

    // Handle specific message types
    void handleModuleToggle(WebSocket conn, JsonObject data);
    void handleSettingUpdate(WebSocket conn, JsonObject data);
}
```

## Frontend Architecture

### Component Structure
```
src/
├── main.ts                 # App entry point
├── App.vue                 # Root component
├── stores/
│   ├── modules.ts          # Pinia store for modules state
│   ├── settings.ts         # Pinia store for settings state
│   ├── websocket.ts        # WebSocket connection management
│   └── theme.ts            # Theme/UI preferences
├── components/
│   ├── ModuleList.vue      # Category-organized module list
│   ├── ModuleCard.vue      # Individual module card
│   ├── SettingsPanel.vue   # Settings display panel
│   ├── settings/
│   │   ├── BoolSetting.vue      # Bool toggle
│   │   ├── IntSetting.vue       # Int slider/input
│   │   ├── DoubleSetting.vue    # Double slider/input
│   │   ├── StringSetting.vue    # Text input
│   │   ├── EnumSetting.vue      # Dropdown
│   │   ├── ColorSetting.vue     # Color picker
│   │   ├── BlockListSetting.vue # Block multi-select
│   │   └── ...                  # Other setting types
│   ├── ThemeConfigurator.vue    # Theme customization UI
│   └── ConnectionStatus.vue     # WebSocket status indicator
└── utils/
    ├── websocket.ts        # WebSocket client wrapper
    └── settingRenderer.ts  # Dynamic setting component selector
```

### State Management (Pinia)

**Modules Store**:
```typescript
interface ModulesState {
    byCategory: Record<string, ModuleInfo[]>;
    activeModules: Set<string>;
    loading: boolean;
}

const useModulesStore = defineStore('modules', {
    state: (): ModulesState => ({
        byCategory: {},
        activeModules: new Set(),
        loading: true
    }),
    actions: {
        setInitialState(data: any),
        updateModuleState(moduleName: string, active: boolean),
        toggleModule(moduleName: string)
    }
});
```

**WebSocket Store**:
```typescript
const useWebSocketStore = defineStore('websocket', {
    state: () => ({
        connected: false,
        ws: null as WebSocket | null,
        reconnecting: false
    }),
    actions: {
        connect(),
        disconnect(),
        send(message: WSMessage),
        onMessage(handler: (msg: WSMessage) => void)
    }
});
```

**Theme Store**:
```typescript
interface ThemeState {
    primaryColor: string;
    secondaryColor: string;
    backgroundColor: string;
    textColor: string;
    fontFamily: string;
    fontSize: number;
    // ... many more customization options
}
```

### Dynamic Setting Rendering
```vue
<template>
    <component
        :is="getSettingComponent(setting.type)"
        :setting="setting"
        @update="handleSettingUpdate"
    />
</template>

<script setup lang="ts">
function getSettingComponent(type: SettingType) {
    switch (type) {
        case 'BOOL': return BoolSetting;
        case 'INT': return IntSetting;
        case 'DOUBLE': return DoubleSetting;
        // ... etc
    }
}
</script>
```

## Implementation Phases

### Phase 1: Core Infrastructure ✓
- [x] Copy base addon structure
- [x] Research Meteor settings system
- [ ] Implement WebSocket server
- [ ] Implement Module Mapper
- [ ] Implement Settings Reflector
- [ ] Define communication protocol

### Phase 2: Basic Functionality
- [ ] Module listing (all categories)
- [ ] Module toggle (enable/disable)
- [ ] Basic setting types (Bool, Int, Double, String, Enum)
- [ ] Real-time module state sync
- [ ] Frontend: Module list UI
- [ ] Frontend: Basic settings UI

### Phase 3: Advanced Settings
- [ ] Color settings
- [ ] List settings (blocks, items, entities)
- [ ] Complex settings (BlockPos, Vector3d)
- [ ] Map settings
- [ ] Frontend: Advanced setting components

### Phase 4: Polish & Features
- [ ] Theme system
- [ ] Search/filter modules
- [ ] Favorites system
- [ ] Settings import/export
- [ ] Error handling & validation
- [ ] Connection status indicators
- [ ] Auto-reconnect logic

### Phase 5: Future Enhancements
- [ ] LAN accessibility (optional)
- [ ] Authentication (for LAN mode)
- [ ] Module profiles/presets
- [ ] Setting change history
- [ ] Mobile-responsive design

## Security Considerations

### Current (Localhost Only)
- No authentication needed (single-user, localhost)
- WebSocket bound to 127.0.0.1
- No CORS issues (same origin)

### Future (LAN Mode)
- API key or password authentication
- Rate limiting on commands
- Input validation and sanitization
- HTTPS/WSS for encrypted communication
- IP allowlist

## File Structure

### Java Addon
```
src/main/java/com/cope/meteorwebgui/
├── MeteorWebGUIAddon.java          # Main entry point
├── server/
│   ├── MeteorWebServer.java        # WebSocket server
│   ├── MessageHandler.java         # Message routing
│   └── StaticFileServer.java       # Serve WebUI files
├── mapping/
│   ├── ModuleMapper.java           # Module discovery
│   ├── SettingsReflector.java      # Setting reflection
│   └── models/
│       ├── ModuleInfo.java
│       ├── SettingMetadata.java
│       └── SettingType.java
├── events/
│   ├── EventMonitor.java           # Event listener
│   └── SettingChangeWrapper.java   # Wrap setting callbacks
└── protocol/
    ├── WSMessage.java              # Message model
    └── MessageType.java            # Message type enum
```

### WebUI Frontend
```
webui/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── index.html
└── src/
    ├── main.ts
    ├── App.vue
    ├── stores/       # Pinia stores
    ├── components/   # Vue components
    ├── utils/        # Utility functions
    └── assets/       # CSS, images, etc.
```

## Development Workflow

1. **Java Development**: Standard Gradle workflow
   - `./gradlew build` - Build addon
   - `./gradlew runClient` - Test in Minecraft

2. **WebUI Development**: Vite dev server
   - `cd webui && npm run dev` - Hot-reload dev server
   - Connects to WebSocket at `localhost:8080`

3. **Production Build**:
   - Build WebUI: `npm run build`
   - Copy `dist/` to `src/main/resources/webui/`
   - WebSocket server serves these static files

## Performance Considerations

- **Initial State Transfer**: Only send once on connection
- **Delta Updates**: Only broadcast changed values
- **Throttling**: Rate-limit setting updates (debounce on frontend)
- **Selective Sync**: Option to sync only visible modules/settings
- **Lazy Loading**: Load settings only when module is expanded
