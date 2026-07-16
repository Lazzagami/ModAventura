package com.ruan.medieval_fantasy.combat.heat;

public enum HeatStage {
    STABLE,
    WARMING,
    OVERHEATED,
    COMBUSTION,
    INTERNAL_COMBUSTION;

    public static HeatStage fromHeat(int heat) {
        if (heat >= HeatData.INTERNAL_COMBUSTION_MIN) {
            return INTERNAL_COMBUSTION;
        }

        if (heat >= HeatData.COMBUSTION_MIN) {
            return COMBUSTION;
        }

        if (heat >= HeatData.OVERHEATED_MIN) {
            return OVERHEATED;
        }

        if (heat >= HeatData.WARMING_MIN) {
            return WARMING;
        }

        return STABLE;
    }
}
