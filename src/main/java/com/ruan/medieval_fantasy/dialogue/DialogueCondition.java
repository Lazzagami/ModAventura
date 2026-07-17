package com.ruan.medieval_fantasy.dialogue;

import com.ruan.medieval_fantasy.origin.OriginCondition;
import net.minecraft.server.level.ServerPlayer;

public class DialogueCondition {

    private String type;
    private String key;
    private String value;
    private int amount;
    private boolean booleanValue;

    public String getType() {
        return type == null ? "" : type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public int getAmount() {
        return amount;
    }

    public boolean getBooleanValue() {
        return booleanValue;
    }

    public boolean test(ServerPlayer player) {
        String conditionType = type == null ? "" : type;
        return switch (conditionType) {
            case "memory_bool" -> DialogueMemory.getBoolean(player, key) == booleanValue;
            case "memory_int_at_least" -> DialogueMemory.getInt(player, key) >= amount;
            case "memory_int_below" -> DialogueMemory.getInt(player, key) < amount;
            case "memory_string" -> value != null && value.equals(DialogueMemory.getString(player, key));
            case "origin" -> OriginCondition.hasOrigin(player, value);
            case "origin_reputation_at_least" -> OriginCondition.reputationAtLeast(player, key, amount);
            case "origin_reputation_below" -> OriginCondition.reputationBelow(player, key, amount);
            default -> true;
        };
    }
}
