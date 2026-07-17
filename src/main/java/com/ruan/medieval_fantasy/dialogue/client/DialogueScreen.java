package com.ruan.medieval_fantasy.dialogue.client;

import com.ruan.medieval_fantasy.dialogue.DialogueNode;
import com.ruan.medieval_fantasy.dialogue.DialogueOption;
import com.ruan.medieval_fantasy.dialogue.DialogueTree;
import com.ruan.medieval_fantasy.dialogue.network.DialogueNetworkHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class DialogueScreen extends Screen {

    private final DialogueTree tree;
    private final int speakerEntityId;
    private String nodeId;
    private int visibleCharacters;
    private int tickCounter;

    public DialogueScreen(DialogueTree tree, String nodeId, int speakerEntityId) {
        super(Component.literal("Dialogue"));
        this.tree = tree;
        this.nodeId = nodeId;
        this.speakerEntityId = speakerEntityId;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void tick() {
        DialogueNode node = currentNode();
        if (node == null) {
            onClose();
            return;
        }

        tickCounter++;
        if (tickCounter % 2 == 0 && visibleCharacters < node.getText().length()) {
            visibleCharacters++;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        DialogueNode node = currentNode();
        if (node == null) {
            return;
        }

        String visibleText = node.getText().substring(0, Math.min(visibleCharacters, node.getText().length()));
        DialogueRenderer.renderBox(graphics, font, node, visibleText, mouseX, mouseY, width, height, isTextComplete(node));

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        DialogueNode node = currentNode();
        if (node == null) {
            return true;
        }

        if (!isTextComplete(node)) {
            visibleCharacters = node.getText().length();
            return true;
        }

        List<DialogueOption> options = node.getOptions();
        if (options.isEmpty()) {
            DialogueNetworkHandler.choose(speakerEntityId, tree.getId(), nodeId, -1);
            return true;
        }

        int boxWidth = Math.min(width - 48, 430);
        int x = (width - boxWidth) / 2;
        int y = height - 118 - 28;
        int startY = y + 76;

        for (int i = 0; i < options.size(); i++) {
            int optionY = startY + i * 17;
            if (DialogueRenderer.isInside((int) mouseX, (int) mouseY, x + 12, optionY - 3, boxWidth - 24, 15)) {
                DialogueNetworkHandler.choose(speakerEntityId, tree.getId(), nodeId, i);
                return true;
            }
        }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode >= 49 && keyCode <= 57) {
            DialogueNode node = currentNode();
            int index = keyCode - 49;
            if (node != null && isTextComplete(node) && index < node.getOptions().size()) {
                DialogueNetworkHandler.choose(speakerEntityId, tree.getId(), nodeId, index);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private DialogueNode currentNode() {
        return tree.getNode(nodeId);
    }

    private boolean isTextComplete(DialogueNode node) {
        return visibleCharacters >= node.getText().length();
    }

}
