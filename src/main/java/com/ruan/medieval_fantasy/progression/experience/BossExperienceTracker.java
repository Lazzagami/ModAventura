package com.ruan.medieval_fantasy.progression.experience;

import net.minecraft.server.level.ServerPlayer;

public final class BossExperienceTracker {

    private BossExperienceTracker() {
    }

    public static double repeatedKillMultiplier(ServerPlayer player, String bossId) {
        if (!ExperienceConfig.REPEATED_BOSS_XP) {
            return PlayerExperienceData.getBossKills(player, bossId) == 0 ? 1.0D : 0.0D;
        }

        int kills = PlayerExperienceData.getBossKills(player, bossId);
        if (kills <= 0) {
            return 1.0D;
        }
        if (kills == 1) {
            return ExperienceConfig.SECOND_BOSS_KILL_MULTIPLIER;
        }
        return ExperienceConfig.REPEATED_BOSS_KILL_MULTIPLIER;
    }
}
