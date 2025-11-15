package com.cope.meteorwebgui.protocol;

import com.google.gson.JsonElement;

public class WSMessage {
    private String type;
    private JsonElement data;
    private String id;

    public WSMessage() {
    }

    public WSMessage(String type, JsonElement data) {
        this.type = type;
        this.data = data;
    }

    public WSMessage(String type, JsonElement data, String id) {
        this.type = type;
        this.data = data;
        this.id = id;
    }

    public WSMessage(MessageType type, JsonElement data) {
        this.type = type.getValue();
        this.data = data;
    }

    public WSMessage(MessageType type, JsonElement data, String id) {
        this.type = type.getValue();
        this.data = data;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MessageType getMessageType() {
        return MessageType.fromString(type);
    }
}
