package com.ruan.medieval_fantasy.origin;

import net.minecraft.server.level.ServerPlayer;

public final class OriginCondition {

    private OriginCondition() {
    }

    public static boolean hasOrigin(ServerPlayer player, String originId) {
        return OriginManager.getOrigin(player) == OriginType.fromId(originId);
    }

    public static boolean reputationAtLeast(ServerPlayer player, String faction, int amount) {
        return OriginManager.getReputation(player, faction) >= amount;
    }

    public static boolean reputationBelow(ServerPlayer player, String faction, int amount) {
        return OriginManager.getReputation(player, faction) < amount;
    }
}
