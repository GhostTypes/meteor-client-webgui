# Testing Checklist - Meteor WebGUI

## Pre-Testing Verification

### ✅ Files Exist
- [x] 10 Java source files in `src/main/java/`
- [x] 9 Vue components in `webui/src/components/`
- [x] 3 TypeScript files (stores + main.ts)
- [x] 4 config files (package.json, vite.config.ts, tsconfigs)
- [x] Built JAR at `build/libs/meteor-webgui-0.1.0.jar` (412K)

### ✅ Java Components
- [x] MeteorWebGUIAddon.java - Main entry point
- [x] MeteorWebServer.java - WebSocket server
- [x] ModuleMapper.java - Module discovery
- [x] SettingsReflector.java - Settings manipulation
- [x] EventMonitor.java - Real-time sync
- [x] WebGUIConfig.java - Configuration system
- [x] WebGUITab.java - GUI integration
- [x] MessageType.java - Protocol enums
- [x] WSMessage.java - Message model
- [x] SettingType.java - Setting types

### ✅ Vue Components
- [x] App.vue - Main application
- [x] ModuleList.vue - Category module list
- [x] ModuleCard.vue - Individual module
- [x] SettingsPanel.vue - Settings container
- [x] BoolSetting.vue - Boolean toggle
- [x] IntSetting.vue - Integer slider
- [x] DoubleSetting.vue - Decimal slider
- [x] StringSetting.vue - Text input
- [x] EnumSetting.vue - Dropdown
- [x] GenericSetting.vue - Fallback

### ✅ Stores (Pinia)
- [x] websocket.ts - WebSocket connection
- [x] modules.ts - Module state

### ✅ Configuration
- [x] package.json - NPM dependencies
- [x] vite.config.ts - Build config
- [x] tsconfig.json - TypeScript config
- [x] index.html - Entry HTML

## Installation Testing

### Step 1: Build
```bash
cd C:\Users\Cope\Documents\GitHub\meteor-client-webgui
./gradlew build
```
- [ ] Build succeeds (BUILD SUCCESSFUL)
- [ ] JAR created at `build/libs/meteor-webgui-0.1.0.jar`
- [ ] JAR size is ~400KB+

### Step 2: Install Mod
- [ ] Copy JAR to `.minecraft/mods/`
- [ ] Copy `libs/meteor-client-1.21.8-56.jar` to `.minecraft/mods/`
- [ ] Fabric Loader installed

### Step 3: Launch Minecraft
- [ ] Minecraft starts without crash
- [ ] No errors in logs related to "meteor-webgui"
- [ ] Meteor Client loads successfully

### Step 4: Open Meteor GUI
- [ ] Press Right Shift (default keybind)
- [ ] Meteor GUI opens
- [ ] **WebGUI** tab appears in tab list
- [ ] Click WebGUI tab → screen loads

### Step 5: WebGUI Tab UI
- [ ] Settings visible: Port, Host, Auto Start
- [ ] Default port = 8080
- [ ] Default host = 127.0.0.1
- [ ] Auto start = false (unchecked)
- [ ] "Server Status: Stopped" shown
- [ ] "Start Server" button visible

### Step 6: Start Server
- [ ] Click "Start Server"
- [ ] Status changes to "Running"
- [ ] Button changes to "Stop Server"
- [ ] "Copy URL" button appears
- [ ] Check logs for: `WebGUI server started on 127.0.0.1:8080`
- [ ] Check logs for: `Event monitoring started for X modules`

## WebUI Testing

### Step 1: NPM Setup
```bash
cd C:\Users\Cope\Documents\GitHub\meteor-client-webgui\webui
npm install
```
- [ ] No errors during install
- [ ] `node_modules` folder created
- [ ] All dependencies installed (vue, pinia, vite, etc.)

### Step 2: Start Dev Server
```bash
npm run dev
```
- [ ] Vite starts successfully
- [ ] Shows `Local: http://localhost:3000/`
- [ ] No TypeScript errors
- [ ] No build errors

### Step 3: Access WebUI
- [ ] Open `http://localhost:3000` in browser
- [ ] Page loads (dark theme)
- [ ] Header shows "Meteor WebGUI"
- [ ] Connection status visible (should be green dot + "Connected")

### Step 4: Initial Load
- [ ] Sidebar shows categories (Combat, Movement, Player, Render, World, Misc)
- [ ] No "Loading modules..." stuck forever
- [ ] No errors in browser console (F12 → Console)
- [ ] WebSocket shows "Connected" in header

## Functionality Testing

### Test 1: Module Listing
- [ ] Click "Combat" category
- [ ] Modules appear (e.g., Auto Crystal, Kill Aura, etc.)
- [ ] Each module shows: title, description, addon name
- [ ] Toggle button shows current state (ON/OFF)
- [ ] Click different categories → modules update

### Test 2: Module Toggle (WebUI → Minecraft)
- [ ] Click toggle on a module (e.g., Fullbright)
- [ ] Button updates immediately
- [ ] Check in Minecraft: Module toggles
- [ ] Toggle again → Module toggles off
- [ ] Try with multiple modules

### Test 3: Module Toggle (Minecraft → WebUI)
- [ ] In Minecraft, toggle a module via keybind
- [ ] Check WebUI: Button updates automatically (real-time sync!)
- [ ] In Minecraft GUI, toggle a module
- [ ] Check WebUI: Button updates automatically

