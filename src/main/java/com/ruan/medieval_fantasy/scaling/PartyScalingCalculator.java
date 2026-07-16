package com.ruan.medieval_fantasy.scaling;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public final class PartyScalingCalculator {

    private PartyScalingCalculator() {
    }

    public static List<ServerPlayer> nearbyParticipants(Entity anchor) {
        List<ServerPlayer> participants = new ArrayList<>();
        if (!(anchor.level() instanceof ServerLevel serverLevel)) {
            return participants;
        }

        double radiusSqr = ScalingConfig.BOSS_PARTICIPATION_RADIUS * ScalingConfig.BOSS_PARTICIPATION_RADIUS;
        for (ServerPlayer player : serverLevel.players()) {
            if (!player.isSpectator() && player.isAlive() && player.distanceToSqr(anchor) <= radiusSqr) {
                participants.add(player);
            }
        }

        return participants;
    }

    public static double averageLevel(List<ServerPlayer> players) {
        if (players.isEmpty()) {
            return 0.0D;
        }

        int total = 0;
        for (ServerPlayer player : players) {
            total += PlayerScalingData.getLevel(player);
        }

        return total / (double) players.size();
    }

    public static double playerHealthMultiplier(int players) {
        return switch (Math.max(1, players)) {
            case 1 -> 1.0D;
            case 2 -> 1.6D;
            case 3 -> 2.1D;
            default -> 2.5D;
        };
    }

    public static double extraPlayerDamageMultiplier(int players) {
        return 1.0D + Math.max(0, players - 1) * ScalingConfig.BOSS_DAMAGE_PER_EXTRA_PLAYER;
    }
}
