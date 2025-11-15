package com.cope.meteorwebgui.mapping;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.network.packet.Packet;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reflects on Meteor Client settings to extract metadata and manipulate values
 */
public class SettingsReflector {
    private static final Logger LOG = LoggerFactory.getLogger("WebGUI Settings Reflector");

    /**
     * Detect the type of a setting
     */
    public static SettingType detectSettingType(Setting<?> setting) {
        String className = setting.getClass().getSimpleName();

        return switch (className) {
            case "BoolSetting" -> SettingType.BOOL;
            case "IntSetting" -> SettingType.INT;
            case "DoubleSetting" -> SettingType.DOUBLE;
            case "StringSetting" -> SettingType.STRING;
            case "EnumSetting" -> SettingType.ENUM;
            case "ColorSetting" -> SettingType.COLOR;
            case "KeybindSetting" -> SettingType.KEYBIND;
            case "BlockSetting" -> SettingType.BLOCK;
            case "ItemSetting" -> SettingType.ITEM;
            case "PotionSetting" -> SettingType.POTION;
            case "BlockListSetting" -> SettingType.BLOCK_LIST;
            case "ItemListSetting" -> SettingType.ITEM_LIST;
            case "EntityTypeListSetting" -> SettingType.ENTITY_TYPE_LIST;
            case "ModuleListSetting" -> SettingType.MODULE_LIST;
            case "EnchantmentListSetting" -> SettingType.ENCHANTMENT_LIST;
            case "ParticleTypeListSetting" -> SettingType.PARTICLE_TYPE_LIST;
            case "SoundEventListSetting" -> SettingType.SOUND_EVENT_LIST;
            case "StatusEffectListSetting" -> SettingType.STATUS_EFFECT_LIST;
            case "StorageBlockListSetting" -> SettingType.STORAGE_BLOCK_LIST;
            case "StringListSetting" -> SettingType.STRING_LIST;
            case "ColorListSetting" -> SettingType.COLOR_LIST;
            case "PacketListSetting" -> SettingType.PACKET_LIST;
            case "BlockPosSetting" -> SettingType.BLOCK_POS;
            case "Vector3dSetting" -> SettingType.VECTOR3D;
            case "FontFaceSetting" -> SettingType.FONT_FACE;
            case "ProvidedStringSetting" -> SettingType.PROVIDED_STRING;
            case "StatusEffectAmplifierMapSetting" -> SettingType.STATUS_EFFECT_AMPLIFIER_MAP;
            case "BlockDataSetting" -> SettingType.BLOCK_DATA;
            case "ScreenHandlerListSetting" -> SettingType.SCREEN_HANDLER_LIST;
            case "GenericSetting" -> SettingType.GENERIC;
            default -> SettingType.UNKNOWN;
        };
    }

    /**
     * Get setting metadata as JSON
     */
    public static JsonObject getSettingMetadata(Setting<?> setting) {
        JsonObject metadata = new JsonObject();
        SettingType type = detectSettingType(setting);

        metadata.addProperty("name", setting.name);
        metadata.addProperty("title", setting.title);
        metadata.addProperty("description", setting.description);
        metadata.addProperty("type", type.name());
        metadata.add("value", getSettingValue(setting, type));
        metadata.add("defaultValue", getDefaultValue(setting, type));
        metadata.addProperty("visible", setting.isVisible());

        // Add type-specific metadata
        JsonObject typeMetadata = getTypeSpecificMetadata(setting, type);
        if (typeMetadata.size() > 0) {
            metadata.add("typeMetadata", typeMetadata);
        }

        return metadata;
    }

    /**
     * Get current setting value as JSON
     */
    public static JsonObject getSettingValue(Setting<?> setting, SettingType type) {
        Object value = null;
        try {
            value = setting.get();
        } catch (Exception e) {
            LOG.error("Failed to read current value for setting {}: {}", setting.name, e.getMessage());
        }

        if (value == null) {
            try {
                value = setting.getDefaultValue();
            } catch (Exception e) {
                LOG.warn("Failed to read default value for setting {}: {}", setting.name, e.getMessage());
            }
        }

        return serializeValue(setting, type, value);
    }

