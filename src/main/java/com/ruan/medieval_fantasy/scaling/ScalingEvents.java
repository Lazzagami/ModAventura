package com.ruan.medieval_fantasy.scaling;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.scaling.network.ScalingNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class ScalingEvents {

    private ScalingEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerScalingManager.recalculate(player);
            ScalingNetworkHandler.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerScalingManager.recalculate(player);
            ScalingNetworkHandler.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerScalingManager.recalculate(player);
            ScalingNetworkHandler.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getPersistentData().put(PlayerScalingData.ROOT_TAG,
                    event.getOriginal().getPersistentData().getCompound(PlayerScalingData.ROOT_TAG).copy());
            PlayerScalingManager.recalculate(player);
            ScalingNetworkHandler.sync(player);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChanged(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerScalingManager.recalculate(player);
        }
    }
}
