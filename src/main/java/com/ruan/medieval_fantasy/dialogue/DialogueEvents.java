package com.ruan.medieval_fantasy.dialogue;

import com.ruan.medieval_fantasy.ExampleMod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class DialogueEvents {

    private DialogueEvents() {
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        DialogueMemory.copy(event.getOriginal(), event.getEntity());
    }
}
