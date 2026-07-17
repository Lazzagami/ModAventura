package com.ruan.medieval_fantasy.dialogue.client;

import com.ruan.medieval_fantasy.dialogue.DialogueNode;
import com.ruan.medieval_fantasy.dialogue.DialogueOption;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class DialogueRenderer {

    private DialogueRenderer() {
    }

    public static void renderBox(GuiGraphics graphics, Font font, DialogueNode node, String visibleText,
                                 int mouseX, int mouseY, int screenWidth, int screenHeight, boolean complete,
                                 List<DialogueOption> visibleOptions) {
        int boxWidth = Math.min(screenWidth - 48, 430);
        int boxHeight = 118;
        int x = (screenWidth - boxWidth) / 2;
        int y = screenHeight - boxHeight - 28;

        graphics.fill(x - 2, y - 2, x + boxWidth + 2, y + boxHeight + 2, 0xAA3A160B);
        graphics.fill(x, y, x + boxWidth, y + boxHeight, 0xDD120D0A);
        graphics.fill(x, y, x + boxWidth, y + 22, 0xCC5A250E);
        graphics.drawString(font, node.getSpeaker(), x + 12, y + 7, 0xFFFFB36A, false);
        graphics.drawWordWrap(font, Component.literal(visibleText), x + 14, y + 34, boxWidth - 28, 0xFFE8DED2);

        if (complete) {
            renderOptions(graphics, font, visibleOptions, mouseX, mouseY, x, y, boxWidth);
        } else {
            graphics.drawString(font, "Clique para revelar", x + boxWidth - 105, y + boxHeight - 14, 0xFF9F8F84, false);
        }
    }

    private static void renderOptions(GuiGraphics graphics, Font font, List<DialogueOption> options, int mouseX, int mouseY, int x, int y, int boxWidth) {
        if (options.isEmpty()) {
            int optionY = y + 92;
            int color = isInside(mouseX, mouseY, x + boxWidth - 96, optionY - 4, 86, 16) ? 0xFFFFD27D : 0xFFE8DED2;
            graphics.drawString(font, "Continuar >", x + boxWidth - 92, optionY, color, false);
            return;
        }

        int startY = y + 76;
        for (int i = 0; i < options.size(); i++) {
            int optionY = startY + i * 17;
            int color = isInside(mouseX, mouseY, x + 12, optionY - 3, boxWidth - 24, 15) ? 0xFFFFD27D : 0xFFE8DED2;
            graphics.drawString(font, (i + 1) + ". " + options.get(i).getText(), x + 16, optionY, color, false);
        }
    }

    public static boolean isInside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
