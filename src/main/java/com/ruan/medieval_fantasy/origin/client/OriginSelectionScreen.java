package com.ruan.medieval_fantasy.origin.client;

import com.ruan.medieval_fantasy.origin.OriginData;
import com.ruan.medieval_fantasy.origin.OriginRegistry;
import com.ruan.medieval_fantasy.origin.OriginType;
import com.ruan.medieval_fantasy.origin.network.OriginNetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class OriginSelectionScreen extends Screen {

    private final List<OriginData> origins = new ArrayList<>(OriginRegistry.all());
    private OriginType selected = OriginType.NONE;

    public OriginSelectionScreen() {
        super(Component.literal("Escolha sua Origem"));
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int titleColor = 0xFFFFD28A;
        graphics.drawCenteredString(font, "Escolha sua Origem", width / 2, 26, titleColor);
        graphics.drawCenteredString(font, "Esta escolha sera permanente e acompanhara sua campanha.", width / 2, 42, 0xFFD6C8B8);

        int spacing = 12;
        int cardWidth = Math.min(300, (width - 64 - spacing * (origins.size() - 1)) / origins.size());
        int cardHeight = Math.min(230, Math.max(172, height - 150));
        int totalWidth = cardWidth * origins.size() + spacing * (origins.size() - 1);
        int startX = (width - totalWidth) / 2;
        int y = 72;

        for (int i = 0; i < origins.size(); i++) {
            OriginData data = origins.get(i);
            int x = startX + i * (cardWidth + spacing);
            renderCard(graphics, data, x, y, cardWidth, cardHeight, mouseX, mouseY);
        }

        int buttonWidth = 190;
        int buttonHeight = 24;
        int buttonX = (width - buttonWidth) / 2;
        int buttonY = y + cardHeight + 24;
        boolean canConfirm = selected != OriginType.NONE;
        int buttonColor = canConfirm ? 0xCC7A3515 : 0x8844332B;
        if (canConfirm && isInside(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight)) {
            buttonColor = 0xDDAE4A1A;
        }
        graphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, buttonColor);
        graphics.drawCenteredString(font, canConfirm ? "Confirmar origem" : "Selecione uma origem", width / 2, buttonY + 8, 0xFFFFE3C2);

        if (selected != OriginType.NONE) {
            graphics.drawCenteredString(font, "Depois de confirmar, nao sera possivel alterar.", width / 2, buttonY + 34, 0xFFFF9F7C);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderCard(GuiGraphics graphics, OriginData data, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hovered = isInside(mouseX, mouseY, x, y, w, h);
        boolean selectedCard = selected == data.getType();
        int border = selectedCard ? 0xFFFFB347 : hovered ? 0xFF9D6A3E : 0xFF3B2B24;
        int fill = selectedCard ? 0xCC2A1810 : 0xBB14100D;

        graphics.fill(x - 2, y - 2, x + w + 2, y + h + 2, border);
        graphics.fill(x, y, x + w, y + h, fill);
        graphics.fill(x, y, x + w, y + 22, 0xAA5A250E);
        graphics.drawCenteredString(font, data.getDisplayName(), x + w / 2, y + 7, 0xFFFFD28A);
        int textWidth = w - 20;
        graphics.drawWordWrap(font, Component.literal(data.getSubtitle()), x + 10, y + 30, textWidth, 0xFFE5D1BE);

        int descriptionY = y + 52;
        graphics.drawWordWrap(font, Component.literal(data.getDescription()), x + 10, descriptionY, textWidth, 0xFFD6C8B8);
        int descriptionLines = Math.max(1, font.split(Component.literal(data.getDescription()), textWidth).size());
        int traitY = descriptionY + descriptionLines * 10 + 10;
        for (String trait : data.getTraits()) {
            String traitText = "- " + trait;
            int traitLines = Math.max(1, font.split(Component.literal(traitText), textWidth).size());
            if (traitY + traitLines * 10 > y + h - 8) {
                break;
            }
            graphics.drawWordWrap(font, Component.literal(traitText), x + 10, traitY, textWidth, 0xFFC8B6A2);
            traitY += traitLines * 10 + 3;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int spacing = 12;
        int cardWidth = Math.min(300, (width - 64 - spacing * (origins.size() - 1)) / origins.size());
        int cardHeight = Math.min(230, Math.max(172, height - 150));
        int totalWidth = cardWidth * origins.size() + spacing * (origins.size() - 1);
        int startX = (width - totalWidth) / 2;
        int y = 72;

        for (int i = 0; i < origins.size(); i++) {
            int x = startX + i * (cardWidth + spacing);
            if (isInside((int) mouseX, (int) mouseY, x, y, cardWidth, cardHeight)) {
                selected = origins.get(i).getType();
                return true;
            }
        }

        int buttonWidth = 190;
        int buttonHeight = 24;
        int buttonX = (width - buttonWidth) / 2;
        int buttonY = y + cardHeight + 24;
        if (selected != OriginType.NONE && isInside((int) mouseX, (int) mouseY, buttonX, buttonY, buttonWidth, buttonHeight)) {
            OriginNetworkHandler.select(selected);
            onClose();
            return true;
        }

        return true;
    }

    private boolean isInside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }
}
