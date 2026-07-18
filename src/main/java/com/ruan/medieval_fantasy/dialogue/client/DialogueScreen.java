package com.ruan.medieval_fantasy.dialogue.client;

import com.ruan.medieval_fantasy.dialogue.DialogueNode;
import com.ruan.medieval_fantasy.dialogue.DialogueOption;
import com.ruan.medieval_fantasy.dialogue.DialogueTree;
import com.ruan.medieval_fantasy.dialogue.network.DialogueNetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

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

        lockCameraOnSpeaker();
        tickCounter++;
        if (tickCounter % 2 == 0 && visibleCharacters < node.getText().length()) {
            visibleCharacters++;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        DialogueNode node = currentNode();
        if (node == null) {
            return;
        }

        String visibleText = node.getText().substring(0, Math.min(visibleCharacters, node.getText().length()));
        DialogueRenderer.renderBox(graphics, font, node, visibleText, mouseX, mouseY, width, height,
                isTextComplete(node), visibleOptions(node).stream().map(OptionSlot::option).toList());

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

        List<OptionSlot> options = visibleOptions(node);
        List<DialogueOption> visibleDialogueOptions = options.stream().map(OptionSlot::option).toList();
        int clickedOption = DialogueRenderer.getClickedOption(font, node.getText(), visibleDialogueOptions,
                (int) mouseX, (int) mouseY, width, height);
        if (options.isEmpty()) {
            if (clickedOption == -1) {
                DialogueNetworkHandler.choose(speakerEntityId, tree.getId(), nodeId, -1);
            }
            return true;
        }

        if (clickedOption >= 0 && clickedOption < options.size()) {
            DialogueNetworkHandler.choose(speakerEntityId, tree.getId(), nodeId, options.get(clickedOption).originalIndex());
            return true;
        }

        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode >= 49 && keyCode <= 57) {
            DialogueNode node = currentNode();
            int index = keyCode - 49;
            List<OptionSlot> options = node == null ? List.of() : visibleOptions(node);
            if (node != null && isTextComplete(node) && index < options.size()) {
                DialogueNetworkHandler.choose(speakerEntityId, tree.getId(), nodeId, options.get(index).originalIndex());
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

    private List<OptionSlot> visibleOptions(DialogueNode node) {
        List<OptionSlot> visible = new java.util.ArrayList<>();
        List<DialogueOption> options = node.getOptions();
        for (int i = 0; i < options.size(); i++) {
            DialogueOption option = options.get(i);
            boolean available = option.getConditions().stream().allMatch(DialogueClientCondition::test);
            if (available) {
                visible.add(new OptionSlot(i, option));
            }
        }
        return visible;
    }

    private void lockCameraOnSpeaker() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) {
            return;
        }

        Entity speaker = minecraft.level.getEntity(speakerEntityId);
        if (speaker == null) {
            return;
        }

        Vec3 from = player.getEyePosition();
        Vec3 to = speaker.getEyePosition().add(0.0D, -0.15D, 0.0D);
        Vec3 direction = to.subtract(from);
        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);

        float targetYaw = (float) (Mth.atan2(direction.z, direction.x) * (180.0F / Math.PI)) - 90.0F;
        float targetPitch = (float) (-(Mth.atan2(direction.y, horizontalDistance) * (180.0F / Math.PI)));

        float yaw = Mth.rotLerp(0.28F, player.getYRot(), targetYaw);
        float pitch = Mth.lerp(0.28F, player.getXRot(), Mth.clamp(targetPitch, -75.0F, 75.0F));

        player.setYRot(yaw);
        player.setXRot(pitch);
        player.setYHeadRot(yaw);
        player.yBodyRot = yaw;
    }

    private record OptionSlot(int originalIndex, DialogueOption option) {
    }
}
