package com.ruan.medieval_fantasy.dialogue.client;

import com.ruan.medieval_fantasy.dialogue.DialogueRegistry;
import com.ruan.medieval_fantasy.dialogue.DialogueTree;
import net.minecraft.client.Minecraft;

public final class DialogueClientHooks {

    private DialogueClientHooks() {
    }

    public static void open(String treeJson, String nodeId, int speakerEntityId) {
        DialogueTree tree = DialogueRegistry.fromJson(treeJson);
        Minecraft.getInstance().setScreen(new DialogueScreen(tree, nodeId, speakerEntityId));
    }

    public static void close() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof DialogueScreen) {
            minecraft.setScreen(null);
        }
    }
}
