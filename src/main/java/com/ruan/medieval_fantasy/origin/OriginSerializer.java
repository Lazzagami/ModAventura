package com.ruan.medieval_fantasy.origin;

import net.minecraft.nbt.CompoundTag;

public final class OriginSerializer {

    public static final String ROOT = "medieval_fantasy_origin";
    public static final String ORIGIN = "player_origin";
    public static final String LOCKED = "origin_locked";
    public static final String REPUTATION = "origin_reputation";

    private OriginSerializer() {
    }

    public static CompoundTag root(CompoundTag persistent) {
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    public static String writeOrigin(OriginType origin) {
        return origin == null ? OriginType.NONE.getId() : origin.getId();
    }

    public static OriginType readOrigin(String originId) {
        return OriginType.fromId(originId);
    }
}
