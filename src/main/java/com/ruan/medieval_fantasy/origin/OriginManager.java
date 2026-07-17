package com.ruan.medieval_fantasy.origin;

import com.ruan.medieval_fantasy.origin.network.OriginNetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class OriginManager {

    private OriginManager() {
    }

    public static boolean hasOrigin(Player player) {
        return getOrigin(player) != OriginType.NONE;
    }

    public static boolean isLocked(Player player) {
        return root(player).getBoolean(OriginSerializer.LOCKED);
    }

    public static OriginType getOrigin(Player player) {
        CompoundTag root = root(player);
        return OriginSerializer.readOrigin(root.getString(OriginSerializer.ORIGIN));
    }

    public static boolean selectOrigin(ServerPlayer player, OriginType origin) {
        if (isLocked(player) || hasOrigin(player) || !OriginRegistry.isSelectable(origin)) {
            sync(player);
            return false;
        }

        CompoundTag root = root(player);
        root.putString(OriginSerializer.ORIGIN, OriginSerializer.writeOrigin(origin));
        root.putBoolean(OriginSerializer.LOCKED, true);
        applyInitialReputation(player, origin);
        sync(player);
        return true;
    }

    public static int getReputation(Player player, String faction) {
        return root(player).getCompound(OriginSerializer.REPUTATION).getInt(faction);
    }

    public static void addReputation(Player player, String faction, int amount) {
        if (faction == null || faction.isBlank() || amount == 0) {
            return;
        }

        CompoundTag reputation = root(player).getCompound(OriginSerializer.REPUTATION);
        reputation.putInt(faction, reputation.getInt(faction) + amount);
        root(player).put(OriginSerializer.REPUTATION, reputation);
    }

    public static boolean hasOrigin(ServerPlayer player, OriginType origin) {
        return getOrigin(player) == origin;
    }

    public static void requestSelectionIfNeeded(ServerPlayer player) {
        if (!hasOrigin(player)) {
            OriginNetworkHandler.openSelection(player);
            sync(player);
        }
    }

    public static void sync(ServerPlayer player) {
        OriginNetworkHandler.sync(player, getOrigin(player), isLocked(player));
    }

    public static void copy(Player original, Player target) {
        if (original.getPersistentData().contains(OriginSerializer.ROOT)) {
            target.getPersistentData().put(OriginSerializer.ROOT,
                    original.getPersistentData().getCompound(OriginSerializer.ROOT).copy());
        }
    }

    private static void applyInitialReputation(Player player, OriginType origin) {
        OriginData data = OriginRegistry.get(origin);
        if (data == null) {
            return;
        }

        CompoundTag reputation = new CompoundTag();
        data.getInitialReputation().forEach(reputation::putInt);
        root(player).put(OriginSerializer.REPUTATION, reputation);
    }

    private static CompoundTag root(Player player) {
        return OriginSerializer.root(player.getPersistentData());
    }
}
