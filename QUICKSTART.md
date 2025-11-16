# Quick Start Guide - Meteor WebGUI

## Prerequisites
- Java 21+
- Node.js 18+ (for WebUI development)
- Minecraft 1.21.10
- Fabric Loader 0.17.3+
- Meteor Client 1.21.10-32

## Step 1: Build the Addon (if not already built)

```bash
cd C:\Users\Cope\Documents\GitHub\meteor-client-webgui
./gradlew build
```

The JAR will be at: `build/libs/meteor-webgui-0.1.0.jar`

## Step 2: Install to Minecraft

1. Copy `build/libs/meteor-webgui-0.1.0.jar` to `.minecraft/mods/`
2. Copy `libs/meteor-client-1.21.10-32.jar` to `.minecraft/mods/` (if not already there)
3. Ensure Fabric Loader is installed

## Step 3: Launch Minecraft

1. Start Minecraft with Fabric profile
2. Once in-game, press **Right Shift** to open Meteor GUI
3. Navigate to the **WebGUI** tab (should be in the tabs list)

## Step 4: Start the WebGUI Server

In the WebGUI tab:
1. Leave **Port** as `8080` (or change if needed)
2. Leave **Host** as `127.0.0.1`
3. Click **Start Server**
4. You should see "Server Status: Running"

**Expected log output:**
```
[Meteor WebGUI] Starting WebGUI server on 127.0.0.1:8080
[Meteor WebGUI] Event monitoring started for X modules
[WebGUI Server] WebGUI server started on 127.0.0.1:8080
[Meteor WebGUI] Access the WebGUI at: http://127.0.0.1:8080
```

## Step 5: Launch the Web Interface

Open a new terminal/command prompt:

```bash
cd C:\Users\Cope\Documents\GitHub\meteor-client-webgui\webui
npm install
npm run dev
```

**Expected output:**
```
  VITE v6.x.x  ready in XXX ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
```

## Step 6: Access the WebUI

Open your browser to: **http://localhost:3000**

You should see:
- Header with "Meteor WebGUI" title
- Green connection status dot (if connected)
- Sidebar with categories (Combat, Movement, Player, Render, World, Misc)
- Module cards when you click a category

## Testing the WebUI

### Test Module Toggle:
1. Click a category (e.g., "Render")
2. Find a module (e.g., "Fullbright")
3. Click the toggle button (ON/OFF)
4. **Check in Minecraft** - the module should toggle
5. **Check WebUI** - button should update in real-time

### Test Settings:
1. Click "Show Settings" on a module
2. Try changing a setting value
3. **Check in Minecraft** - open Meteor GUI → find that module → verify the setting changed

### Test Real-time Sync:
1. In **Minecraft**, toggle a module via keybind or GUI
2. **Check WebUI** - the button should update automatically!
3. In **Minecraft**, change a setting value
4. **Check WebUI** - the value should update automatically!

## Troubleshooting

### "WebSocket disconnected" / Red status dot

**Check:**
1. Is the server running in Minecraft? (WebGUI tab shows "Running")
2. Is the port correct? (Default: 8080)
3. Any firewall blocking localhost:8080?

**Try:**
- Stop and restart the server in the WebGUI tab
- Check Minecraft logs for errors
- The WebUI auto-reconnects every 3 seconds

### "Cannot GET /" when accessing localhost:8080

**This is expected!** The WebSocket server on port 8080 is ONLY for WebSocket connections, not HTTP.

**You must:**
- Run `npm run dev` in the `webui` folder
- Access the UI at `localhost:3000` (Vite dev server)

### TypeScript errors in WebUI

```bash
cd webui
rm -rf node_modules package-lock.json
npm install
```

### WebUI shows "Loading modules..." forever

**Check:**
1. Is WebSocket connected? (Green dot in header)
2. Check browser console (F12) for errors
3. Check if Meteor Client loaded properly in Minecraft

## Configuration

### Change Port

In Minecraft → Meteor GUI → WebGUI tab:
1. Change "Port" to desired port (e.g., 8888)
2. Stop server (if running)
3. Start server
4. WebUI will auto-connect to new port

### Auto-start on Launch

In the WebGUI tab:
1. Enable "Auto Start" checkbox
2. Save
3. Next time you launch Minecraft, the server starts automatically

## What's Working

✅ Module listing by category (excludes HUD)
✅ Module toggle (on/off)
✅ Real-time module state sync
✅ Bool settings (checkbox)
✅ Int settings (slider + input)
✅ Double settings (slider + input)
✅ String settings (text input)
✅ Enum settings (dropdown)
✅ Real-time setting value sync
✅ Auto-reconnect on disconnect
✅ Connection status indicator

## What's Read-Only (for now)

🚧 Color settings
🚧 Keybind settings
🚧 List settings (blocks, items, etc.)
🚧 BlockPos/Vector3D settings

These show up with current values but can't be edited yet (coming in future updates).

## Next Steps

Once everything is working:
- Explore different modules and categories
- Try toggling modules from both the WebUI and Minecraft
- Test setting changes and watch them sync in real-time
- Leave feedback/issues!

## Need Help?

Check the logs:
- **Minecraft**: `.minecraft/logs/latest.log` (search for "WebGUI")
- **WebUI**: Browser console (F12 → Console tab)
