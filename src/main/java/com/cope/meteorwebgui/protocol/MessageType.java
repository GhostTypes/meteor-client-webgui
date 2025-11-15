package com.cope.meteorwebgui.protocol;

public enum MessageType {
    // Server -> Client
    INITIAL_STATE("initial.state"),
    MODULE_STATE_CHANGED("module.state.changed"),
    SETTING_VALUE_CHANGED("setting.value.changed"),
    REGISTRY_DATA("registry.data"),
    ERROR("error"),

    // Client -> Server
    MODULE_TOGGLE("module.toggle"),
    MODULE_LIST("module.list"),
    SETTING_UPDATE("setting.update"),
    SETTING_GET("setting.get"),
    REGISTRY_REQUEST("registry.request"),
    PING("ping"),
    PONG("pong");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MessageType fromString(String value) {
        for (MessageType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