    /**
     * Get default setting value as JSON
     */
    private static JsonObject getDefaultValue(Setting<?> setting, SettingType type) {
        Object defaultValue = null;
        try {
            defaultValue = setting.getDefaultValue();
        } catch (Exception e) {
            LOG.error("Failed to read default value for setting {}: {}", setting.name, e.getMessage());
        }

        return serializeValue(setting, type, defaultValue);
    }

    private static JsonObject serializeValue(Setting<?> setting, SettingType type, Object rawValue) {
        JsonObject valueObj = new JsonObject();
        Object value = rawValue;

        if (value == null) {
            addEmptyValueContainer(valueObj, type);
            return valueObj;
        }

        try {
            switch (type) {
                case BOOL -> valueObj.addProperty("value", (Boolean) value);
                case INT -> valueObj.addProperty("value", (Integer) value);
                case DOUBLE -> valueObj.addProperty("value", (Double) value);
                case STRING, PROVIDED_STRING -> valueObj.addProperty("value", (String) value);
                case ENUM -> valueObj.addProperty("value", value.toString());
                case COLOR -> {
                    SettingColor color = (SettingColor) value;
                    valueObj.addProperty("r", color.r);
                    valueObj.addProperty("g", color.g);
                    valueObj.addProperty("b", color.b);
                    valueObj.addProperty("a", color.a);
                }
                case BLOCK_POS -> {
                    BlockPos pos = (BlockPos) value;
                    valueObj.addProperty("x", pos.getX());
                    valueObj.addProperty("y", pos.getY());
                    valueObj.addProperty("z", pos.getZ());
                }
                case VECTOR3D -> {
                    try {
                        if (value instanceof Vec3d vec) {
                            valueObj.addProperty("x", vec.x);
                            valueObj.addProperty("y", vec.y);
                            valueObj.addProperty("z", vec.z);
                        } else {
                            double x = (double) value.getClass().getField("x").get(value);
                            double y = (double) value.getClass().getField("y").get(value);
                            double z = (double) value.getClass().getField("z").get(value);
                            valueObj.addProperty("x", x);
                            valueObj.addProperty("y", y);
                            valueObj.addProperty("z", z);
                        }
                    } catch (Exception e) {
                        valueObj.addProperty("error", "Unsupported vector type: " + value.getClass().getName());
                    }
                }
                case KEYBIND -> valueObj.addProperty("value", value.toString());
                case BLOCK, ITEM, POTION -> valueObj.addProperty("value", value.toString());
                case BLOCK_LIST -> {
                    @SuppressWarnings("unchecked")
                    List<Block> blocks = (List<Block>) value;
                    JsonArray array = new JsonArray();
                    for (Block block : blocks) {
                        array.add(Registries.BLOCK.getId(block).toString());
                    }
                    valueObj.add("items", array);
                }
                case ITEM_LIST -> {
                    @SuppressWarnings("unchecked")
                    List<Item> items = (List<Item>) value;
                    JsonArray array = new JsonArray();
                    for (Item item : items) {
                        array.add(Registries.ITEM.getId(item).toString());
                    }
                    valueObj.add("items", array);
                }
                case ENTITY_TYPE_LIST -> {
                    @SuppressWarnings("unchecked")
                    Set<EntityType<?>> entities = (Set<EntityType<?>>) value;
                    JsonArray array = new JsonArray();
                    for (EntityType<?> entity : entities) {
                        array.add(Registries.ENTITY_TYPE.getId(entity).toString());
                    }
                    valueObj.add("items", array);
                }
                case MODULE_LIST -> {
                    @SuppressWarnings("unchecked")
                    List<Module> modules = (List<Module>) value;
                    JsonArray array = new JsonArray();
                    for (Module module : modules) {
                        array.add(module.name);
                    }
                    valueObj.add("items", array);
                }
                case STRING_LIST -> {
                    @SuppressWarnings("unchecked")
                    List<String> strings = (List<String>) value;
                    JsonArray array = new JsonArray();
                    strings.forEach(array::add);
                    valueObj.add("items", array);
                }
                case COLOR_LIST -> {
                    @SuppressWarnings("unchecked")
                    List<SettingColor> colors = (List<SettingColor>) value;
                    JsonArray array = new JsonArray();
                    for (SettingColor color : colors) {
                        JsonObject colorObj = new JsonObject();
                        colorObj.addProperty("r", color.r);
                        colorObj.addProperty("g", color.g);
                        colorObj.addProperty("b", color.b);
                        colorObj.addProperty("a", color.a);
                        array.add(colorObj);
                    }
                    valueObj.add("items", array);
                }
                case ENCHANTMENT_LIST -> {
                    JsonArray array = new JsonArray();
                    valueObj.add("items", array);
                }
                case STATUS_EFFECT_LIST -> {
                    @SuppressWarnings("unchecked")
                    List<StatusEffect> effects = (List<StatusEffect>) value;
                    JsonArray array = new JsonArray();
                    for (StatusEffect effect : effects) {
                        array.add(Registries.STATUS_EFFECT.getId(effect).toString());
                    }
                    valueObj.add("items", array);
                }
                case PARTICLE_TYPE_LIST -> {
                    @SuppressWarnings("unchecked")
                    List<ParticleType<?>> particles = (List<ParticleType<?>>) value;
                    JsonArray array = new JsonArray();
                    for (ParticleType<?> particle : particles) {
                        array.add(Registries.PARTICLE_TYPE.getId(particle).toString());
                    }
                    valueObj.add("items", array);
                }
                case SOUND_EVENT_LIST -> {
                    @SuppressWarnings("unchecked")
                    List<SoundEvent> sounds = (List<SoundEvent>) value;
                    JsonArray array = new JsonArray();
                    for (SoundEvent sound : sounds) {
                        Identifier id = Registries.SOUND_EVENT.getId(sound);
                        if (id != null) array.add(id.toString());
                    }
                    valueObj.add("items", array);
                }
                case STORAGE_BLOCK_LIST -> {
                    @SuppressWarnings("unchecked")
                    List<BlockEntityType<?>> blockEntityTypes = (List<BlockEntityType<?>>) value;
                    JsonArray array = new JsonArray();
                    for (BlockEntityType<?> blockEntityType : blockEntityTypes) {
                        Identifier id = Registries.BLOCK_ENTITY_TYPE.getId(blockEntityType);
                        if (id != null) array.add(id.toString());
                    }
                    valueObj.add("items", array);
                }
                case SCREEN_HANDLER_LIST -> {
                    @SuppressWarnings("unchecked")
                    List<ScreenHandlerType<?>> handlers = (List<ScreenHandlerType<?>>) value;
                    JsonArray array = new JsonArray();
                    for (ScreenHandlerType<?> handler : handlers) {
                        array.add(Registries.SCREEN_HANDLER.getId(handler).toString());
                    }
                    valueObj.add("items", array);
                }
                case STATUS_EFFECT_AMPLIFIER_MAP -> {
                    @SuppressWarnings("unchecked")
                    Map<StatusEffect, Integer> map = (Map<StatusEffect, Integer>) value;
                    JsonArray entries = new JsonArray();
                    for (Map.Entry<StatusEffect, Integer> entry : map.entrySet()) {
                        JsonObject entryObj = new JsonObject();
                        entryObj.addProperty("effect", Registries.STATUS_EFFECT.getId(entry.getKey()).toString());
                        entryObj.addProperty("amplifier", entry.getValue());
                        entries.add(entryObj);
                    }
                    valueObj.add("entries", entries);
                }
                case PACKET_LIST -> {
                    @SuppressWarnings("unchecked")
                    Set<Class<? extends Packet<?>>> packets = (Set<Class<? extends Packet<?>>>) value;
                    JsonArray array = new JsonArray();
                    for (Class<? extends Packet<?>> packet : packets) {
                        array.add(packet.getSimpleName());
                    }
                    valueObj.add("items", array);
                }
                default -> valueObj.addProperty("value", value.toString());
            }
        } catch (Exception e) {
            LOG.error("Failed to get value for setting {}: {}", setting.name, e.getMessage());
            valueObj.addProperty("error", e.getMessage());
        }

        return valueObj;
    }

