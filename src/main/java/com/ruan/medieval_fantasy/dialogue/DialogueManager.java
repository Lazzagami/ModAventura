package com.ruan.medieval_fantasy.dialogue;

import com.ruan.medieval_fantasy.dialogue.network.DialogueNetworkHandler;
import com.ruan.medieval_fantasy.entity.custom.CavaleiroDasCinzas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class DialogueManager {

    private static final String ACTIVE_ROOT = "medieval_fantasy_active_dialogue";

    private DialogueManager() {
    }

    public static void startDialogue(ServerPlayer player, LivingEntity speaker, String dialogueId, String startNode) {
        DialogueRegistry.get(player.server, dialogueId).ifPresent(tree -> {
            String nodeId = startNode == null || startNode.isBlank() ? tree.getStart() : startNode;
            if (tree.getNode(nodeId) == null) {
                return;
            }

            CompoundTag active = active(player);
            active.putBoolean("active", true);
            active.putString("dialogue", dialogueId);
            active.putString("node", nodeId);
            active.putInt("speaker", speaker.getId());

            if (speaker instanceof CavaleiroDasCinzas boss) {
                boss.setDialogueMode(true);
            }

            DialogueNetworkHandler.open(player, DialogueRegistry.toJson(tree), nodeId, speaker.getId());
            runNodeEntryActions(player, speaker, tree.getNode(nodeId));
        });
    }

    public static void chooseOption(ServerPlayer player, int speakerEntityId, String dialogueId, String nodeId, int optionIndex) {
        CompoundTag active = active(player);
        if (!active.getBoolean("active")
                || !dialogueId.equals(active.getString("dialogue"))
                || !nodeId.equals(active.getString("node"))
                || speakerEntityId != active.getInt("speaker")) {
            return;
        }

        Entity entity = findEntity(player, speakerEntityId);
        if (!(entity instanceof LivingEntity speaker)) {
            endDialogue(player);
            return;
        }

        DialogueRegistry.get(player.server, dialogueId).ifPresent(tree -> {
            DialogueNode node = tree.getNode(nodeId);
            if (node == null) {
                endDialogue(player);
                return;
            }

            String next = node.getNext();
            if (optionIndex >= 0) {
                if (optionIndex >= node.getOptions().size()) {
                    return;
                }

                DialogueOption option = node.getOptions().get(optionIndex);
                if (!option.getConditions().stream().allMatch(condition -> condition.test(player))) {
                    return;
                }

                option.getActions().forEach(action -> runAction(player, speaker, action));
                next = option.getNext();
            } else {
                node.getActions().forEach(action -> runAction(player, speaker, action));
            }

            if (next == null || next.isBlank()) {
                endDialogue(player);
                return;
            }

            DialogueNode nextNode = tree.getNode(next);
            if (nextNode == null) {
                endDialogue(player);
                return;
            }

            active.putString("node", next);
            DialogueNetworkHandler.open(player, DialogueRegistry.toJson(tree), next, speaker.getId());
            runNodeEntryActions(player, speaker, nextNode);
        });
    }

    public static void endDialogue(ServerPlayer player) {
        CompoundTag active = active(player);
        int speakerId = active.getInt("speaker");
        Entity entity = findEntity(player, speakerId);
        if (entity instanceof CavaleiroDasCinzas boss) {
            boss.setDialogueMode(false);
        }
        active.putBoolean("active", false);
        DialogueNetworkHandler.close(player);
    }

    private static void runNodeEntryActions(ServerPlayer player, LivingEntity speaker, DialogueNode node) {
        if (node.getAnimation() != null && speaker instanceof CavaleiroDasCinzas boss) {
            boss.playDialogueAnimation(node.getAnimation(), node.getAnimationTicks());
        }
        node.getEntryActions().forEach(action -> runAction(player, speaker, action));
    }

    private static void runAction(ServerPlayer player, LivingEntity speaker, DialogueAction action) {
        switch (action.getType()) {
            case "set_memory_bool" -> DialogueMemory.setBoolean(player, action.getKey(), action.getBooleanValue());
            case "set_memory_int" -> DialogueMemory.setInt(player, action.getKey(), action.getAmount());
            case "add_memory_int" -> DialogueMemory.addInt(player, action.getKey(), action.getAmount());
            case "set_memory_string" -> DialogueMemory.setString(player, action.getKey(), action.getValue());
            case "play_animation", "boss_animation" -> {
                if (speaker instanceof CavaleiroDasCinzas boss) {
                    boss.playDialogueAnimation(action.getAnimation(), action.getDuration());
                }
            }
            case "boss_step_sound", "step_sound" -> {
                speaker.level().playSound(null, speaker.blockPosition(), SoundEvents.NETHERITE_BLOCK_STEP, speaker.getSoundSource(), action.getVolume(), action.getPitch());
                speaker.level().playSound(null, speaker.blockPosition(), SoundEvents.CHAIN_STEP, speaker.getSoundSource(), action.getVolume() * 0.45F, action.getPitch() * 0.82F);
            }
            case "start_boss_fight" -> {
                if (speaker instanceof CavaleiroDasCinzas boss) {
                    boss.beginCombatAfterDialogue(player);
                }
            }
            default -> {
            }
        }
    }

    private static CompoundTag active(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ACTIVE_ROOT)) {
            persistent.put(ACTIVE_ROOT, new CompoundTag());
        }
        return persistent.getCompound(ACTIVE_ROOT);
    }

    private static Entity findEntity(ServerPlayer player, int entityId) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        for (Entity entity : serverLevel.getAllEntities()) {
            if (entity.getId() == entityId) {
                return entity;
            }
        }
        return null;
    }
}
