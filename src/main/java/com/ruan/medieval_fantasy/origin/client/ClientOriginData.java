package com.ruan.medieval_fantasy.origin.client;

import com.ruan.medieval_fantasy.origin.OriginType;

public final class ClientOriginData {

    private static OriginType origin = OriginType.NONE;
    private static boolean locked;

    private ClientOriginData() {
    }

    public static void set(OriginType originType, boolean originLocked) {
        origin = originType == null ? OriginType.NONE : originType;
        locked = originLocked;
    }

    public static OriginType getOrigin() {
        return origin;
    }

    public static boolean isLocked() {
        return locked;
    }
}
