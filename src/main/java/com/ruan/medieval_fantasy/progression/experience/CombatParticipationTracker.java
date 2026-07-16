package com.ruan.medieval_fantasy.progression.experience;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CombatParticipationTracker {

    private static final Map<UUID, Map<UUID, Long>> PARTICIPATION = new HashMap<>();

    private CombatParticipationTracker() {
    }

    public static void recordDamage(LivingEntity target, ServerPlayer player) {
        PARTICIPATION.computeIfAbsent(target.getUUID(), ignored -> new HashMap<>())
                .put(player.getUUID(), target.level().getGameTime());
    }

    public static List<ServerPlayer> getParticipants(LivingEntity target) {
        List<ServerPlayer> participants = new ArrayList<>();
        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return participants;
        }

        Map<UUID, Long> hits = PARTICIPATION.remove(target.getUUID());
        if (hits == null || hits.isEmpty()) {
            return participants;
        }

        long now = target.level().getGameTime();
        double maxDistanceSqr = ExperienceConfig.PARTICIPATION_DISTANCE * ExperienceConfig.PARTICIPATION_DISTANCE;
        for (ServerPlayer player : serverLevel.players()) {
            Long lastHit = hits.get(player.getUUID());
            if (lastHit == null) {
                continue;
            }

            if (now - lastHit <= ExperienceConfig.PARTICIPATION_TIMEOUT_TICKS
                    && player.distanceToSqr(target) <= maxDistanceSqr
                    && !player.isSpectator()) {
                participants.add(player);
            }
        }

        return participants;
    }
}
