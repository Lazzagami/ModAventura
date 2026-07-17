package com.ruan.medieval_fantasy.dialogue;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public final class DialogueTrigger {

    private DialogueTrigger() {
    }

    public static void start(ServerPlayer player, LivingEntity speaker, String dialogueId) {
        DialogueManager.startDialogue(player, speaker, dialogueId, null);
    }

    public static void startAt(ServerPlayer player, LivingEntity speaker, String dialogueId, String nodeId) {
        DialogueManager.startDialogue(player, speaker, dialogueId, nodeId);
    }
}
