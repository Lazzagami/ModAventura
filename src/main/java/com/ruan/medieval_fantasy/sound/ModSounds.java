package com.ruan.medieval_fantasy.sound;

import com.ruan.medieval_fantasy.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ExampleMod.MODID);

    public static final RegistryObject<SoundEvent> ASH_KNIGHT_BATTLE =
            registerVariableRange("music.boss.ash_knight_battle");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> registerVariableRange(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ExampleMod.MODID, name)));
    }
}
