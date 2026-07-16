package com.ruan.medieval_fantasy.progression.experience.client;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.progression.experience.ExperienceConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ExperienceHudOverlay {

    private static final int BAR_WIDTH = 72;
    private static final int BAR_HEIGHT = 4;
    private static final float TEXT_SCALE = 0.65F;

    private static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> {
        if (!ExperienceConfig.SHOW_HUD || Minecraft.getInstance().player == null) {
            return;
        }

        renderExperienceBar(graphics, screenWidth, screenHeight);
    };

    private ExperienceHudOverlay() {
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("medieval_fantasy_experience", OVERLAY);
    }

    private static void renderExperienceBar(GuiGraphics graphics, int screenWidth, int screenHeight) {
        int hotbarRight = screenWidth / 2 + 91;
        int x = hotbarRight + 8;
        int y = screenHeight - 28;
        int filled = Math.round((BAR_WIDTH - 2) * ClientExperienceData.progress());

        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, 0xAA000000);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, 0xCC171A28);
        graphics.fill(x + 1, y + 1, x + 1 + filled, y + BAR_HEIGHT - 1, 0xFF7B5CFF);

        String text = "Nv " + ClientExperienceData.level() + "  "
                + ClientExperienceData.currentXp() + " / " + ClientExperienceData.requiredXp() + " XP";
        drawSmallString(graphics, text, x, y - 8, 0xFFE6D4FF);
    }

    private static void drawSmallString(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.pose().pushPose();
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
        graphics.drawString(Minecraft.getInstance().font, text, Math.round(x / TEXT_SCALE), Math.round(y / TEXT_SCALE), color, true);
        graphics.pose().popPose();
    }
}
