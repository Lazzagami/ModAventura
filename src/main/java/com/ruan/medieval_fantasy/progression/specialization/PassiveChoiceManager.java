package com.ruan.medieval_fantasy.progression.specialization;

import com.ruan.medieval_fantasy.progression.specialization.network.SpecializationNetworkHandler;
import com.ruan.medieval_fantasy.scaling.PlayerAttribute;
import com.ruan.medieval_fantasy.scaling.PlayerScalingData;
import com.ruan.medieval_fantasy.scaling.PlayerScalingManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class PassiveChoiceManager {

    private PassiveChoiceManager() {
    }

    public static void refreshUnlocks(ServerPlayer player) {
        for (PlayerAttribute attribute : PlayerAttribute.values()) {
            int invested = PlayerScalingData.getAttribute(player, attribute);
            for (int milestone : PassiveRegistry.milestones()) {
                if (invested < milestone || PassiveChoiceData.hasChoice(player, attribute, milestone)) {
                    continue;
                }

                if (PassiveRegistry.options(attribute, milestone).isEmpty()) {
                    continue;
                }

                if (!PassiveChoiceData.isPending(player, attribute, milestone)) {
                    PassiveChoiceData.setPending(player, attribute, milestone, true);
                    player.sendSystemMessage(Component.literal("Marco desbloqueado: " + attribute.id() + " " + milestone + ". Abra Atributos com K."));
                }
            }
        }

        PassiveEffectHandler.applyPermanentPassives(player);
        SpecializationNetworkHandler.sync(player);
    }

    public static boolean choose(ServerPlayer player, PlayerAttribute attribute, int milestone, int option) {
        if (PlayerScalingData.getAttribute(player, attribute) < milestone) {
            return false;
        }

        if (PassiveChoiceData.hasChoice(player, attribute, milestone)) {
            return false;
        }

        Optional<PassiveDefinition> passive = PassiveRegistry.option(attribute, milestone, option);
        if (passive.isEmpty()) {
            return false;
        }

        PassiveChoiceData.setChoice(player, attribute, milestone, passive.get().id());
        PassiveChoiceData.unlockTitle(player, passive.get().titleId());
        if (PassiveChoiceData.equippedTitle(player).isBlank()) {
            PassiveChoiceData.equipTitle(player, passive.get().titleId());
        }

        PassiveEffectHandler.applyPermanentPassives(player);
        PlayerScalingManager.recalculate(player);
        SpecializationNetworkHandler.sync(player);
        player.sendSystemMessage(Component.literal("Passiva escolhida: " + passive.get().displayName() + ". Título desbloqueado: " + passive.get().titleName()));
        return true;
    }

    public static void resetChoice(ServerPlayer player, PlayerAttribute attribute, int milestone) {
        PassiveEffectHandler.removeTemporaryPassives(player);
        PassiveChoiceData.clearChoice(player, attribute, milestone);
        PassiveChoiceData.setPending(player, attribute, milestone, PlayerScalingData.getAttribute(player, attribute) >= milestone
                && !PassiveRegistry.options(attribute, milestone).isEmpty());
        PassiveEffectHandler.applyPermanentPassives(player);
        PlayerScalingManager.recalculate(player);
        SpecializationNetworkHandler.sync(player);
    }

    public static void resetAllChoices(ServerPlayer player) {
        PassiveEffectHandler.removeTemporaryPassives(player);
        for (String key : new java.util.ArrayList<>(PassiveChoiceData.choices(player).getAllKeys())) {
            PassiveChoiceData.choices(player).remove(key);
        }
        for (String key : new java.util.ArrayList<>(PassiveChoiceData.pending(player).getAllKeys())) {
            PassiveChoiceData.pending(player).remove(key);
        }
        for (String key : new java.util.ArrayList<>(PassiveChoiceData.titles(player).getAllKeys())) {
            PassiveChoiceData.titles(player).remove(key);
        }
        PassiveChoiceData.equipTitle(player, "");
        refreshUnlocks(player);
        PassiveEffectHandler.applyPermanentPassives(player);
        PlayerScalingManager.recalculate(player);
        SpecializationNetworkHandler.sync(player);
    }

    public static boolean hasPassive(ServerPlayer player, String passiveId) {
        for (String key : PassiveChoiceData.choices(player).getAllKeys()) {
            if (passiveId.equals(PassiveChoiceData.choices(player).getString(key))) {
                return true;
            }
        }
        return false;
    }
}
