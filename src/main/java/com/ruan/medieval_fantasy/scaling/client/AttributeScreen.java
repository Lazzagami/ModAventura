package com.ruan.medieval_fantasy.scaling.client;

import com.ruan.medieval_fantasy.progression.experience.client.ClientExperienceData;
import com.ruan.medieval_fantasy.progression.specialization.PassiveDefinition;
import com.ruan.medieval_fantasy.progression.specialization.PassiveRegistry;
import com.ruan.medieval_fantasy.progression.specialization.TitleRegistry;
import com.ruan.medieval_fantasy.progression.specialization.client.ClientSpecializationData;
import com.ruan.medieval_fantasy.progression.specialization.network.SpecializationNetworkHandler;
import com.ruan.medieval_fantasy.scaling.PlayerAttribute;
import com.ruan.medieval_fantasy.scaling.ScalingFormula;
import com.ruan.medieval_fantasy.scaling.network.ScalingNetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AttributeScreen extends Screen {

    private static final int PANEL_WIDTH = 380;
    private static final int PANEL_HEIGHT = 250;
    private static final int ROW_HEIGHT = 22;

    public AttributeScreen() {
        super(Component.literal("Atributos"));
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        int y = top + 48;

        addAttributeButton(left + PANEL_WIDTH - 34, y, PlayerAttribute.VITALITY);
        y += ROW_HEIGHT;
        addAttributeButton(left + PANEL_WIDTH - 34, y, PlayerAttribute.STRENGTH);
        y += ROW_HEIGHT;
        addAttributeButton(left + PANEL_WIDTH - 34, y, PlayerAttribute.DEFENSE);
        y += ROW_HEIGHT;
        addAttributeButton(left + PANEL_WIDTH - 34, y, PlayerAttribute.AGILITY);
        y += ROW_HEIGHT;
        addAttributeButton(left + PANEL_WIDTH - 34, y, PlayerAttribute.RELIC_CONTROL);

        addPendingChoiceButtons(left, top);
        addTitleButtons(left, top);
    }

    private void addAttributeButton(int x, int y, PlayerAttribute attribute) {
        addRenderableWidget(Button.builder(Component.literal("+"), button -> {
            ScalingNetworkHandler.investAttribute(attribute);
            ScalingNetworkHandler.requestSyncFromServer();
        }).bounds(x, y - 4, 20, 20).build());
    }

    private void addPendingChoiceButtons(int left, int top) {
        int y = top + 172;
        for (String key : ClientSpecializationData.pending().keySet()) {
            String[] parts = key.split("\\.");
            if (parts.length != 2) {
                continue;
            }

            PlayerAttribute attribute = attributeById(parts[0]);
            int milestone = parseInt(parts[1]);
            if (attribute == null || milestone <= 0) {
                continue;
            }

            int x = left + 18;
            for (PassiveDefinition option : PassiveRegistry.options(attribute, milestone)) {
                addRenderableWidget(Button.builder(Component.literal(option.displayName()), button -> {
                    SpecializationNetworkHandler.choosePassive(attribute, milestone, option.option());
                    SpecializationNetworkHandler.requestSyncFromServer();
                }).bounds(x, y, 150, 20).build());
                x += 158;
            }
            return;
        }
    }

    private void addTitleButtons(int left, int top) {
        int x = left + PANEL_WIDTH - 104;
        int y = top + 204;
        int added = 0;
        for (String titleId : ClientSpecializationData.titles()) {
            String title = TitleRegistry.byId(titleId).map(t -> t.displayName()).orElse(titleId);
            addRenderableWidget(Button.builder(Component.literal(shortText(title, 12)), button -> {
                SpecializationNetworkHandler.equipTitle(titleId);
                SpecializationNetworkHandler.requestSyncFromServer();
            }).bounds(x, y + added * 22, 86, 20).build());
            added++;
            if (added >= 2) {
                break;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);

        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xDD1A0F0A);
        graphics.fill(left + 2, top + 2, left + PANEL_WIDTH - 2, top + PANEL_HEIGHT - 2, 0xAA2B1810);

        graphics.drawCenteredString(font, title, width / 2, top + 10, 0xFFD08A3A);
        graphics.drawString(font, "Nivel: " + ClientExperienceData.level(), left + 18, top + 28, 0xFFE6D4B8, false);
        graphics.drawString(font, "Pontos disponiveis: " + ClientScalingData.getAvailablePoints(), left + 100, top + 28, 0xFFFFC86B, false);
        graphics.drawString(font, "Titulo: " + equippedTitleText(), left + 240, top + 28, 0xFFD08A3A, false);

        int y = top + 48;
        drawAttribute(graphics, left, y, "Vitalidade", PlayerAttribute.VITALITY, vitalityDescription());
        y += ROW_HEIGHT;
        drawAttribute(graphics, left, y, "Forca", PlayerAttribute.STRENGTH, strengthDescription());
        y += ROW_HEIGHT;
        drawAttribute(graphics, left, y, "Defesa", PlayerAttribute.DEFENSE, defenseDescription());
        y += ROW_HEIGHT;
        drawAttribute(graphics, left, y, "Agilidade", PlayerAttribute.AGILITY, agilityDescription());
        y += ROW_HEIGHT;
        drawAttribute(graphics, left, y, "Controle de Reliquias", PlayerAttribute.RELIC_CONTROL, relicControlDescription());

        drawMilestoneInfo(graphics, left, top + 158);
        drawTitleInfo(graphics, left, top + 204);

        graphics.drawCenteredString(font, "Pressione ESC para fechar", width / 2, top + PANEL_HEIGHT - 18, 0xFF8F7A65);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawAttribute(GuiGraphics graphics, int left, int y, String name, PlayerAttribute attribute, String description) {
        int value = ClientScalingData.getAttribute(attribute);
        graphics.drawString(font, name + ": " + value, left + 18, y, 0xFFFFFFFF, false);
        graphics.drawString(font, description, left + 118, y, 0xFFFFB45C, false);
    }

    private String vitalityDescription() {
        int value = ClientScalingData.getAttribute(PlayerAttribute.VITALITY);
        return "+" + format(ScalingFormula.additionalHealth(value) / 2.0D) + " coracoes";
    }

    private String strengthDescription() {
        int value = ClientScalingData.getAttribute(PlayerAttribute.STRENGTH);
        return "+" + percent(ScalingFormula.physicalDamageMultiplier(value) - 1.0D) + " dano fisico";
    }

    private String defenseDescription() {
        int value = ClientScalingData.getAttribute(PlayerAttribute.DEFENSE);
        return percent(ScalingFormula.defenseReduction(value)) + " reducao";
    }

    private String agilityDescription() {
        int value = ClientScalingData.getAttribute(PlayerAttribute.AGILITY);
        return "+" + percent(ScalingFormula.movementMultiplier(value) - 1.0D) + " mov / +" +
                percent(ScalingFormula.attackSpeedMultiplier(value) - 1.0D) + " atk";
    }

    private String relicControlDescription() {
        int value = ClientScalingData.getAttribute(PlayerAttribute.RELIC_CONTROL);
        return percent(ScalingFormula.relicPenaltyReduction(value)) + " custo menor";
    }

    private void drawMilestoneInfo(GuiGraphics graphics, int left, int y) {
        if (ClientSpecializationData.pending().isEmpty()) {
            graphics.drawString(font, "Marcos: nenhuma escolha pendente.", left + 18, y, 0xFF8F7A65, false);
            return;
        }

        String key = ClientSpecializationData.pending().keySet().iterator().next();
        graphics.drawString(font, "Marco disponivel: " + key + " - escolha permanente", left + 18, y, 0xFFFFD36B, false);
        String[] parts = key.split("\\.");
        if (parts.length == 2) {
            PlayerAttribute attribute = attributeById(parts[0]);
            int milestone = parseInt(parts[1]);
            if (attribute != null) {
                int lineY = y + 34;
                for (PassiveDefinition option : PassiveRegistry.options(attribute, milestone)) {
                    graphics.drawString(font, option.description(), left + 18, lineY, 0xFFE6D4B8, false);
                    lineY += 12;
                }
            }
        }
    }

    private void drawTitleInfo(GuiGraphics graphics, int left, int y) {
        graphics.drawString(font, "Titulos desbloqueados: " + ClientSpecializationData.titles().size(), left + 18, y, 0xFFE6D4B8, false);
        graphics.drawString(font, "Clique em um titulo para equipar.", left + 18, y + 12, 0xFF8F7A65, false);
    }

    private String equippedTitleText() {
        String titleId = ClientSpecializationData.equippedTitle();
        if (titleId == null || titleId.isBlank()) {
            return "nenhum";
        }
        return TitleRegistry.byId(titleId).map(t -> t.displayName()).orElse(titleId);
    }

    private String percent(double value) {
        return format(value * 100.0D) + "%";
    }

    private String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private PlayerAttribute attributeById(String id) {
        for (PlayerAttribute attribute : PlayerAttribute.values()) {
            if (attribute.id().equals(id)) {
                return attribute;
            }
        }
        return null;
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private String shortText(String text, int max) {
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max - 1) + "...";
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
