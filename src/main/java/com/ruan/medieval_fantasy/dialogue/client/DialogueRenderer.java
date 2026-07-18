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
                                 List<DialogueOption> visibleOptions, int selectedOptionIndex) {
        int boxWidth = getBoxWidth(screenWidth, visibleOptions);
        int boxHeight = getBoxHeight(font, node.getText(), visibleOptions, screenWidth, screenHeight);
        int x = (screenWidth - boxWidth) / 2;
        int y = screenHeight - boxHeight - 34;

        drawStringShadow(graphics, font, node.getSpeaker(), x, y, 0xFFFFB36A);
        drawWordWrapShadow(graphics, font, visibleText, x, y + 18, boxWidth, 0xFFE8DED2);

        if (complete) {
            renderOptions(graphics, font, node.getText(), visibleOptions, mouseX, mouseY, x, y, boxWidth, boxHeight, selectedOptionIndex);
        } else {
            drawStringShadow(graphics, font, "Clique para revelar", x + boxWidth - 105, y + boxHeight - 8, 0xFFB8A898);
        }
    }

    private static void renderOptions(GuiGraphics graphics, Font font, String fullText, List<DialogueOption> options,
                                      int mouseX, int mouseY, int x, int y, int boxWidth, int boxHeight, int selectedOptionIndex) {
        if (options.isEmpty()) {
            int optionY = y + boxHeight - 8;
            int color = isInside(mouseX, mouseY, x + boxWidth - 96, optionY - 4, 86, 16) ? 0xFFFFD27D : 0xFFE8DED2;
            drawStringShadow(graphics, font, "Continuar >", x + boxWidth - 92, optionY, color);
            return;
        }

        int startY = getOptionsStartY(font, fullText, boxWidth, y);
        for (int i = 0; i < options.size(); i++) {
            String optionText = (i + 1) + ". " + options.get(i).getText();
            int optionHeight = getOptionHeight(font, optionText, boxWidth);
            boolean hovered = isInside(mouseX, mouseY, x, startY - 3, boxWidth, optionHeight);
            boolean selected = i == selectedOptionIndex;
            int color = hovered || selected ? 0xFFFFD27D : 0xFFE8DED2;
            String prefix = selected ? "> " : "  ";
            drawWordWrapShadow(graphics, font, prefix + optionText, x, startY, boxWidth, color);
            startY += optionHeight + getOptionGap(options);
        }
    }

    public static int getBoxWidth(int screenWidth) {
        return Math.min(screenWidth - 80, 660);
    }

    public static int getBoxWidth(int screenWidth, List<DialogueOption> visibleOptions) {
        int maxWidth = visibleOptions.size() >= 4 ? 560 : 660;
        return Math.max(240, Math.min(screenWidth - 80, maxWidth));
    }

    public static int getBoxHeight(Font font, String fullText, List<DialogueOption> visibleOptions, int screenWidth, int screenHeight) {
        int boxWidth = getBoxWidth(screenWidth, visibleOptions);
        int textLines = Math.max(1, font.split(Component.literal(fullText), boxWidth).size());
        int optionsHeight = 16;
        if (!visibleOptions.isEmpty()) {
            optionsHeight = 0;
            for (int i = 0; i < visibleOptions.size(); i++) {
                String optionText = (i + 1) + ". " + visibleOptions.get(i).getText();
                optionsHeight += getOptionHeight(font, optionText, boxWidth) + getOptionGap(visibleOptions);
            }
        }

        int wantedHeight = 26 + textLines * 10 + 10 + optionsHeight + 14;
        return Math.min(screenHeight - 70, Math.max(62, wantedHeight));
    }

    public static int getClickedOption(Font font, String fullText, List<DialogueOption> visibleOptions,
                                       int mouseX, int mouseY, int screenWidth, int screenHeight) {
        int boxWidth = getBoxWidth(screenWidth, visibleOptions);
        int boxHeight = getBoxHeight(font, fullText, visibleOptions, screenWidth, screenHeight);
        int x = (screenWidth - boxWidth) / 2;
        int y = screenHeight - boxHeight - 34;

        if (visibleOptions.isEmpty()) {
            int optionY = y + boxHeight - 10;
            return isInside(mouseX, mouseY, x + boxWidth - 104, optionY - 4, 96, 18) ? -1 : -2;
        }

        int optionY = getOptionsStartY(font, fullText, boxWidth, y);
        for (int i = 0; i < visibleOptions.size(); i++) {
            String optionText = (i + 1) + ". " + visibleOptions.get(i).getText();
            int optionHeight = getOptionHeight(font, optionText, boxWidth);
            if (isInside(mouseX, mouseY, x, optionY - 3, boxWidth, optionHeight)) {
                return i;
            }
            optionY += optionHeight + getOptionGap(visibleOptions);
        }
        return -2;
    }

    private static int getOptionsStartY(Font font, String fullText, int boxWidth, int boxY) {
        int textLines = Math.max(1, font.split(Component.literal(fullText), boxWidth).size());
        return boxY + 26 + textLines * 10 + 10;
    }

    private static int getOptionHeight(Font font, String text, int boxWidth) {
        int lines = Math.max(1, font.split(Component.literal(text), boxWidth - 18).size());
        return lines * 10 + 2;
    }

    private static int getOptionGap(List<DialogueOption> options) {
        return options.size() >= 4 ? 1 : 4;
    }

    private static void drawStringShadow(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        graphics.drawString(font, text, x + 1, y + 1, 0xDD000000, false);
        graphics.drawString(font, text, x, y, color, false);
    }

    private static void drawWordWrapShadow(GuiGraphics graphics, Font font, String text, int x, int y, int width, int color) {
        Component component = Component.literal(text);
        graphics.drawWordWrap(font, component, x + 1, y + 1, width, 0xDD000000);
        graphics.drawWordWrap(font, component, x, y, width, color);
    }

    public static boolean isInside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
