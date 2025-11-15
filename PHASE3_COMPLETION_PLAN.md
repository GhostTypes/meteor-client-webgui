# Phase 3: Advanced Settings - Completion & Bug Fixes

## Current Status
- ✅ Backend: All list/map serialization implemented
- ✅ Frontend: All 11 Vue components created
- ✅ Integration: SettingsPanel.vue updated
- ⚠️ **BUGS FOUND IN TESTING**

## Critical Bugs to Fix

### 1. Null Pointer Exceptions (PARTIALLY FIXED)
**Status:** User has added null checks in `serializeValue()` and `addEmptyValueContainer()`

**Remaining Work:**
- ✅ Null check wrapper already added
- ✅ Empty value container helper already added
- ⚠️ Need to verify all list types are covered in `addEmptyValueContainer()`

**Files:**
- `src/main/java/com/cope/meteorwebgui/mapping/SettingsReflector.java` (lines 295-304)

### 2. WebSocket Performance - CRITICAL LAG ISSUE

**Problem:**
- Initial state message is MASSIVE (~500KB-1MB JSON)
- Contains 1000+ blocks, 1000+ items, 500+ entities
- Causes 5-second timeout and connection drops
- WebSocket times out: "Read timed out" after 5 seconds

**Evidence from logs:**
```
[22:55:36] Sent initial state to client (including registries)
[22:55:41] WebSocket exception: Read timed out  // 5 seconds later!
```

**Root Cause:**
- `RegistryProvider.getAllRegistries()` sends ALL registry data at once
- Browser takes too long to parse massive JSON
- WebSocket read timeout (5 seconds) kills connection

**Solutions (Pick ONE):**

#### Option A: Lazy Load Registries (RECOMMENDED)
Don't send registries in initial state. Send them on-demand when a list setting is opened.

**Changes:**
1. Remove registry data from `MeteorWebSocket.onOpen()` initial state
2. Add new message type: `registry.request`
3. Client requests specific registry when needed:
   ```typescript
   // When BlockListSetting component mounts
   if (!wsStore.registries?.blocks) {
     wsStore.send({ type: 'registry.request', data: { registry: 'blocks' } })
   }
   ```
4. Server responds with single registry (much smaller)

**Files to modify:**
- `src/main/java/com/cope/meteorwebgui/server/MeteorWebSocket.java` - Remove registry from initial state, add registry request handler
- `src/main/java/com/cope/meteorwebgui/protocol/MessageType.java` - Add REGISTRY_REQUEST, REGISTRY_DATA
- `webui/src/stores/websocket.ts` - Add registry request logic
- `webui/src/components/settings/BlockListSetting.vue` (and others) - Request registry on mount

#### Option B: Increase WebSocket Timeout
Quick fix but doesn't solve the lag problem.

**Changes:**
- Find where NanoHTTPD WebSocket timeout is set
- Increase from 5000ms to 30000ms
- Still laggy but won't disconnect

#### Option C: Paginated Registries
Send registries in chunks.

**Too complex, not recommended.**

### 3. Registry Data Structure Mismatch

**Current Code Expects:**
```typescript
wsStore.registries?.blocks?.blocks  // Array of block objects
```

**RegistryProvider Actually Sends:**
```json
{
  "blocks": {
    "blocks": [...],      // Redundant nesting
    "byNamespace": {...}
  }
}
```

**Fix:** Flatten structure in RegistryProvider OR update Vue components to use correct path.

## Recommended Implementation Order

### IMMEDIATE (Fix crash):
1. ✅ Null checks (DONE by user)
2. 🔴 **Performance fix - Remove registries from initial state**
3. 🔴 **Add on-demand registry loading**

### NEXT (Full solution):
4. Add REGISTRY_REQUEST/REGISTRY_DATA message types
5. Update Vue components to request registries on mount
6. Add loading state to list components ("Loading blocks...")

## Code Changes Needed

### 1. MeteorWebSocket.java - Remove Registry from Initial State

**File:** `src/main/java/com/cope/meteorwebgui/server/MeteorWebSocket.java`

**Change:**
```java
// REMOVE these lines (41-44):
// Add registry data for autocomplete/list settings
LOG.debug("Loading registry data for client...");
initialData.add("registries", RegistryProvider.getAllRegistries());
LOG.debug("Registry data loaded");
```

**Result:** Initial state is small (~50KB), connects instantly.

### 2. Add Registry Request Handler

**File:** `src/main/java/com/cope/meteorwebgui/server/MeteorWebSocket.java`

**Add to switch statement:**
```java
case REGISTRY_REQUEST -> handleRegistryRequest(wsMessage);
```

