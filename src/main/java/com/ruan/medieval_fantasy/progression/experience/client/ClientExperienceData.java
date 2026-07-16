package com.ruan.medieval_fantasy.progression.experience.client;

public final class ClientExperienceData {

    private static int level = 1;
    private static int currentXp;
    private static int requiredXp;
    private static long totalXp;
    private static int availablePoints;

    private ClientExperienceData() {
    }

    public static void set(int syncedLevel, int syncedCurrentXp, int syncedRequiredXp, long syncedTotalXp, int syncedAvailablePoints) {
        level = syncedLevel;
        currentXp = syncedCurrentXp;
        requiredXp = syncedRequiredXp;
        totalXp = syncedTotalXp;
        availablePoints = syncedAvailablePoints;
    }

    public static int level() {
        return level;
    }

    public static int currentXp() {
        return currentXp;
    }

    public static int requiredXp() {
        return requiredXp;
    }

    public static long totalXp() {
        return totalXp;
    }

    public static int availablePoints() {
        return availablePoints;
    }

    public static float progress() {
        if (requiredXp <= 0) {
            return 1.0F;
        }
        return Math.min(1.0F, currentXp / (float) requiredXp);
    }
}
