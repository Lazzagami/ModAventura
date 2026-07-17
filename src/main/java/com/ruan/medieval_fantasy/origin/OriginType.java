package com.ruan.medieval_fantasy.origin;

import java.util.Locale;

public enum OriginType {
    NONE("none"),
    KNIGHT("knight"),
    ARCHAEOLOGIST("archaeologist"),
    RENEGADE("renegade");

    private final String id;

    OriginType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static OriginType fromId(String id) {
        if (id == null || id.isBlank()) {
            return NONE;
        }

        String normalized = id.toLowerCase(Locale.ROOT);
        for (OriginType type : values()) {
            if (type.id.equals(normalized)) {
                return type;
            }
        }
        return NONE;
    }
}
