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
        int boxWidth = getBoxWidth(screenWidth);
        int boxHeight = getBoxHeight(font, node.getText(), visibleOptions, screenWidth, screenHeight);
        int x = (screenWidth - boxWidth) / 2;
        int y = screenHeight - boxHeight - 28;

        graphics.fill(x - 2, y - 2, x + boxWidth + 2, y + boxHeight + 2, 0xAA3A160B);
        graphics.fill(x, y, x + boxWidth, y + boxHeight, 0xDD120D0A);
        graphics.fill(x, y, x + boxWidth, y + 22, 0xCC5A250E);
        graphics.drawString(font, node.getSpeaker(), x + 12, y + 7, 0xFFFFB36A, false);
        graphics.drawWordWrap(font, Component.literal(visibleText), x + 14, y + 34, boxWidth - 28, 0xFFE8DED2);

        if (complete) {
            renderOptions(graphics, font, node.getText(), visibleOptions, mouseX, mouseY, x, y, boxWidth, boxHeight);
        } else {
            graphics.drawString(font, "Clique para revelar", x + boxWidth - 105, y + boxHeight - 14, 0xFF9F8F84, false);
        }
    }

    private static void renderOptions(GuiGraphics graphics, Font font, String fullText, List<DialogueOption> options,
                                      int mouseX, int mouseY, int x, int y, int boxWidth, int boxHeight) {
        if (options.isEmpty()) {
            int optionY = y + boxHeight - 22;
            int color = isInside(mouseX, mouseY, x + boxWidth - 96, optionY - 4, 86, 16) ? 0xFFFFD27D : 0xFFE8DED2;
            graphics.drawString(font, "Continuar >", x + boxWidth - 92, optionY, color, false);
            return;
        }

        int startY = getOptionsStartY(font, fullText, boxWidth, y);
        for (int i = 0; i < options.size(); i++) {
            String optionText = (i + 1) + ". " + options.get(i).getText();
            int optionHeight = getOptionHeight(font, optionText, boxWidth);
            int color = isInside(mouseX, mouseY, x + 12, startY - 3, boxWidth - 24, optionHeight) ? 0xFFFFD27D : 0xFFE8DED2;
            graphics.drawWordWrap(font, Component.literal(optionText), x + 16, startY, boxWidth - 32, color);
            startY += optionHeight + 3;
        }
    }

    public static int getBoxWidth(int screenWidth) {
        return Math.min(screenWidth - 32, 720);
    }

    public static int getBoxHeight(Font font, String fullText, List<DialogueOption> visibleOptions, int screenWidth, int screenHeight) {
        int boxWidth = getBoxWidth(screenWidth);
        int textLines = Math.max(1, font.split(Component.literal(fullText), boxWidth - 28).size());
        int optionsHeight = 16;
        if (!visibleOptions.isEmpty()) {
            optionsHeight = 0;
            for (int i = 0; i < visibleOptions.size(); i++) {
                String optionText = (i + 1) + ". " + visibleOptions.get(i).getText();
                optionsHeight += getOptionHeight(font, optionText, boxWidth) + 3;
            }
        }

        int wantedHeight = 64 + textLines * 10 + 14 + optionsHeight + 18;
        return Math.min(screenHeight - 42, Math.max(138, wantedHeight));
    }

    public static int getClickedOption(Font font, String fullText, List<DialogueOption> visibleOptions,
                                       int mouseX, int mouseY, int screenWidth, int screenHeight) {
        int boxWidth = getBoxWidth(screenWidth);
        int boxHeight = getBoxHeight(font, fullText, visibleOptions, screenWidth, screenHeight);
        int x = (screenWidth - boxWidth) / 2;
        int y = screenHeight - boxHeight - 28;

        if (visibleOptions.isEmpty()) {
            int optionY = y + boxHeight - 24;
            return isInside(mouseX, mouseY, x + boxWidth - 104, optionY - 4, 96, 18) ? -1 : -2;
        }

        int optionY = getOptionsStartY(font, fullText, boxWidth, y);
        for (int i = 0; i < visibleOptions.size(); i++) {
            String optionText = (i + 1) + ". " + visibleOptions.get(i).getText();
            int optionHeight = getOptionHeight(font, optionText, boxWidth);
            if (isInside(mouseX, mouseY, x + 12, optionY - 3, boxWidth - 24, optionHeight)) {
                return i;
            }
            optionY += optionHeight + 3;
        }
        return -2;
    }

    private static int getOptionsStartY(Font font, String fullText, int boxWidth, int boxY) {
        int textLines = Math.max(1, font.split(Component.literal(fullText), boxWidth - 28).size());
        return boxY + 42 + textLines * 10 + 10;
    }

    private static int getOptionHeight(Font font, String text, int boxWidth) {
        int lines = Math.max(1, font.split(Component.literal(text), boxWidth - 32).size());
        return lines * 10 + 4;
    }

    public static boolean isInside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
