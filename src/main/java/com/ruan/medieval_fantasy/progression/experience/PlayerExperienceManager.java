package com.ruan.medieval_fantasy.progression.experience;

import com.ruan.medieval_fantasy.progression.experience.network.ExperienceNetworkHandler;
import com.ruan.medieval_fantasy.scaling.PlayerScalingData;
import com.ruan.medieval_fantasy.scaling.network.ScalingNetworkHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public final class PlayerExperienceManager {

    private PlayerExperienceManager() {
    }

    public static void addXp(ServerPlayer player, int amount, ExperienceSource source) {
        if (amount <= 0) {
            return;
        }

        int level = PlayerExperienceData.getLevel(player);
        if (level >= ExperienceConfig.MAX_LEVEL && !ExperienceConfig.ACCUMULATE_XP_AFTER_MAX_LEVEL) {
            sync(player);
            return;
        }

        PlayerExperienceData.addTotalXp(player, amount);
        if (level >= ExperienceConfig.MAX_LEVEL) {
            sync(player);
            return;
        }

        int beforeLevel = level;
        int xp = PlayerExperienceData.getCurrentXp(player) + amount;
        int levelsGained = 0;

        while (level < ExperienceConfig.MAX_LEVEL) {
            int required = ExperienceFormula.getRequiredXpForNextLevel(level);
            if (xp < required) {
                break;
            }

            xp -= required;
            level++;
            levelsGained++;
        }

        if (level >= ExperienceConfig.MAX_LEVEL && !ExperienceConfig.ACCUMULATE_XP_AFTER_MAX_LEVEL) {
            xp = 0;
        }

        PlayerExperienceData.setLevel(player, level);
        PlayerExperienceData.setCurrentXp(player, xp);
        if (levelsGained > 0) {
            PlayerScalingData.addAvailablePoints(player, levelsGained * ExperienceConfig.ATTRIBUTE_POINTS_PER_LEVEL);
            showLevelUp(player, beforeLevel, level, levelsGained);
            ScalingNetworkHandler.sync(player);
        } else {
            player.displayClientMessage(Component.literal("+" + amount + " XP"), true);
        }

        sync(player);
    }

    public static void removeXp(ServerPlayer player, int amount) {
        PlayerExperienceData.setCurrentXp(player, Math.max(0, PlayerExperienceData.getCurrentXp(player) - Math.max(0, amount)));
        sync(player);
    }

    public static void setXp(ServerPlayer player, int amount) {
        PlayerExperienceData.setCurrentXp(player, Math.max(0, amount));
        sync(player);
    }

    public static void setLevel(ServerPlayer player, int level) {
        PlayerExperienceData.setLevel(player, level);
        PlayerExperienceData.setCurrentXp(player, 0);
        sync(player);
        ScalingNetworkHandler.sync(player);
    }

    public static int getLevel(ServerPlayer player) {
        return PlayerExperienceData.getLevel(player);
    }

    public static int getCurrentXp(ServerPlayer player) {
        return PlayerExperienceData.getCurrentXp(player);
    }

    public static int getRequiredXp(ServerPlayer player) {
        int level = PlayerExperienceData.getLevel(player);
        return level >= ExperienceConfig.MAX_LEVEL ? 0 : ExperienceFormula.getRequiredXpForNextLevel(level);
    }

    public static double getProgress(ServerPlayer player) {
        int required = getRequiredXp(player);
        if (required <= 0) {
            return 1.0D;
        }
        return Math.min(1.0D, PlayerExperienceData.getCurrentXp(player) / (double) required);
    }

    public static void rewardMobKill(LivingEntity entity) {
        if (!isValidRewardEntity(entity)) {
            return;
        }

        int baseXp = ExperienceRewardRegistry.getEntityXp(entity);
        String bossId = ExperienceRewardRegistry.bossIdFor(entity);
        if (!bossId.isBlank()) {
            rewardBossKill(entity, CombatParticipationTracker.getParticipants(entity), bossId);
            return;
        }

        if (baseXp <= 0) {
            return;
        }

        rewardParticipants(entity, CombatParticipationTracker.getParticipants(entity), baseXp, ExperienceSource.COMMON_MOB);
    }

    public static void rewardBossKill(LivingEntity boss, List<ServerPlayer> participants, String bossId) {
        int baseXp = ExperienceRewardRegistry.getBossXp(bossId);
        if (baseXp <= 0 || participants.isEmpty()) {
            return;
        }

        int players = participants.size();
        double groupMultiplier = ExperienceConfig.groupMultiplier(players);
        for (ServerPlayer player : participants) {
            int reward = (int) Math.round(baseXp * groupMultiplier / players * BossExperienceTracker.repeatedKillMultiplier(player, bossId));
            addXp(player, reward, ExperienceSource.BOSS);
            PlayerExperienceData.addBossKill(player, bossId);
        }
    }

    public static void rewardDiscovery(ServerPlayer player, String discoveryId, int xp) {
        if (PlayerExperienceData.hasDiscovery(player, discoveryId)) {
            return;
        }
        PlayerExperienceData.addDiscovery(player, discoveryId);
        addXp(player, xp, ExperienceSource.DISCOVERY);
    }

    public static void sync(ServerPlayer player) {
        ExperienceNetworkHandler.sync(player);
    }

    public static boolean isValidRewardEntity(LivingEntity entity) {
        if (entity.getPersistentData().getBoolean("medieval_fantasy:no_progression_xp")) {
            return false;
        }

        return !entity.getPersistentData().getBoolean("medieval_fantasy:summoned_entity")
                || ExperienceConfig.SUMMON_XP_MULTIPLIER > 0.0D;
    }

    private static void rewardParticipants(LivingEntity entity, List<ServerPlayer> participants, int baseXp, ExperienceSource source) {
        if (participants.isEmpty()) {
            return;
        }

        double multiplier = entity.getPersistentData().getBoolean("medieval_fantasy:spawner_entity")
                ? ExperienceConfig.SPAWNER_XP_MULTIPLIER
                : 1.0D;
        if (entity.getPersistentData().getBoolean("medieval_fantasy:summoned_entity")) {
            multiplier *= ExperienceConfig.SUMMON_XP_MULTIPLIER;
        }

        int players = participants.size();
        double groupMultiplier = ExperienceConfig.groupMultiplier(players);
        for (ServerPlayer player : participants) {
            int reward = (int) Math.round(baseXp * multiplier * groupMultiplier / players);
            addXp(player, reward, source);
        }
    }

    private static void showLevelUp(ServerPlayer player, int beforeLevel, int level, int levelsGained) {
        player.displayClientMessage(Component.literal("Nível " + beforeLevel + " → " + level
                + " | +" + levelsGained * ExperienceConfig.ATTRIBUTE_POINTS_PER_LEVEL + " ponto(s) de atributo"), true);
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 0.9F);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0D, player.getZ(),
                    24, 0.6D, 0.8D, 0.6D, 0.08D);
        }
    }
}
