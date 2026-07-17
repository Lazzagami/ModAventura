package com.ruan.medieval_fantasy.dialogue.client;

import com.ruan.medieval_fantasy.dialogue.DialogueCondition;
import com.ruan.medieval_fantasy.origin.OriginType;
import com.ruan.medieval_fantasy.origin.client.ClientOriginData;

public final class DialogueClientCondition {

    private DialogueClientCondition() {
    }

    public static boolean test(DialogueCondition condition) {
        String type = condition.getType();
        return switch (type) {
            case "origin" -> ClientOriginData.getOrigin() == OriginType.fromId(condition.getValue());
            default -> true;
        };
    }
}
