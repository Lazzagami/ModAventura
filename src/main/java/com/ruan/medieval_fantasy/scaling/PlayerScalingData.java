package com.ruan.medieval_fantasy.scaling;

import com.ruan.medieval_fantasy.progression.experience.PlayerExperienceData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class PlayerScalingData {

    public static final String ROOT_TAG = "medieval_fantasy.scaling";
    public static final String LEVEL_TAG = "level";
    public static final String AVAILABLE_POINTS_TAG = "available_points";
    public static final String INVESTED_PREFIX = "attr.";

    private PlayerScalingData() {
    }

    public static int getAttribute(Player player, PlayerAttribute attribute) {
        return Math.max(0, scalingTag(player).getInt(INVESTED_PREFIX + attribute.id()));
    }

    public static void setAttribute(Player player, PlayerAttribute attribute, int value) {
        scalingTag(player).putInt(INVESTED_PREFIX + attribute.id(), Math.max(0, value));
    }

    public static boolean investPoint(Player player, PlayerAttribute attribute) {
        int availablePoints = getAvailablePoints(player);
        if (availablePoints <= 0) {
            return false;
        }

        setAvailablePoints(player, availablePoints - 1);
        setAttribute(player, attribute, getAttribute(player, attribute) + 1);
        return true;
    }

    public static int getLevel(Player player) {
        return PlayerExperienceData.getLevel(player);
    }

    public static int getAvailablePoints(Player player) {
        return Math.max(0, scalingTag(player).getInt(AVAILABLE_POINTS_TAG));
    }

    public static void setAvailablePoints(Player player, int points) {
        scalingTag(player).putInt(AVAILABLE_POINTS_TAG, Math.max(0, points));
    }

    public static void addAvailablePoints(Player player, int points) {
        setAvailablePoints(player, getAvailablePoints(player) + Math.max(0, points));
    }

    public static CompoundTag scalingTag(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_TAG)) {
            persistent.put(ROOT_TAG, new CompoundTag());
        }

        return persistent.getCompound(ROOT_TAG);
    }
}
