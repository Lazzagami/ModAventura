package com.ruan.medieval_fantasy.scaling.client;

import com.ruan.medieval_fantasy.scaling.PlayerAttribute;

import java.util.EnumMap;
import java.util.Map;

public final class ClientScalingData {

    private static final Map<PlayerAttribute, Integer> ATTRIBUTES = new EnumMap<>(PlayerAttribute.class);
    private static int level;
    private static int availablePoints;

    private ClientScalingData() {
    }

    public static void set(int syncedLevel, int syncedAvailablePoints, Map<PlayerAttribute, Integer> syncedAttributes) {
        level = syncedLevel;
        availablePoints = syncedAvailablePoints;
        ATTRIBUTES.clear();
        ATTRIBUTES.putAll(syncedAttributes);
    }

    public static int getLevel() {
        return level;
    }

    public static int getAvailablePoints() {
        return availablePoints;
    }

    public static int getAttribute(PlayerAttribute attribute) {
        return ATTRIBUTES.getOrDefault(attribute, 0);
    }
}