### Test 4: View Settings
- [ ] Click "Show Settings" on a module
- [ ] Settings panel expands
- [ ] Settings grouped properly
- [ ] Each setting shows: title, description, current value

### Test 5: Bool Settings
- [ ] Find a module with bool settings
- [ ] Click checkbox to toggle
- [ ] Check in Minecraft: Setting changes
- [ ] In Minecraft, change the setting
- [ ] Check WebUI: Checkbox updates automatically

### Test 6: Int Settings
- [ ] Find a module with int settings (e.g., range, delay)
- [ ] Drag the slider
- [ ] Value updates in real-time
- [ ] Type in number input
- [ ] Value updates
- [ ] Check in Minecraft: Setting changed
- [ ] Change in Minecraft → WebUI updates

### Test 7: Double Settings
- [ ] Find a module with double settings
- [ ] Test slider
- [ ] Test number input
- [ ] Verify decimals display correctly
- [ ] Verify sync works

### Test 8: String Settings
- [ ] Find a module with string settings
- [ ] Type new value
- [ ] Press Tab or click away
- [ ] Check in Minecraft: Value changed
- [ ] Change in Minecraft → WebUI updates

### Test 9: Enum Settings
- [ ] Find a module with enum settings (e.g., mode selection)
- [ ] Click dropdown
- [ ] All options visible
- [ ] Select different option
- [ ] Check in Minecraft: Setting changed
- [ ] Change in Minecraft → WebUI updates

### Test 10: Connection Resilience
- [ ] Stop server in Minecraft (WebGUI tab → Stop Server)
- [ ] WebUI shows "Disconnected" / reconnecting
- [ ] Start server again
- [ ] WebUI reconnects automatically (within 3 seconds)
- [ ] State loads properly

### Test 11: Multiple Categories
- [ ] Test modules in Combat category
- [ ] Test modules in Movement category
- [ ] Test modules in Render category
- [ ] Test modules in Player category
- [ ] Verify all work correctly

### Test 12: Settings Visibility
- [ ] Find a module with conditional settings (visible only when enabled)
- [ ] Verify settings appear/disappear correctly
- [ ] Verify `visible: true/false` respected

## Performance Testing

### Test 1: Initial Load Time
- [ ] Note time from server start to WebUI connection
- [ ] Should be < 2 seconds
- [ ] All modules load quickly

### Test 2: Toggle Responsiveness
- [ ] Toggle feels instant (< 100ms)
- [ ] No lag in UI
- [ ] No lag in Minecraft

### Test 3: Setting Updates
- [ ] Slider updates feel smooth
- [ ] No input lag
- [ ] Changes sync quickly (< 200ms)

## Error Handling Testing

### Test 1: Invalid Setting Value
- [ ] Try entering invalid number (e.g., text in int field)
- [ ] App doesn't crash
- [ ] Error handled gracefully

### Test 2: Server Disconnected
- [ ] Close Minecraft while WebUI open
- [ ] WebUI shows "Disconnected"
- [ ] No crash, just auto-retry

### Test 3: Browser Refresh
- [ ] Refresh browser page (F5)
- [ ] Reconnects automatically
- [ ] State loads correctly

## Cross-Browser Testing (Optional)

- [ ] Chrome/Edge (Chromium)
- [ ] Firefox
- [ ] Safari (if on Mac)

## Final Checks

### Documentation
- [ ] README.md clear and complete
- [ ] QUICKSTART.md accurate
- [ ] ARCHITECTURE.md useful
- [ ] METEOR_SETTINGS_RESEARCH.md helpful

### Code Quality
- [ ] No console errors in WebUI
- [ ] No warnings in Minecraft logs (related to addon)
- [ ] Java code compiles without warnings
- [ ] TypeScript has no errors

### User Experience
- [ ] UI is intuitive
- [ ] Dark theme looks good
- [ ] Text is readable
- [ ] Buttons are clearly labeled
- [ ] Connection status is obvious

## Known Limitations (Expected)

✅ These are NORMAL for the prototype:
- HUD modules are excluded (by design)
- Color settings show as read-only
- Keybind settings show as read-only
- List settings (blocks, items, etc.) show as read-only
- BlockPos/Vector3D settings show as read-only
- No search/filter yet
- No theming customization UI yet

## Success Criteria

For the prototype to be considered successful:
1. ✅ Server starts without errors
2. ✅ WebUI connects to server
3. ✅ Modules list properly by category
4. ✅ Module toggle works (both directions)
5. ✅ At least Bool, Int, Double, String, Enum settings work
6. ✅ Real-time sync works for both modules and settings
7. ✅ Connection auto-reconnects
8. ✅ No crashes or critical bugs

## Reporting Issues

If something doesn't work:
1. Check browser console (F12 → Console)
2. Check Minecraft logs (`.minecraft/logs/latest.log`)
3. Note exact steps to reproduce
4. Note what was expected vs what happened
5. Include any error messages

## Next Steps After Successful Testing

Once everything works:
- [ ] Test with different Meteor modules
- [ ] Test with addon modules (if any installed)
- [ ] Try all setting types available
- [ ] Stress test with many rapid toggles
- [ ] Leave running for extended period
- [ ] Provide feedback on UX/UI
