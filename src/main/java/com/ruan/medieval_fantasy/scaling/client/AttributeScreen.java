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
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttributeScreen extends Screen {

    private static final int PANEL_WIDTH = 540;
    private static final int PANEL_HEIGHT = 348;
    private static final int PADDING = 18;
    private static final int TAB_Y = 30;
    private static final int CONTENT_TOP = 62;
    private static final int FOOTER_HEIGHT = 20;
    private static final int ATTRIBUTE_ROW_HEIGHT = 38;
    private static final int MILESTONE_CARD_HEIGHT = 112;
    private static final int TITLE_SECTION_HEIGHT = 82;
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
        int left = left();
        int top = top();

        addTabButton(left + PADDING, top + TAB_Y, Tab.ATTRIBUTES, "Atributos");
        addTabButton(left + PADDING + 102, top + TAB_Y, Tab.MILESTONES, pendingTabText());

        if (activeTab == Tab.ATTRIBUTES) {
            addAttributeButtons(left, top);
        } else {
            addMilestoneChoiceButtons(left, top);
            addTitleButtons(left, top);
        }
    }

    private int left() {
        return (width - PANEL_WIDTH) / 2;
    }

    private int top() {
        return (height - PANEL_HEIGHT) / 2;
    }

    private void addTabButton(int x, int y, Tab tab, String text) {
        addRenderableWidget(Button.builder(Component.literal(text), button -> switchTab(tab))
                .bounds(x, y, 94, 20)
                .build());
    }

    private void switchTab(Tab tab) {
        activeTab = tab;
        clearWidgets();
        init();
    }

    private void addAttributeButtons(int left, int top) {
        int y = top + CONTENT_TOP + 28;
        for (PlayerAttribute attribute : List.of(
                PlayerAttribute.VITALITY,
                PlayerAttribute.STRENGTH,
                PlayerAttribute.DEFENSE,
                PlayerAttribute.AGILITY,
                PlayerAttribute.RELIC_CONTROL
        )) {
            addRenderableWidget(Button.builder(Component.literal("+"), button -> {
                ScalingNetworkHandler.investAttribute(attribute);
                ScalingNetworkHandler.requestSyncFromServer();
            }).bounds(left + PANEL_WIDTH - PADDING - 26, y + 7, 22, 20).build());
            y += ATTRIBUTE_ROW_HEIGHT;
        }
    }

    private void addMilestoneChoiceButtons(int left, int top) {
        List<PendingMilestone> pending = pendingMilestones();
        int titleY = titleSectionY(top);
        int y = top + CONTENT_TOP;
        int rendered = 0;

        for (PendingMilestone milestone : pending) {
            if (rendered >= 2 || y + MILESTONE_CARD_HEIGHT > titleY - 8) {
                break;
            }

            List<PassiveDefinition> options = PassiveRegistry.options(milestone.attribute(), milestone.milestone());
            int optionAreaTop = y + 24;
            int optionCardWidth = (PANEL_WIDTH - PADDING * 2 - 10) / 2;

            for (int i = 0; i < Math.min(2, options.size()); i++) {
                PassiveDefinition option = options.get(i);
                int currentOption = option.option();
                int optionX = left + PADDING + i * (optionCardWidth + 10);
                int buttonY = optionAreaTop + 60;

                addRenderableWidget(Button.builder(Component.literal(shortText(option.displayName(), 24)), button -> {
                    SpecializationNetworkHandler.choosePassive(milestone.attribute(), milestone.milestone(), currentOption);
                    SpecializationNetworkHandler.requestSyncFromServer();
                    switchTab(Tab.MILESTONES);
                }).bounds(optionX + 8, buttonY, optionCardWidth - 16, 20).build());
            }

            y += MILESTONE_CARD_HEIGHT + 8;
            rendered++;
        }
    }

    private void addTitleButtons(int left, int top) {
        int y = titleSectionY(top) + 44;
        int x = left + PADDING;
        int buttonWidth = 142;
        int added = 0;

        for (String titleId : ClientSpecializationData.titles()) {
            String title = TitleRegistry.byId(titleId).map(t -> t.displayName()).orElse(titleId);
            addRenderableWidget(Button.builder(Component.literal(shortText(title, 22)), button -> {
                SpecializationNetworkHandler.equipTitle(titleId);
                SpecializationNetworkHandler.requestSyncFromServer();
            }).bounds(x + added * (buttonWidth + 8), y, buttonWidth, 20).build());

            added++;
            if (added >= 3) {
                break;
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);

        int left = left();
        int top = top();
        renderPanel(graphics, left, top);

        graphics.drawCenteredString(font, title, width / 2, top + 8, 0xFFD08A3A);
        graphics.drawString(font, "Nivel " + ClientExperienceData.level(), left + PANEL_WIDTH - 162, top + 34, 0xFFE6D4B8, false);
        graphics.drawString(font, "Pontos " + ClientScalingData.getAvailablePoints(), left + PANEL_WIDTH - 90, top + 34, 0xFFFFC86B, false);

        if (activeTab == Tab.ATTRIBUTES) {
            renderAttributesTab(graphics, left, top);
        } else {
            renderMilestonesTab(graphics, left, top);
        }

        graphics.drawCenteredString(font, "ESC para fechar", width / 2, top + PANEL_HEIGHT - 14, 0xFF8F7A65);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderPanel(GuiGraphics graphics, int left, int top) {
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xE01A0F0A);
        graphics.fill(left + 2, top + 2, left + PANEL_WIDTH - 2, top + PANEL_HEIGHT - 2, 0xB02B1810);
        graphics.fill(left + PADDING, top + 56, left + PANEL_WIDTH - PADDING, top + 57, 0x664A2B1A);
        graphics.fill(left + PADDING, top + PANEL_HEIGHT - FOOTER_HEIGHT - 3, left + PANEL_WIDTH - PADDING, top + PANEL_HEIGHT - FOOTER_HEIGHT - 2, 0x664A2B1A);
    }

    private void renderAttributesTab(GuiGraphics graphics, int left, int top) {
        drawClippedString(graphics, "Titulo: " + equippedTitleText(), left + PADDING, top + 34, PANEL_WIDTH - 210, 0xFFD08A3A);

        int y = top + CONTENT_TOP + 24;
        drawAttributeCard(graphics, left, y, "Vitalidade", PlayerAttribute.VITALITY, vitalityDescription(), "Sobrevivencia e vida maxima.");
        y += ATTRIBUTE_ROW_HEIGHT;
        drawAttributeCard(graphics, left, y, "Forca", PlayerAttribute.STRENGTH, strengthDescription(), "Aumenta somente dano fisico direto.");
        y += ATTRIBUTE_ROW_HEIGHT;
        drawAttributeCard(graphics, left, y, "Defesa", PlayerAttribute.DEFENSE, defenseDescription(), "Reducao com retorno decrescente.");
        y += ATTRIBUTE_ROW_HEIGHT;
        drawAttributeCard(graphics, left, y, "Agilidade", PlayerAttribute.AGILITY, agilityDescription(), "Movimento e velocidade de ataque.");
        y += ATTRIBUTE_ROW_HEIGHT;
        drawAttributeCard(graphics, left, y, "Controle de Reliquias", PlayerAttribute.RELIC_CONTROL, relicControlDescription(), "Atrasa e suaviza custos de reliquias.");

        int pending = pendingMilestones().size();
        int infoColor = pending > 0 ? 0xFFFFD36B : 0xFF8F7A65;
        String info = pending > 0 ? "Marcos pendentes: " + pending + ". Abra a aba Marcos para escolher." : "Nenhum marco pendente.";
        drawClippedString(graphics, info, left + PADDING, top + PANEL_HEIGHT - 44, PANEL_WIDTH - PADDING * 2, infoColor);
    }

    private void drawAttributeCard(GuiGraphics graphics, int left, int y, String name, PlayerAttribute attribute,
                                   String result, String description) {
        int x = left + PADDING;
        int cardWidth = PANEL_WIDTH - PADDING * 2;
        graphics.fill(x, y, x + cardWidth, y + 32, 0x4420100A);
        graphics.fill(x, y, x + 3, y + 32, 0xAA9A5A24);

        int value = ClientScalingData.getAttribute(attribute);
        drawClippedString(graphics, name + ": " + value, x + 10, y + 6, 142, 0xFFFFFFFF);
        drawClippedString(graphics, result, x + 156, y + 6, 150, 0xFFFFB45C);
        drawClippedString(graphics, description, x + 156, y + 18, cardWidth - 214, 0xFF8F7A65);
    }

    private void renderMilestonesTab(GuiGraphics graphics, int left, int top) {
        List<PendingMilestone> pending = pendingMilestones();
        int titleY = titleSectionY(top);

        if (pending.isEmpty()) {
            graphics.drawString(font, "Marcos", left + PADDING, top + CONTENT_TOP, 0xFFFFD36B, false);
            drawWrapped(graphics,
                    "Nenhuma escolha pendente agora. Ao atingir 15, 30, 50, 75 ou 100 pontos em um atributo, novas escolhas aparecem aqui.",
                    left + PADDING,
                    top + CONTENT_TOP + 18,
                    PANEL_WIDTH - PADDING * 2,
                    0xFFE6D4B8,
                    6);
        } else {
            int y = top + CONTENT_TOP;
            int rendered = 0;
            for (PendingMilestone milestone : pending) {
                if (rendered >= 2 || y + MILESTONE_CARD_HEIGHT > titleY - 8) {
                    break;
                }

                drawPendingMilestoneCard(graphics, left, y, milestone);
                y += MILESTONE_CARD_HEIGHT + 8;
                rendered++;
            }

            if (pending.size() > rendered) {
                graphics.drawString(font, "+" + (pending.size() - rendered) + " marco(s) aguardando escolha.",
                        left + PADDING, titleY - 15, 0xFF8F7A65, false);
            }
        }

        drawTitleInfo(graphics, left, titleY);
    }

    private void drawPendingMilestoneCard(GuiGraphics graphics, int left, int y, PendingMilestone milestone) {
        int x = left + PADDING;
        int cardWidth = PANEL_WIDTH - PADDING * 2;
        graphics.fill(x, y, x + cardWidth, y + MILESTONE_CARD_HEIGHT, 0x4420100A);
        graphics.fill(x, y, x + 3, y + MILESTONE_CARD_HEIGHT, 0xAA9A5A24);

        String title = displayAttribute(milestone.attribute()) + " " + milestone.milestone() + " - escolha permanente";
        graphics.drawString(font, title, x + 10, y + 8, 0xFFFFD36B, false);

        List<PassiveDefinition> options = PassiveRegistry.options(milestone.attribute(), milestone.milestone());
        int optionCardWidth = (cardWidth - 30) / 2;
        for (int i = 0; i < Math.min(2, options.size()); i++) {
            PassiveDefinition option = options.get(i);
            int optionX = x + 10 + i * (optionCardWidth + 10);
            int optionY = y + 28;
            drawPassiveOption(graphics, optionX, optionY, optionCardWidth, option);
        }
    }

    private void drawPassiveOption(GuiGraphics graphics, int x, int y, int width, PassiveDefinition option) {
        graphics.fill(x, y, x + width, y + 76, 0x552A1810);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, 0x665A361E);
        drawClippedString(graphics, option.displayName(), x + 8, y + 7, width - 16, 0xFFFFFFFF);
        drawWrapped(graphics, option.description(), x + 8, y + 21, width - 16, 0xFFE6D4B8, 3);
    }

    private int titleSectionY(int top) {
        return top + PANEL_HEIGHT - TITLE_SECTION_HEIGHT - FOOTER_HEIGHT;
    }

    private void drawTitleInfo(GuiGraphics graphics, int left, int y) {
        graphics.fill(left + PADDING, y - 8, left + PANEL_WIDTH - PADDING, y - 7, 0x664A2B1A);
        drawClippedString(graphics, "Titulo equipado: " + equippedTitleText(), left + PADDING, y, PANEL_WIDTH - PADDING * 2, 0xFFD08A3A);
        graphics.drawString(font, "Titulos desbloqueados: " + ClientSpecializationData.titles().size(), left + PADDING, y + 13, 0xFFE6D4B8, false);
        if (ClientSpecializationData.titles().isEmpty()) {
            graphics.drawString(font, "Escolhas de marcos desbloqueiam titulos.", left + PADDING, y + 27, 0xFF8F7A65, false);
        } else {
            graphics.drawString(font, "Escolha um titulo para equipar:", left + PADDING, y + 27, 0xFF8F7A65, false);
        }
    }

    private int drawWrapped(GuiGraphics graphics, String text, int x, int y, int width, int color, int maxLines) {
        List<FormattedCharSequence> lines = font.split(Component.literal(text), width);
        int linesToDraw = Math.min(maxLines, lines.size());
        for (int i = 0; i < linesToDraw; i++) {
            graphics.drawString(font, lines.get(i), x, y + i * 10, color, false);
        }
        if (lines.size() > maxLines) {
            graphics.drawString(font, "...", x + width - font.width("..."), y + (maxLines - 1) * 10, color, false);
        }
        return linesToDraw * 10;
    }

    private void drawClippedString(GuiGraphics graphics, String text, int x, int y, int width, int color) {
        graphics.drawString(font, font.plainSubstrByWidth(text, width), x, y, color, false);
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
