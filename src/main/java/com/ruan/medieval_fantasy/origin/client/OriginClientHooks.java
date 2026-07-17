package com.ruan.medieval_fantasy.origin.client;

import net.minecraft.client.Minecraft;

public final class OriginClientHooks {

    private OriginClientHooks() {
    }

    public static void openSelection() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof OriginSelectionScreen) && ClientOriginData.getOrigin().getId().equals("none")) {
            minecraft.setScreen(new OriginSelectionScreen());
        }
    }
}
