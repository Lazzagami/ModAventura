package com.ruan.medieval_fantasy.progression.specialization;

public final class SpecializationConfig {

    public static final int[] MILESTONES = {15, 30, 50, 75, 100};

    public static double FEROCIOUS_RUNNER_MOVEMENT = 0.15D;
    public static double SWIFT_BLADE_ATTACK_SPEED = 0.15D;
    public static double ROBUST_HEART_HEALTH = 6.0D;
    public static double BRUTAL_STRIKE_DAMAGE = 0.15D;
    public static double IRON_WALL_PHYSICAL_REDUCTION = 0.15D;
    public static double DISCIPLINED_BEARER_THRESHOLD_DELAY = 0.15D;

    public static double PHANTOM_STEP_MOVEMENT = 0.20D;
    public static int PHANTOM_STEP_DURATION_TICKS = 60;
    public static int PHANTOM_STEP_COOLDOWN_TICKS = 200;

    public static double BATTLE_RHYTHM_ATTACK_SPEED_PER_STACK = 0.02D;
    public static int BATTLE_RHYTHM_MAX_STACKS = 5;
    public static int BATTLE_RHYTHM_TIMEOUT_TICKS = 60;

    public static double EXECUTOR_DAMAGE = 0.25D;
    public static double EXECUTOR_TARGET_HEALTH = 0.20D;
    public static double OVERHEAT_DAMAGE_REDUCTION = 0.20D;

    private SpecializationConfig() {
    }
}
