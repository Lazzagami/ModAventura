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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeScreen extends Screen {

    private static final int PANEL_WIDTH = 430;
    private static final int PANEL_HEIGHT = 276;
    private static final int ROW_HEIGHT = 24;
    private Tab activeTab;

    public AttributeScreen() {
        this(Tab.ATTRIBUTES);
    }

    private AttributeScreen(Tab activeTab) {
        super(Component.literal("Atributos"));
        this.activeTab = activeTab;
    }

    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;

        addTabButton(left + 18, top + 26, Tab.ATTRIBUTES, "Atributos");
        addTabButton(left + 112, top + 26, Tab.MILESTONES, pendingTabText());

        if (activeTab == Tab.ATTRIBUTES) {
            addAttributeButtons(left, top);
        } else {
            addMilestoneChoiceButtons(left, top);
            addTitleButtons(left, top);
        }
    }

    private void addTabButton(int x, int y, Tab tab, String text) {
        addRenderableWidget(Button.builder(Component.literal(text), button -> switchTab(tab))
                .bounds(x, y, tab == Tab.ATTRIBUTES ? 86 : 116, 20)
                .build());
    }

    private void switchTab(Tab tab) {
        activeTab = tab;
        clearWidgets();
        init();
    }

    private void addAttributeButtons(int left, int top) {
        int y = top + 78;
        addAttributeButton(left + PANEL_WIDTH - 44, y, PlayerAttribute.VITALITY);
        y += ROW_HEIGHT;
        addAttributeButton(left + PANEL_WIDTH - 44, y, PlayerAttribute.STRENGTH);
        y += ROW_HEIGHT;
        addAttributeButton(left + PANEL_WIDTH - 44, y, PlayerAttribute.DEFENSE);
        y += ROW_HEIGHT;
        addAttributeButton(left + PANEL_WIDTH - 44, y, PlayerAttribute.AGILITY);
        y += ROW_HEIGHT;
        addAttributeButton(left + PANEL_WIDTH - 44, y, PlayerAttribute.RELIC_CONTROL);
    }

    private void addAttributeButton(int x, int y, PlayerAttribute attribute) {
        addRenderableWidget(Button.builder(Component.literal("+"), button -> {
            ScalingNetworkHandler.investAttribute(attribute);
            ScalingNetworkHandler.requestSyncFromServer();
        }).bounds(x, y - 5, 22, 20).build());
    }

    private void addMilestoneChoiceButtons(int left, int top) {
        int y = top + 78;
        int rendered = 0;
        for (PendingMilestone pending : pendingMilestones()) {
            int buttonX = left + PANEL_WIDTH - 176;
            int buttonY = y + 18;
            int optionIndex = 0;
            for (PassiveDefinition option : PassiveRegistry.options(pending.attribute(), pending.milestone())) {
                int currentOption = option.option();
                addRenderableWidget(Button.builder(Component.literal(shortText(option.displayName(), 18)), button -> {
                    SpecializationNetworkHandler.choosePassive(pending.attribute(), pending.milestone(), currentOption);
                    SpecializationNetworkHandler.requestSyncFromServer();
                    switchTab(Tab.MILESTONES);
                }).bounds(buttonX, buttonY + optionIndex * 22, 156, 20).build());
                optionIndex++;
            }

            y += 76;
            rendered++;
            if (rendered >= 2) {
                break;
            }
        }
    }

    private void addTitleButtons(int left, int top) {
        int x = left + 24;
        int y = top + PANEL_HEIGHT - 56;
        int added = 0;
        for (String titleId : ClientSpecializationData.titles()) {
            String title = TitleRegistry.byId(titleId).map(t -> t.displayName()).orElse(titleId);
            addRenderableWidget(Button.builder(Component.literal(shortText(title, 16)), button -> {
                SpecializationNetworkHandler.equipTitle(titleId);
                SpecializationNetworkHandler.requestSyncFromServer();
            }).bounds(x + added * 106, y, 100, 20).build());
            added++;
            if (added >= 3) {
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

        graphics.drawCenteredString(font, title, width / 2, top + 8, 0xFFD08A3A);
        graphics.drawString(font, "Nivel: " + ClientExperienceData.level(), left + 250, top + 32, 0xFFE6D4B8, false);
        graphics.drawString(font, "Pontos: " + ClientScalingData.getAvailablePoints(), left + 320, top + 32, 0xFFFFC86B, false);

        if (activeTab == Tab.ATTRIBUTES) {
            renderAttributesTab(graphics, left, top);
        } else {
            renderMilestonesTab(graphics, left, top);
        }

        graphics.drawCenteredString(font, "ESC para fechar", width / 2, top + PANEL_HEIGHT - 14, 0xFF8F7A65);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderAttributesTab(GuiGraphics graphics, int left, int top) {
        graphics.drawString(font, "Titulo: " + equippedTitleText(), left + 18, top + 56, 0xFFD08A3A, false);

        int y = top + 78;
        drawAttribute(graphics, left, y, "Vitalidade", PlayerAttribute.VITALITY, vitalityDescription());
        y += ROW_HEIGHT;
        drawAttribute(graphics, left, y, "Forca", PlayerAttribute.STRENGTH, strengthDescription());
        y += ROW_HEIGHT;
        drawAttribute(graphics, left, y, "Defesa", PlayerAttribute.DEFENSE, defenseDescription());
        y += ROW_HEIGHT;
        drawAttribute(graphics, left, y, "Agilidade", PlayerAttribute.AGILITY, agilityDescription());
        y += ROW_HEIGHT;
        drawAttribute(graphics, left, y, "Controle de Reliquias", PlayerAttribute.RELIC_CONTROL, relicControlDescription());

        int pending = pendingMilestones().size();
        int infoColor = pending > 0 ? 0xFFFFD36B : 0xFF8F7A65;
        String info = pending > 0 ? "Marcos pendentes: " + pending + " - abra a aba Marcos." : "Nenhum marco pendente.";
        graphics.drawString(font, info, left + 18, top + 216, infoColor, false);
    }

    private void renderMilestonesTab(GuiGraphics graphics, int left, int top) {
        List<PendingMilestone> pending = pendingMilestones();
        if (pending.isEmpty()) {
            graphics.drawString(font, "Marcos", left + 18, top + 58, 0xFFFFD36B, false);
            drawWrapped(graphics, "Nenhuma escolha pendente agora. Ao atingir 15, 30, 50, 75 ou 100 pontos em um atributo, novas escolhas aparecem aqui.",
                    left + 18, top + 76, PANEL_WIDTH - 36, 0xFFE6D4B8);
        } else {
            int y = top + 56;
            int rendered = 0;
            for (PendingMilestone milestone : pending) {
                drawPendingMilestone(graphics, left, y, milestone);
                y += 76;
                rendered++;
                if (rendered >= 2) {
                    break;
                }
            }

            if (pending.size() > 2) {
                graphics.drawString(font, "+" + (pending.size() - 2) + " marco(s) aguardando escolha.", left + 18, top + 208, 0xFF8F7A65, false);
            }
        }

        drawTitleInfo(graphics, left, top + PANEL_HEIGHT - 78);
    }

    private void drawPendingMilestone(GuiGraphics graphics, int left, int y, PendingMilestone milestone) {
        String title = displayAttribute(milestone.attribute()) + " " + milestone.milestone() + " - escolha permanente";
        graphics.drawString(font, title, left + 18, y, 0xFFFFD36B, false);

        int textY = y + 15;
        for (PassiveDefinition option : PassiveRegistry.options(milestone.attribute(), milestone.milestone())) {
            String line = option.displayName() + ": " + option.description();
            int used = drawWrapped(graphics, line, left + 22, textY, PANEL_WIDTH - 218, 0xFFE6D4B8);
            textY += Math.max(11, used);
        }
    }

    private void drawAttribute(GuiGraphics graphics, int left, int y, String name, PlayerAttribute attribute, String description) {
        int value = ClientScalingData.getAttribute(attribute);
        graphics.drawString(font, name + ": " + value, left + 18, y, 0xFFFFFFFF, false);
        graphics.drawString(font, description, left + 168, y, 0xFFFFB45C, false);
    }

    private int drawWrapped(GuiGraphics graphics, String text, int x, int y, int width, int color) {
        List<net.minecraft.util.FormattedCharSequence> lines = font.split(Component.literal(text), width);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), x, y + i * 10, color, false);
        }
        return lines.size() * 10;
    }

    private List<PendingMilestone> pendingMilestones() {
        List<PendingMilestone> milestones = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : ClientSpecializationData.pending().entrySet()) {
            if (!entry.getValue()) {
                continue;
            }

            String[] parts = entry.getKey().split("\\.");
            if (parts.length != 2) {
                continue;
            }

            PlayerAttribute attribute = attributeById(parts[0]);
            int milestone = parseInt(parts[1]);
            if (attribute != null && milestone > 0 && !PassiveRegistry.options(attribute, milestone).isEmpty()) {
                milestones.add(new PendingMilestone(attribute, milestone));
            }
        }
        return milestones;
    }

    private void drawTitleInfo(GuiGraphics graphics, int left, int y) {
        graphics.drawString(font, "Titulo equipado: " + equippedTitleText(), left + 18, y, 0xFFD08A3A, false);
        graphics.drawString(font, "Titulos desbloqueados: " + ClientSpecializationData.titles().size(), left + 18, y + 12, 0xFFE6D4B8, false);
        if (!ClientSpecializationData.titles().isEmpty()) {
            graphics.drawString(font, "Clique em um titulo para equipar.", left + 18, y + 24, 0xFF8F7A65, false);
        }
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

    private String equippedTitleText() {
        String titleId = ClientSpecializationData.equippedTitle();
        if (titleId == null || titleId.isBlank()) {
            return "nenhum";
        }
        return TitleRegistry.byId(titleId).map(t -> t.displayName()).orElse(titleId);
    }

    private String pendingTabText() {
        int pending = pendingMilestones().size();
        return pending > 0 ? "Marcos (" + pending + ")" : "Marcos";
    }

    private String displayAttribute(PlayerAttribute attribute) {
        return switch (attribute) {
            case VITALITY -> "Vitalidade";
            case STRENGTH -> "Forca";
            case DEFENSE -> "Defesa";
            case AGILITY -> "Agilidade";
            case RELIC_CONTROL -> "Controle";
            default -> attribute.id();
        };
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

    private enum Tab {
        ATTRIBUTES,
        MILESTONES
    }

    private record PendingMilestone(PlayerAttribute attribute, int milestone) {
    }
}
