package com.ruan.medieval_fantasy.progression.specialization;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.progression.specialization.network.SpecializationNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class SpecializationEvents {

    private SpecializationEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PassiveChoiceManager.refreshUnlocks(player);
            PassiveEffectHandler.applyPermanentPassives(player);
            SpecializationNetworkHandler.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PassiveEffectHandler.applyPermanentPassives(player);
            SpecializationNetworkHandler.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PassiveEffectHandler.applyPermanentPassives(player);
            SpecializationNetworkHandler.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PassiveChoiceData.copy(event.getOriginal(), player);
            PassiveEffectHandler.applyPermanentPassives(player);
            SpecializationNetworkHandler.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        PassiveEffectHandler.tick(player);
    }
}
