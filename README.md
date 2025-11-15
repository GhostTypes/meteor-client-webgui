# Meteor WebGUI

Web-based GUI for Meteor Client with real-time bi-directional control.

## Features

- **Real-time Sync**: WebSocket-based communication for instant updates
- **Module Control**: Toggle modules on/off from web interface
- **Settings Editor**: Edit all module settings with type-specific UI components
- **Auto-Discovery**: Automatically maps all modules from Meteor Client and installed addons
- **Configurable**: Port, host, and auto-start settings
- **Dark Theme**: Clean, modern dark UI

## Quick Start

### 1. Build the Addon

```bash
./gradlew build
```

The JAR will be in `build/libs/meteor-webgui-0.1.0.jar`

### 2. Install to Minecraft

Copy the JAR to your `.minecraft/mods` folder along with:
- Meteor Client JAR (included in `libs/`)
- Fabric Loader

### 3. Configure & Start

1. Launch Minecraft with Fabric + Meteor Client
2. Open Meteor GUI (Right Shift by default)
3. Go to the **WebGUI** tab
4. Configure port (default: 8080) and host (default: 127.0.0.1)
5. Enable "Auto Start" if you want it to start automatically
6. Click "Start Server"

### 4. Access the WebUI

#### Development Mode (with hot-reload):

```bash
cd webui
npm install
npm run dev
```

Then open `http://localhost:3000`

#### Production Mode:

Once the server is running, open your browser to:
```
http://localhost:8080
```

## Supported Setting Types

- ✅ Bool (Checkbox)
- ✅ Int (Slider + Number input)
- ✅ Double (Slider + Number input)
- ✅ String (Text input)
- ✅ Enum (Dropdown)
- 🚧 Color (Read-only, picker coming soon)
- 🚧 Keybind (Read-only)
- 🚧 List types (Read-only)
- 🚧 BlockPos/Vector3D (Read-only)

## Configuration

Access via Meteor GUI → WebGUI tab:

- **Port**: WebSocket server port (default: 8080)
- **Host**: Bind address (default: 127.0.0.1)
- **Auto Start**: Start server automatically on launch

## Development

### Java Side

```bash
# Build
./gradlew build

# Run in Minecraft (dev environment)
./gradlew runClient
```

### WebUI Side

```bash
cd webui

# Install dependencies
npm install

# Development server (hot-reload)
npm run dev

# Production build
npm run build
```

## Architecture

See [ARCHITECTURE.md](ai_docs/ARCHITECTURE.md) for detailed system design.

See [METEOR_SETTINGS_RESEARCH.md](ai_docs/METEOR_SETTINGS_RESEARCH.md) for Meteor Client settings research.

## License

MIT License - See [LICENSE](LICENSE)
