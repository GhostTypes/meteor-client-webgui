package com.cope.meteorwebgui.mapping;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

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
        JsonObject valueObj = new JsonObject();

        try {
            Object value = setting.get();

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
                    // Handle both Vec3d and Vector3d types
                    try {
                        if (value instanceof Vec3d vec) {
                            valueObj.addProperty("x", vec.x);
                            valueObj.addProperty("y", vec.y);
                            valueObj.addProperty("z", vec.z);
                        } else if (value != null) {
                            // Try reflection for other vector types (like org.joml.Vector3d)
                            try {
                                double x = (double) value.getClass().getField("x").get(value);
                                double y = (double) value.getClass().getField("y").get(value);
                                double z = (double) value.getClass().getField("z").get(value);
                                valueObj.addProperty("x", x);
                                valueObj.addProperty("y", y);
                                valueObj.addProperty("z", z);
                            } catch (Exception e) {
                                valueObj.addProperty("error", "Unsupported vector type: " + value.getClass().getName());
                            }
                        }
                    } catch (Exception e) {
                        valueObj.addProperty("error", e.getMessage());
                    }
                }
                case KEYBIND -> valueObj.addProperty("value", value.toString());
                case BLOCK, ITEM, POTION -> valueObj.addProperty("value", value.toString());
                default -> valueObj.addProperty("value", value != null ? value.toString() : "null");
            }
        } catch (Exception e) {
            LOG.error("Failed to get value for setting {}: {}", setting.name, e.getMessage());
            valueObj.addProperty("error", e.getMessage());
        }

        return valueObj;
    }

    /**
     * Get default value as JSON
     */
    private static JsonObject getDefaultValue(Setting<?> setting, SettingType type) {
        JsonObject valueObj = new JsonObject();

        try {
            Object value = setting.getDefaultValue();

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
                default -> valueObj.addProperty("value", value != null ? value.toString() : "null");
            }
        } catch (Exception e) {
            valueObj.addProperty("error", e.getMessage());
        }

        return valueObj;
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
