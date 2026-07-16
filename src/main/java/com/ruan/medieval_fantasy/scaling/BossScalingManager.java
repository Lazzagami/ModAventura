package com.ruan.medieval_fantasy.scaling;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;

public final class BossScalingManager {

    public static final String BOSS_SCALING_TAG = "medieval_fantasy.boss_scaling_applied";
    public static final String BOSS_PLAYER_COUNT_TAG = "medieval_fantasy.boss_scaling_players";
    public static final String BOSS_AVG_LEVEL_TAG = "medieval_fantasy.boss_scaling_avg_level";

    private BossScalingManager() {
    }

    public static void applyOnce(Mob boss, double baseHealth, double baseDamage) {
        CompoundTag data = boss.getPersistentData();
        if (data.getBoolean(BOSS_SCALING_TAG)) {
            return;
        }

        List<ServerPlayer> participants = PartyScalingCalculator.nearbyParticipants(boss);
        int playerCount = Math.max(1, participants.size());
        double averageLevel = PartyScalingCalculator.averageLevel(participants);

        double levelHealthBonus = Math.min(ScalingConfig.BOSS_MAX_HEALTH_LEVEL_BONUS,
                averageLevel * ScalingConfig.BOSS_HEALTH_PER_AVERAGE_LEVEL);
        double levelDamageBonus = Math.min(ScalingConfig.BOSS_MAX_DAMAGE_LEVEL_BONUS,
                averageLevel * ScalingConfig.BOSS_DAMAGE_PER_AVERAGE_LEVEL);

        double healthMultiplier = PartyScalingCalculator.playerHealthMultiplier(playerCount) * (1.0D + levelHealthBonus);
        double damageMultiplier = PartyScalingCalculator.extraPlayerDamageMultiplier(playerCount) * (1.0D + levelDamageBonus);

        if (boss.getAttribute(Attributes.MAX_HEALTH) != null) {
            boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth * healthMultiplier);
            boss.setHealth(boss.getMaxHealth());
        }

        if (boss.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            boss.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(baseDamage * damageMultiplier);
        }

        data.putBoolean(BOSS_SCALING_TAG, true);
        data.putInt(BOSS_PLAYER_COUNT_TAG, playerCount);
        data.putDouble(BOSS_AVG_LEVEL_TAG, averageLevel);
    }
}
