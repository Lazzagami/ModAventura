package com.ruan.medieval_fantasy.progression.experience;

public final class ExperienceFormula {

    private ExperienceFormula() {
    }

    public static int getRequiredXpForNextLevel(int currentLevel) {
        int level = Math.max(ExperienceConfig.INITIAL_LEVEL, currentLevel);
        return ExperienceConfig.XP_BASE
                + level * ExperienceConfig.XP_LINEAR
                + level * level * ExperienceConfig.XP_QUADRATIC;
    }

    public static long getTotalXpForLevel(int level) {
        long total = 0L;
        for (int current = ExperienceConfig.INITIAL_LEVEL; current < level; current++) {
            total += getRequiredXpForNextLevel(current);
        }
        return total;
    }
}