    private static void addEmptyValueContainer(JsonObject valueObj, SettingType type) {
        switch (type) {
            case BLOCK_LIST, ITEM_LIST, ENTITY_TYPE_LIST, MODULE_LIST,
                    ENCHANTMENT_LIST, PARTICLE_TYPE_LIST, SOUND_EVENT_LIST,
                    STATUS_EFFECT_LIST, STORAGE_BLOCK_LIST, STRING_LIST,
                    COLOR_LIST, PACKET_LIST, SCREEN_HANDLER_LIST -> valueObj.add("items", new JsonArray());
            case STATUS_EFFECT_AMPLIFIER_MAP -> valueObj.add("entries", new JsonArray());
            default -> valueObj.add("value", JsonNull.INSTANCE);
        }
    }
    /**
     * Get type-specific metadata (min/max for numbers, enum values, etc.)
     */
    private static JsonObject getTypeSpecificMetadata(Setting<?> setting, SettingType type) {
        JsonObject meta = new JsonObject();

        try {
            switch (type) {
                case INT -> {
                    IntSetting intSetting = (IntSetting) setting;
                    // Handle Integer.MAX_VALUE/MIN_VALUE which can't be serialized to JSON
                    meta.addProperty("min", intSetting.min == Integer.MIN_VALUE ? -999999999 : intSetting.min);
                    meta.addProperty("max", intSetting.max == Integer.MAX_VALUE ? 999999999 : intSetting.max);
                    meta.addProperty("sliderMin", intSetting.sliderMin);
                    meta.addProperty("sliderMax", intSetting.sliderMax);
                    meta.addProperty("noSlider", intSetting.noSlider);
                }
                case DOUBLE -> {
                    DoubleSetting doubleSetting = (DoubleSetting) setting;
                    // Handle Double.MAX_VALUE/MIN_VALUE which can't be serialized to JSON
                    double min = Double.isInfinite(doubleSetting.min) ? -999999999.0 : doubleSetting.min;
                    double max = Double.isInfinite(doubleSetting.max) ? 999999999.0 : doubleSetting.max;
                    meta.addProperty("min", min);
                    meta.addProperty("max", max);
                    meta.addProperty("sliderMin", doubleSetting.sliderMin);
                    meta.addProperty("sliderMax", doubleSetting.sliderMax);
                    meta.addProperty("noSlider", doubleSetting.noSlider);
                    meta.addProperty("decimalPlaces", doubleSetting.decimalPlaces);
                }
                case ENUM -> {
                    List<String> suggestions = setting.getSuggestions();
                    JsonArray values = new JsonArray();
                    suggestions.forEach(values::add);
                    meta.add("values", values);
                }
                case PROVIDED_STRING -> {
                    List<String> suggestions = setting.getSuggestions();
                    JsonArray values = new JsonArray();
                    suggestions.forEach(values::add);
                    meta.add("suggestions", values);
                }
                case COLOR -> {
                    meta.addProperty("format", "rgba");
                    meta.addProperty("minValue", 0);
                    meta.addProperty("maxValue", 255);
                }
                case BLOCK_LIST -> {
                    meta.addProperty("registry", "blocks");
                    meta.addProperty("searchable", true);
                }
                case ITEM_LIST -> {
                    meta.addProperty("registry", "items");
                    meta.addProperty("searchable", true);
                }
                case ENTITY_TYPE_LIST -> {
                    meta.addProperty("registry", "entities");
                    meta.addProperty("searchable", true);
                }
                case MODULE_LIST -> {
                    meta.addProperty("registry", "modules");
                    meta.addProperty("searchable", true);
                }
                case STATUS_EFFECT_LIST -> {
                    meta.addProperty("registry", "statusEffects");
                    meta.addProperty("searchable", true);
                }
                case ENCHANTMENT_LIST -> {
                    meta.addProperty("registry", "enchantments");
                    meta.addProperty("searchable", true);
                }
                case PARTICLE_TYPE_LIST, SOUND_EVENT_LIST, SCREEN_HANDLER_LIST -> {
                    meta.addProperty("searchable", true);
                }
                case STRING_LIST -> {
                    meta.addProperty("freeInput", true);
                }
                case COLOR_LIST -> {
                    meta.addProperty("itemType", "color");
                }
                case BLOCK_POS -> {
                    meta.addProperty("minY", -64);
                    meta.addProperty("maxY", 319);
                }
                case STATUS_EFFECT_AMPLIFIER_MAP -> {
                    meta.addProperty("keyRegistry", "statusEffects");
                    meta.addProperty("valueType", "integer");
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to get type metadata for setting {}: {}", setting.name, e.getMessage());
        }

        return meta;
    }

    /**
     * Set a setting value from JSON
     */
    @SuppressWarnings("unchecked")
    public static boolean setSettingValue(Setting<?> setting, JsonObject valueData) {
        SettingType type = detectSettingType(setting);

        try {
            switch (type) {
                case BOOL -> {
                    Setting<Boolean> boolSetting = (Setting<Boolean>) setting;
                    return boolSetting.set(valueData.get("value").getAsBoolean());
                }
                case INT -> {
                    Setting<Integer> intSetting = (Setting<Integer>) setting;
                    return intSetting.set(valueData.get("value").getAsInt());
                }
                case DOUBLE -> {
                    Setting<Double> doubleSetting = (Setting<Double>) setting;
                    return doubleSetting.set(valueData.get("value").getAsDouble());
                }
                case STRING, PROVIDED_STRING -> {
                    Setting<String> stringSetting = (Setting<String>) setting;
                    return stringSetting.set(valueData.get("value").getAsString());
                }
                case ENUM -> {
                    return setting.parse(valueData.get("value").getAsString());
                }
                case COLOR -> {
                    Setting<SettingColor> colorSetting = (Setting<SettingColor>) setting;
                    SettingColor color = colorSetting.get();
                    color.r = valueData.get("r").getAsInt();
                    color.g = valueData.get("g").getAsInt();
                    color.b = valueData.get("b").getAsInt();
                    color.a = valueData.get("a").getAsInt();
                    color.validate();
                    colorSetting.onChanged();
                    return true;
                }
                case BLOCK_POS -> {
                    Setting<BlockPos> posSetting = (Setting<BlockPos>) setting;
                    int x = valueData.get("x").getAsInt();
                    int y = valueData.get("y").getAsInt();
                    int z = valueData.get("z").getAsInt();
                    return posSetting.set(new BlockPos(x, y, z));
                }
                case VECTOR3D -> {
                    Setting<Vec3d> vecSetting = (Setting<Vec3d>) setting;
                    double x = valueData.get("x").getAsDouble();
                    double y = valueData.get("y").getAsDouble();
                    double z = valueData.get("z").getAsDouble();
                    return vecSetting.set(new Vec3d(x, y, z));
                }
                case BLOCK_LIST -> {
                    Setting<List<Block>> blockList = (Setting<List<Block>>) setting;
                    JsonArray items = valueData.getAsJsonArray("items");
                    List<Block> blocks = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        try {
                            Identifier id = Identifier.of(items.get(i).getAsString());
                            Block block = Registries.BLOCK.get(id);
                            if (block != null && Registries.BLOCK.getId(block).equals(id)) {
                                blocks.add(block);
                            }
                        } catch (Exception e) {
                            LOG.warn("Invalid block ID in list: {}", items.get(i).getAsString());
                        }
                    }
                    return blockList.set(blocks);
                }
                case ITEM_LIST -> {
                    Setting<List<Item>> itemList = (Setting<List<Item>>) setting;
                    JsonArray items = valueData.getAsJsonArray("items");
                    List<Item> itemsList = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        try {
                            Identifier id = Identifier.of(items.get(i).getAsString());
                            Item item = Registries.ITEM.get(id);
                            if (item != null && Registries.ITEM.getId(item).equals(id)) {
                                itemsList.add(item);
                            }
                        } catch (Exception e) {
                            LOG.warn("Invalid item ID in list: {}", items.get(i).getAsString());
                        }
                    }
                    return itemList.set(itemsList);
                }
                case ENTITY_TYPE_LIST -> {
                    Setting<Set<EntityType<?>>> entityList = (Setting<Set<EntityType<?>>>) setting;
                    JsonArray items = valueData.getAsJsonArray("items");
                    Set<EntityType<?>> entities = new ObjectOpenHashSet<>();
                    for (int i = 0; i < items.size(); i++) {
                        try {
                            Identifier id = Identifier.of(items.get(i).getAsString());
                            EntityType<?> entity = Registries.ENTITY_TYPE.get(id);
                            if (entity != null && Registries.ENTITY_TYPE.getId(entity).equals(id)) {
                                entities.add(entity);
                            }
                        } catch (Exception e) {
                            LOG.warn("Invalid entity type ID in list: {}", items.get(i).getAsString());
                        }
                    }
                    return entityList.set(entities);
                }
                case MODULE_LIST -> {
                    Setting<List<Module>> moduleList = (Setting<List<Module>>) setting;
                    JsonArray items = valueData.getAsJsonArray("items");
                    List<Module> modules = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        try {
                            String moduleName = items.get(i).getAsString();
                            Module module = meteordevelopment.meteorclient.systems.modules.Modules.get().get(moduleName);
                            if (module != null) {
                                modules.add(module);
                            }
                        } catch (Exception e) {
                            LOG.warn("Invalid module name in list: {}", items.get(i).getAsString());
                        }
                    }
                    return moduleList.set(modules);
                }
                case STRING_LIST -> {
                    Setting<List<String>> stringList = (Setting<List<String>>) setting;
                    JsonArray items = valueData.getAsJsonArray("items");
                    List<String> strings = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        strings.add(items.get(i).getAsString());
                    }
                    return stringList.set(strings);
                }
                case COLOR_LIST -> {
                    Setting<List<SettingColor>> colorList = (Setting<List<SettingColor>>) setting;
                    JsonArray items = valueData.getAsJsonArray("items");
                    List<SettingColor> colors = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        JsonObject colorObj = items.get(i).getAsJsonObject();
                        SettingColor color = new SettingColor(
                            colorObj.get("r").getAsInt(),
                            colorObj.get("g").getAsInt(),
                            colorObj.get("b").getAsInt(),
                            colorObj.get("a").getAsInt()
                        );
                        colors.add(color);
                    }
                    return colorList.set(colors);
                }
                case ENCHANTMENT_LIST -> {
                    // In 1.21, enchantments may not be in Registries - skip if not available
                    LOG.warn("ENCHANTMENT_LIST setting type not fully supported in 1.21");
                    return false;
                }
                case STATUS_EFFECT_LIST -> {
                    Setting<List<StatusEffect>> effectList = (Setting<List<StatusEffect>>) setting;
                    JsonArray items = valueData.getAsJsonArray("items");
                    List<StatusEffect> effects = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        try {
                            Identifier id = Identifier.of(items.get(i).getAsString());
                            StatusEffect effect = Registries.STATUS_EFFECT.get(id);
                            if (effect != null) {
                                effects.add(effect);
                            }
                        } catch (Exception e) {
                            LOG.warn("Invalid status effect ID in list: {}", items.get(i).getAsString());
                        }
                    }
                    return effectList.set(effects);
                }
                case PARTICLE_TYPE_LIST -> {
                    Setting<List<ParticleType<?>>> particleList = (Setting<List<ParticleType<?>>>) setting;
                    JsonArray items = valueData.getAsJsonArray("items");
                    List<ParticleType<?>> particles = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        try {
                            Identifier id = Identifier.of(items.get(i).getAsString());
                            ParticleType<?> particle = Registries.PARTICLE_TYPE.get(id);
                            if (particle != null) {
                                particles.add(particle);
                            }
                        } catch (Exception e) {
                            LOG.warn("Invalid particle type ID in list: {}", items.get(i).getAsString());
                        }
                    }
                    return particleList.set(particles);
                }
                case SOUND_EVENT_LIST -> {
                    Setting<List<SoundEvent>> soundList = (Setting<List<SoundEvent>>) setting;
                    JsonArray items = valueData.getAsJsonArray("items");
                    List<SoundEvent> sounds = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        try {
                            Identifier id = Identifier.of(items.get(i).getAsString());
                            SoundEvent sound = Registries.SOUND_EVENT.get(id);
                            if (sound != null) {
                                sounds.add(sound);
                            }
                        } catch (Exception e) {
                            LOG.warn("Invalid sound event ID in list: {}", items.get(i).getAsString());
                        }
                    }
                    return soundList.set(sounds);
                }
                case STORAGE_BLOCK_LIST -> {
                    // StorageBlockListSetting stores BlockEntityTypes, not Blocks
                    Setting<List<BlockEntityType<?>>> blockEntityList = (Setting<List<BlockEntityType<?>>>) setting;
                    JsonArray items = valueData.getAsJsonArray("items");
                    List<BlockEntityType<?>> blockEntityTypes = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        try {
                            Identifier id = Identifier.of(items.get(i).getAsString());
                            BlockEntityType<?> blockEntityType = Registries.BLOCK_ENTITY_TYPE.get(id);
                            if (blockEntityType != null && Registries.BLOCK_ENTITY_TYPE.getId(blockEntityType).equals(id)) {
                                blockEntityTypes.add(blockEntityType);
                            }
                        } catch (Exception e) {
                            LOG.warn("Invalid storage block entity ID in list: {}", items.get(i).getAsString());
                        }
                    }
                    return blockEntityList.set(blockEntityTypes);
                }
                case SCREEN_HANDLER_LIST -> {
                    Setting<List<ScreenHandlerType<?>>> handlerList = (Setting<List<ScreenHandlerType<?>>>) setting;
                    JsonArray items = valueData.getAsJsonArray("items");
                    List<ScreenHandlerType<?>> handlers = new ArrayList<>();
                    for (int i = 0; i < items.size(); i++) {
                        try {
                            Identifier id = Identifier.of(items.get(i).getAsString());
                            ScreenHandlerType<?> handler = Registries.SCREEN_HANDLER.get(id);
                            if (handler != null) {
                                handlers.add(handler);
                            }
                        } catch (Exception e) {
                            LOG.warn("Invalid screen handler ID in list: {}", items.get(i).getAsString());
                        }
                    }
                    return handlerList.set(handlers);
                }
                case STATUS_EFFECT_AMPLIFIER_MAP -> {
                    Setting<Map<StatusEffect, Integer>> mapSetting = (Setting<Map<StatusEffect, Integer>>) setting;
                    JsonArray entries = valueData.getAsJsonArray("entries");
                    Map<StatusEffect, Integer> map = new HashMap<>();
                    for (int i = 0; i < entries.size(); i++) {
                        try {
                            JsonObject entry = entries.get(i).getAsJsonObject();
                            Identifier id = Identifier.of(entry.get("effect").getAsString());
                            StatusEffect effect = Registries.STATUS_EFFECT.get(id);
                            int amplifier = entry.get("amplifier").getAsInt();
                            if (effect != null) {
                                map.put(effect, amplifier);
                            }
                        } catch (Exception e) {
                            LOG.warn("Invalid status effect amplifier map entry: {}", e.getMessage());
                        }
                    }
                    return mapSetting.set(map);
                }
                default -> {
                    // Try parse for other types
                    if (valueData.has("value")) {
                        return setting.parse(valueData.get("value").getAsString());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to set value for setting {}: {}", setting.name, e.getMessage());
            return false;
        }

        return false;
    }
}
