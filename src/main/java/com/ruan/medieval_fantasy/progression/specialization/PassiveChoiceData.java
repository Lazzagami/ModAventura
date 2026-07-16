package com.ruan.medieval_fantasy.progression.specialization;

import com.ruan.medieval_fantasy.scaling.PlayerAttribute;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashSet;
import java.util.Set;

public final class PassiveChoiceData {

    public static final String ROOT_TAG = "medieval_fantasy.specialization";
    public static final String CHOICES_TAG = "choices";
    public static final String PENDING_TAG = "pending";
    public static final String TITLES_TAG = "titles";
    public static final String EQUIPPED_TITLE_TAG = "equipped_title";
    public static final String COOLDOWNS_TAG = "cooldowns";
    public static final String RUNTIME_TAG = "runtime";

    private PassiveChoiceData() {
    }

    public static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_TAG)) {
            persistent.put(ROOT_TAG, new CompoundTag());
        }
        return persistent.getCompound(ROOT_TAG);
    }

    public static CompoundTag choices(Player player) {
        return child(root(player), CHOICES_TAG);
    }

    public static CompoundTag pending(Player player) {
        return child(root(player), PENDING_TAG);
    }

    public static CompoundTag titles(Player player) {
        return child(root(player), TITLES_TAG);
    }

    public static CompoundTag cooldowns(Player player) {
        return child(root(player), COOLDOWNS_TAG);
    }

    public static CompoundTag runtime(Player player) {
        return child(root(player), RUNTIME_TAG);
    }

    public static String key(PlayerAttribute attribute, int milestone) {
        return PassiveRegistry.key(attribute, milestone);
    }

    public static boolean hasChoice(Player player, PlayerAttribute attribute, int milestone) {
        return choices(player).contains(key(attribute, milestone));
    }

    public static String getChoice(Player player, PlayerAttribute attribute, int milestone) {
        return choices(player).getString(key(attribute, milestone));
    }

    public static void setChoice(Player player, PlayerAttribute attribute, int milestone, String passiveId) {
        choices(player).putString(key(attribute, milestone), passiveId);
        pending(player).remove(key(attribute, milestone));
    }

    public static void clearChoice(Player player, PlayerAttribute attribute, int milestone) {
        choices(player).remove(key(attribute, milestone));
    }

    public static boolean isPending(Player player, PlayerAttribute attribute, int milestone) {
        return pending(player).getBoolean(key(attribute, milestone));
    }

    public static void setPending(Player player, PlayerAttribute attribute, int milestone, boolean value) {
        String key = key(attribute, milestone);
        if (value) {
            pending(player).putBoolean(key, true);
        } else {
            pending(player).remove(key);
        }
    }

    public static void unlockTitle(Player player, String titleId) {
        if (!titleId.isBlank()) {
            titles(player).putBoolean(titleId, true);
        }
    }

    public static boolean hasTitle(Player player, String titleId) {
        return titles(player).getBoolean(titleId);
    }

    public static Set<String> unlockedTitles(Player player) {
        return new LinkedHashSet<>(titles(player).getAllKeys());
    }

    public static String equippedTitle(Player player) {
        return root(player).getString(EQUIPPED_TITLE_TAG);
    }

    public static void equipTitle(Player player, String titleId) {
        if (titleId == null || titleId.isBlank()) {
            root(player).remove(EQUIPPED_TITLE_TAG);
            return;
        }

        root(player).putString(EQUIPPED_TITLE_TAG, titleId);
    }

    public static void copy(Player original, Player target) {
        target.getPersistentData().put(ROOT_TAG, root(original).copy());
    }

    private static CompoundTag child(CompoundTag root, String key) {
        if (!root.contains(key)) {
            root.put(key, new CompoundTag());
        }
        return root.getCompound(key);
    }
}
