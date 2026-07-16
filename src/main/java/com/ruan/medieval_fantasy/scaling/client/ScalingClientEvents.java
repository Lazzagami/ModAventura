package com.ruan.medieval_fantasy.scaling.client;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.progression.experience.network.ExperienceNetworkHandler;
import com.ruan.medieval_fantasy.progression.specialization.network.SpecializationNetworkHandler;
import com.ruan.medieval_fantasy.scaling.network.ScalingNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class ScalingClientEvents {

    private ScalingClientEvents() {
    }

    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {

        private ModBus() {
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(ScalingKeyMappings.OPEN_ATTRIBUTES);
        }
    }

    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
    public static final class ForgeBus {

        private ForgeBus() {
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null || minecraft.screen != null) {
                return;
            }

            while (ScalingKeyMappings.OPEN_ATTRIBUTES.consumeClick()) {
                ScalingNetworkHandler.requestSyncFromServer();
                ExperienceNetworkHandler.requestSyncFromServer();
                SpecializationNetworkHandler.requestSyncFromServer();
                minecraft.setScreen(new AttributeScreen());
            }
        }
    }
}
