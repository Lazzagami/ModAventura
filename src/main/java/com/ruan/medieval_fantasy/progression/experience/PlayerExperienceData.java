package com.ruan.medieval_fantasy.progression.experience;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashSet;
import java.util.Set;

public final class PlayerExperienceData {

    public static final String ROOT_TAG = "medieval_fantasy.experience";
    public static final String LEVEL_TAG = "level";
    public static final String CURRENT_XP_TAG = "current_xp";
    public static final String TOTAL_XP_TAG = "total_xp";
    public static final String BOSSES_TAG = "boss_kills";
    public static final String DISCOVERIES_TAG = "discoveries";

    private PlayerExperienceData() {
    }

    public static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_TAG)) {
            CompoundTag root = new CompoundTag();
            root.putInt(LEVEL_TAG, ExperienceConfig.INITIAL_LEVEL);
            root.putInt(CURRENT_XP_TAG, 0);
            root.putLong(TOTAL_XP_TAG, 0L);
            persistent.put(ROOT_TAG, root);
        }

        CompoundTag root = persistent.getCompound(ROOT_TAG);
        if (!root.contains(LEVEL_TAG)) {
            root.putInt(LEVEL_TAG, ExperienceConfig.INITIAL_LEVEL);
        }
        return root;
    }

    public static int getLevel(Player player) {
        return Math.max(ExperienceConfig.INITIAL_LEVEL, root(player).getInt(LEVEL_TAG));
    }

    public static void setLevel(Player player, int level) {
        root(player).putInt(LEVEL_TAG, Math.max(ExperienceConfig.INITIAL_LEVEL, Math.min(ExperienceConfig.MAX_LEVEL, level)));
    }

    public static int getCurrentXp(Player player) {
        return Math.max(0, root(player).getInt(CURRENT_XP_TAG));
    }

    public static void setCurrentXp(Player player, int xp) {
        root(player).putInt(CURRENT_XP_TAG, Math.max(0, xp));
    }

    public static long getTotalXp(Player player) {
        return Math.max(0L, root(player).getLong(TOTAL_XP_TAG));
    }

    public static void addTotalXp(Player player, int amount) {
        root(player).putLong(TOTAL_XP_TAG, getTotalXp(player) + Math.max(0, amount));
    }

    public static CompoundTag bossKills(Player player) {
        return child(root(player), BOSSES_TAG);
    }

    public static int getBossKills(Player player, String bossId) {
        return Math.max(0, bossKills(player).getInt(bossId));
    }

    public static void addBossKill(Player player, String bossId) {
        bossKills(player).putInt(bossId, getBossKills(player, bossId) + 1);
    }

    public static CompoundTag discoveries(Player player) {
        return child(root(player), DISCOVERIES_TAG);
    }

    public static boolean hasDiscovery(Player player, String discoveryId) {
        return discoveries(player).getBoolean(discoveryId);
    }

    public static void addDiscovery(Player player, String discoveryId) {
        discoveries(player).putBoolean(discoveryId, true);
    }

    public static Set<String> discoveryIds(Player player) {
        return new LinkedHashSet<>(discoveries(player).getAllKeys());
    }

    public static void copy(Player original, Player target) {
        target.getPersistentData().put(ROOT_TAG, root(original).copy());
    }

    public static void reset(Player player) {
        CompoundTag root = root(player);
        root.putInt(LEVEL_TAG, ExperienceConfig.INITIAL_LEVEL);
        root.putInt(CURRENT_XP_TAG, 0);
        root.putLong(TOTAL_XP_TAG, 0L);
        root.remove(BOSSES_TAG);
        root.remove(DISCOVERIES_TAG);
    }

    private static CompoundTag child(CompoundTag root, String key) {
        if (!root.contains(key)) {
            root.put(key, new CompoundTag());
        }
        return root.getCompound(key);
    }
}
