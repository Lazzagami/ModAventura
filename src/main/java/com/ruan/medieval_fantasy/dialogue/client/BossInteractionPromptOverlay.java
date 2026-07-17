package com.ruan.medieval_fantasy.dialogue.client;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.entity.custom.CavaleiroDasCinzas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BossInteractionPromptOverlay {

    private static final String PROMPT_TEXT = "Botao direito - Falar";
    private static final int BOX_WIDTH = 138;
    private static final int BOX_HEIGHT = 26;

    private static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || minecraft.screen != null || minecraft.hitResult == null) {
            return;
        }

        if (minecraft.hitResult.getType() != HitResult.Type.ENTITY
                || !(minecraft.hitResult instanceof EntityHitResult entityHit)
                || !(entityHit.getEntity() instanceof CavaleiroDasCinzas boss)
                || !boss.canStartDialogueClient()
                || player.distanceToSqr(boss) > 25.0D) {
            return;
        }

        renderPrompt(graphics, minecraft.font, screenWidth, screenHeight);
    };

    private BossInteractionPromptOverlay() {
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("ash_knight_interaction_prompt", OVERLAY);
    }

    private static void renderPrompt(GuiGraphics graphics, Font font, int screenWidth, int screenHeight) {
        int x = screenWidth / 2 - BOX_WIDTH / 2;
        int y = screenHeight - 92;

        graphics.fill(x, y, x + BOX_WIDTH, y + BOX_HEIGHT, 0xB5000000);
        graphics.fill(x + 1, y + 1, x + BOX_WIDTH - 1, y + BOX_HEIGHT - 1, 0xAA21140F);
        graphics.fill(x, y, x + BOX_WIDTH, y + 1, 0xFFE09A3D);
        graphics.fill(x, y + BOX_HEIGHT - 1, x + BOX_WIDTH, y + BOX_HEIGHT, 0xFF4A2B16);

        int mouseX = x + 10;
        int mouseY = y + 5;
        drawMouseIcon(graphics, mouseX, mouseY);

        graphics.drawString(font, PROMPT_TEXT, x + 34, y + 9, 0xFFFFDFA8, true);
    }

    private static void drawMouseIcon(GuiGraphics graphics, int x, int y) {
        graphics.fill(x + 4, y, x + 16, y + 2, 0xFFE8D6B0);
        graphics.fill(x + 2, y + 2, x + 18, y + 14, 0xFFE8D6B0);
        graphics.fill(x + 4, y + 14, x + 16, y + 18, 0xFFE8D6B0);

        graphics.fill(x + 4, y + 3, x + 10, y + 10, 0xFF2A1A12);
        graphics.fill(x + 10, y + 3, x + 16, y + 10, 0xFFFF8A1C);
        graphics.fill(x + 9, y + 2, x + 11, y + 11, 0xFFE8D6B0);
        graphics.fill(x + 8, y + 1, x + 12, y + 4, 0xFFE8D6B0);
    }
}
