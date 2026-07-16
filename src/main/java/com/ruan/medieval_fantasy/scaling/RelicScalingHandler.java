package com.ruan.medieval_fantasy.scaling;

import com.ruan.medieval_fantasy.progression.specialization.PassiveEffectHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class RelicScalingHandler {

    private RelicScalingHandler() {
    }

    public static int getRelicControl(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            return PlayerScalingData.getAttribute(player, PlayerAttribute.RELIC_CONTROL);
        }

        return 0;
    }

    public static double getPenaltyReduction(LivingEntity entity) {
        return ScalingFormula.relicPenaltyReduction(getRelicControl(entity));
    }

    public static float getEffectivePenalty(LivingEntity entity, float basePenalty) {
        float penalty = (float) (basePenalty * (1.0D - getPenaltyReduction(entity)));
        if (entity instanceof ServerPlayer player) {
            penalty = PassiveEffectHandler.modifyRelicPenalty(player, penalty);
        }
        return penalty;
    }

    public static int getEffectiveNegativeEffectDuration(LivingEntity entity, int baseTicks) {
        return Math.max(20, (int) Math.round(baseTicks * (1.0D - getPenaltyReduction(entity))));
    }

    public static int getEffectiveThreshold(LivingEntity entity, int baseThreshold) {
        double reduction = getPenaltyReduction(entity);
        int extraDelay = (int) Math.round((100 - baseThreshold) * reduction);
        int threshold = Math.min(100, baseThreshold + extraDelay);
        if (entity instanceof ServerPlayer player) {
            threshold = PassiveEffectHandler.modifyRelicThreshold(player, threshold);
        }
        return threshold;
    }
}
