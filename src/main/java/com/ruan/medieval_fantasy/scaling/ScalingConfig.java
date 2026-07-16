package com.ruan.medieval_fantasy.scaling;

public final class ScalingConfig {

    public static double HEALTH_PER_VITALITY_PAIR = 1.0D;
    public static double STRENGTH_PHYSICAL_DAMAGE_PER_POINT = 0.005D;

    public static double DEFENSE_CURVE = 150.0D;
    public static double DEFENSE_MAX_REDUCTION = 0.80D;
    public static double MIN_FINAL_DAMAGE = 0.5D;

    public static double AGILITY_MOVEMENT_PER_POINT = 0.0025D;
    public static double AGILITY_ATTACK_SPEED_PER_POINT = 0.0015D;

    public static double RELIC_CONTROL_CURVE = 200.0D;
    public static double RELIC_CONTROL_MAX_PENALTY_REDUCTION = 0.75D;

    public static double BOSS_PARTICIPATION_RADIUS = 64.0D;
    public static double BOSS_HEALTH_PER_AVERAGE_LEVEL = 0.01D;
    public static double BOSS_DAMAGE_PER_AVERAGE_LEVEL = 0.0035D;
    public static double BOSS_MAX_HEALTH_LEVEL_BONUS = 1.0D;
    public static double BOSS_MAX_DAMAGE_LEVEL_BONUS = 0.35D;
    public static double BOSS_DAMAGE_PER_EXTRA_PLAYER = 0.08D;

    private ScalingConfig() {
    }
}
