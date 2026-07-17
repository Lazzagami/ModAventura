package com.ruan.medieval_fantasy.dialogue;

import net.minecraft.server.level.ServerPlayer;

public class DialogueCondition {

    private String type;
    private String key;
    private String value;
    private int amount;
    private boolean booleanValue;

    public boolean test(ServerPlayer player) {
        String conditionType = type == null ? "" : type;
        return switch (conditionType) {
            case "memory_bool" -> DialogueMemory.getBoolean(player, key) == booleanValue;
            case "memory_int_at_least" -> DialogueMemory.getInt(player, key) >= amount;
            case "memory_int_below" -> DialogueMemory.getInt(player, key) < amount;
            case "memory_string" -> value != null && value.equals(DialogueMemory.getString(player, key));
            default -> true;
        };
    }
}
