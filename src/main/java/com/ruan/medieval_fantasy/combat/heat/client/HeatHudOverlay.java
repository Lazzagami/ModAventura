package com.ruan.medieval_fantasy.combat.heat.client;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.combat.heat.HeatManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HeatHudOverlay {

    private static final int BAR_WIDTH = 72;
    private static final int BAR_HEIGHT = 4;
    private static final float TEXT_SCALE = 0.65F;

    private static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null) {
            return;
        }

        int heat = ClientHeatData.getHeat();
        boolean holdingBlade = HeatManager.isEternalFireBlade(player.getMainHandItem())
                || HeatManager.isEternalFireBlade(player.getOffhandItem());

        if (!holdingBlade && heat <= 0) {
            return;
        }

        renderHeatBar(graphics, screenWidth, screenHeight, heat);
    };

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("eternal_fire_blade_heat", OVERLAY);
    }

    private static void renderHeatBar(GuiGraphics graphics, int screenWidth, int screenHeight, int heat) {
        int hotbarLeft = screenWidth / 2 - 91;
        int x = hotbarLeft - BAR_WIDTH - 8;
        int y = screenHeight - 28;
        int filled = Math.round((BAR_WIDTH - 2) * (heat / 100.0F));
        int color = getHeatColor(heat);

        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xAA000000);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xCC2A160E);
        graphics.fill(x + 1, y + 1, x + 1 + filled, y + BAR_HEIGHT - 1, color);
        drawSmallString(graphics, "CALOR " + heat + "%", x, y - 8, 0xFFE8D6B0);
    }

    private static void drawSmallString(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.pose().pushPose();
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
        graphics.drawString(Minecraft.getInstance().font, text, Math.round(x / TEXT_SCALE), Math.round(y / TEXT_SCALE), color, true);
        graphics.pose().popPose();
    }

    private static int getHeatColor(int heat) {
        if (heat >= 81) {
            boolean pulse = (System.currentTimeMillis() / 180L) % 2L == 0L;
            return pulse ? 0xFFFF2A00 : 0xFFFFB000;
        }

        if (heat >= 61) {
            return 0xFFFF3D00;
        }

        if (heat >= 41) {
            return 0xFFFF7A00;
        }

        if (heat >= 21) {
            return 0xFFFFB84D;
        }

        return 0xFF7A3B20;
    }
}
