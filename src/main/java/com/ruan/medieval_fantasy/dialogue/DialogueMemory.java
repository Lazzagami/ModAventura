package com.ruan.medieval_fantasy.dialogue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class DialogueMemory {

    private static final String ROOT = "medieval_fantasy_dialogue_memory";

    private DialogueMemory() {
    }

    private static CompoundTag root(ServerPlayer player) {
        return root((Player) player);
    }

    private static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    public static void setBoolean(ServerPlayer player, String key, boolean value) {
        if (key != null && !key.isBlank()) {
            root(player).putBoolean(key, value);
        }
    }

    public static boolean getBoolean(ServerPlayer player, String key) {
        return key != null && root(player).getBoolean(key);
    }

    public static void setInt(ServerPlayer player, String key, int value) {
        if (key != null && !key.isBlank()) {
            root(player).putInt(key, value);
        }
    }

    public static void addInt(ServerPlayer player, String key, int amount) {
        setInt(player, key, getInt(player, key) + amount);
    }

    public static int getInt(ServerPlayer player, String key) {
        return key == null ? 0 : root(player).getInt(key);
    }

    public static void setString(ServerPlayer player, String key, String value) {
        if (key != null && !key.isBlank()) {
            root(player).putString(key, value == null ? "" : value);
        }
    }

    public static String getString(ServerPlayer player, String key) {
        return key == null ? "" : root(player).getString(key);
    }

    public static void copy(Player original, Player target) {
        if (original.getPersistentData().contains(ROOT)) {
            target.getPersistentData().put(ROOT, original.getPersistentData().getCompound(ROOT).copy());
        }
    }
}
