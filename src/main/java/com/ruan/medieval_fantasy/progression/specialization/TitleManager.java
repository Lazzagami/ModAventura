package com.ruan.medieval_fantasy.progression.specialization;

import com.ruan.medieval_fantasy.progression.specialization.network.SpecializationNetworkHandler;
import net.minecraft.server.level.ServerPlayer;

public final class TitleManager {

    private TitleManager() {
    }

    public static boolean equip(ServerPlayer player, String titleId) {
        if (titleId == null || titleId.isBlank()) {
            PassiveChoiceData.equipTitle(player, "");
            SpecializationNetworkHandler.sync(player);
            return true;
        }

        if (!PassiveChoiceData.hasTitle(player, titleId) || TitleRegistry.byId(titleId).isEmpty()) {
            return false;
        }

        PassiveChoiceData.equipTitle(player, titleId);
        SpecializationNetworkHandler.sync(player);
        return true;
    }

    public static String displayName(String titleId) {
        return TitleRegistry.byId(titleId).map(TitleDefinition::displayName).orElse("");
    }
}