**Add method:**
```java
private void handleRegistryRequest(WSMessage message) {
    try {
        JsonObject data = message.getData().getAsJsonObject();
        String registryType = data.get("registry").getAsString();

        JsonObject response = new JsonObject();
        switch (registryType) {
            case "blocks" -> response.add("blocks", RegistryProvider.getAllBlocks());
            case "items" -> response.add("items", RegistryProvider.getAllItems());
            case "entities" -> response.add("entities", RegistryProvider.getAllEntityTypes());
            case "statusEffects" -> response.add("statusEffects", RegistryProvider.getAllStatusEffects());
            case "modules" -> response.add("modules", RegistryProvider.getAllModules());
        }

        response.addProperty("registryType", registryType);
        send(GSON.toJson(new WSMessage(MessageType.REGISTRY_DATA, response, message.getId())));

        LOG.info("Sent {} registry to client", registryType);
    } catch (Exception e) {
        LOG.error("Failed to send registry: {}", e.getMessage(), e);
        sendError("Failed to load registry: " + e.getMessage(), message.getId());
    }
}
```

### 3. Add Message Types

**File:** `src/main/java/com/cope/meteorwebgui/protocol/MessageType.java`

**Add:**
```java
REGISTRY_REQUEST("registry.request"),
REGISTRY_DATA("registry.data"),
```

### 4. Update WebSocket Store

**File:** `webui/src/stores/websocket.ts`

**Add to handleMessage():**
```typescript
case 'registry.data':
  const regType = message.data.registryType
  if (!registries.value) registries.value = {} as RegistryData

  if (regType === 'blocks') registries.value.blocks = message.data.blocks
  else if (regType === 'items') registries.value.items = message.data.items
  // ... etc
  break
```

**Add helper:**
```typescript
function requestRegistry(type: string) {
  send({
    type: 'registry.request',
    data: { registry: type }
  })
}

return {
  // ... existing
  requestRegistry
}
```

### 5. Update List Components to Request Registry

**Example: BlockListSetting.vue**

**Add to script:**
```typescript
import { onMounted } from 'vue'

onMounted(() => {
  if (!wsStore.registries?.blocks) {
    wsStore.requestRegistry('blocks')
  }
})
```

**Add loading state to template:**
```vue
<div v-if="!wsStore.registries?.blocks" class="loading">
  Loading blocks...
</div>
<div v-else>
  <!-- existing content -->
</div>
```

## Testing Checklist

After fixes:
- [ ] WebSocket connects without timeout
- [ ] Initial state loads instantly (<1 second)
- [ ] No NPE errors in logs
- [ ] Block list setting loads blocks on open
- [ ] Item list setting loads items on open
- [ ] Entity list setting loads entities on open
- [ ] Settings can be changed and sync works
- [ ] No lag when opening/closing modules

## Performance Benchmarks

**Before (current):**
- Initial state: ~1MB JSON
- Connection time: 5+ seconds (timeout)
- Browser freeze: 2-3 seconds

**After (with lazy loading):**
- Initial state: ~50KB JSON
- Connection time: <1 second
- Block registry load: ~200KB, <500ms
- Item registry load: ~150KB, <300ms
- Total data same, but spread over time

## Files to Modify Summary

1. `src/main/java/com/cope/meteorwebgui/server/MeteorWebSocket.java` - Remove registry from initial, add request handler
2. `src/main/java/com/cope/meteorwebgui/protocol/MessageType.java` - Add REGISTRY_REQUEST, REGISTRY_DATA
3. `webui/src/stores/websocket.ts` - Add registry request handling
4. `webui/src/components/settings/BlockListSetting.vue` - Add onMounted registry request
5. `webui/src/components/settings/ItemListSetting.vue` - Add onMounted registry request
6. `webui/src/components/settings/EntityTypeListSetting.vue` - Add onMounted registry request
7. `webui/src/components/settings/ModuleListSetting.vue` - Add onMounted registry request
8. `webui/src/components/settings/StatusEffectAmplifierMapSetting.vue` - Add onMounted registry request

## Estimated Time: 30-45 minutes

## Next Steps

1. Start new chat with this plan
2. Implement lazy loading (removes lag completely)
3. Test in Minecraft
4. Verify all settings work
5. Commit and push

## Alternative Quick Fix (5 minutes)

If you want to test NOW without lazy loading:

**Just comment out registry loading:**

In `MeteorWebSocket.java` line 41-44:
```java
// TEMPORARY: Skip registries to fix timeout
// initialData.add("registries", RegistryProvider.getAllRegistries());
```

This will:
- ✅ Fix timeout/crash
- ✅ Let you test basic settings (Bool, Int, Double, String, Enum)
- ❌ List settings won't have data (but won't crash)
- ⚠️ Need proper lazy loading later

Then re-enable after implementing lazy loading.
