package com.ruan.medieval_fantasy.scaling.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ScalingKeyMappings {

    public static final KeyMapping OPEN_ATTRIBUTES = new KeyMapping(
            "key.medieval_fantasy.open_attributes",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.medieval_fantasy"
    );

    private ScalingKeyMappings() {
    }
}
