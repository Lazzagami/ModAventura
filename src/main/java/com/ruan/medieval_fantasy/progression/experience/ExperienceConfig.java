package com.ruan.medieval_fantasy.progression.experience;

public final class ExperienceConfig {

    public static int INITIAL_LEVEL = 1;
    public static int MAX_LEVEL = 100;
    public static int ATTRIBUTE_POINTS_PER_LEVEL = 1;
    public static boolean ACCUMULATE_XP_AFTER_MAX_LEVEL = false;

    public static int XP_BASE = 100;
    public static int XP_LINEAR = 35;
    public static int XP_QUADRATIC = 4;

    public static double PARTICIPATION_DISTANCE = 32.0D;
    public static int PARTICIPATION_TIMEOUT_TICKS = 20 * 15;

    public static double SPAWNER_XP_MULTIPLIER = 0.25D;
    public static double SUMMON_XP_MULTIPLIER = 0.0D;

    public static boolean REPEATED_BOSS_XP = true;
    public static double SECOND_BOSS_KILL_MULTIPLIER = 0.5D;
    public static double REPEATED_BOSS_KILL_MULTIPLIER = 0.25D;

    public static boolean SHOW_HUD = true;

    private ExperienceConfig() {
    }

    public static double groupMultiplier(int players) {
        return switch (Math.max(1, players)) {
            case 1 -> 1.0D;
            case 2 -> 1.5D;
            case 3 -> 1.9D;
            case 4 -> 2.2D;
            default -> 2.5D;
        };
    }
}
