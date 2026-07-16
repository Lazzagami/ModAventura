package com.ruan.medieval_fantasy.scaling;

public final class ScalingFormula {

    private ScalingFormula() {
    }

    public static double additionalHealth(int vitality) {
        return Math.floor(vitality / 2.0D) * ScalingConfig.HEALTH_PER_VITALITY_PAIR;
    }

    public static double physicalDamageMultiplier(int strength) {
        return 1.0D + strength * ScalingConfig.STRENGTH_PHYSICAL_DAMAGE_PER_POINT;
    }

    public static double defenseReduction(int defense) {
        double reduction = defense / (defense + ScalingConfig.DEFENSE_CURVE);
        return clamp(reduction, 0.0D, ScalingConfig.DEFENSE_MAX_REDUCTION);
    }

    public static double movementMultiplier(int agility) {
        return 1.0D + agility * ScalingConfig.AGILITY_MOVEMENT_PER_POINT;
    }

    public static double attackSpeedMultiplier(int agility) {
        return 1.0D + agility * ScalingConfig.AGILITY_ATTACK_SPEED_PER_POINT;
    }

    public static double relicPenaltyReduction(int relicControl) {
        double reduction = relicControl / (relicControl + ScalingConfig.RELIC_CONTROL_CURVE);
        return clamp(reduction, 0.0D, ScalingConfig.RELIC_CONTROL_MAX_PENALTY_REDUCTION);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
