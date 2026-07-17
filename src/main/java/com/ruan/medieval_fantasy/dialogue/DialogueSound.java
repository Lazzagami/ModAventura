package com.ruan.medieval_fantasy.dialogue;

import com.ruan.medieval_fantasy.ExampleMod;
import net.minecraft.resources.ResourceLocation;

public final class DialogueSound {

    private DialogueSound() {
    }

    public static ResourceLocation voiceLocation(String voiceId) {
        if (voiceId == null || voiceId.isBlank()) {
            return null;
        }

        String path = voiceId.endsWith(".ogg") ? voiceId.substring(0, voiceId.length() - 4) : voiceId;
        return new ResourceLocation(ExampleMod.MODID, "dialogue/" + path);
    }
}
