package com.ruan.medieval_fantasy.dialogue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ruan.medieval_fantasy.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DialogueRegistry {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, DialogueTree> CACHE = new HashMap<>();

    private DialogueRegistry() {
    }

    public static Optional<DialogueTree> get(MinecraftServer server, String dialogueId) {
        if (server == null || dialogueId == null || dialogueId.isBlank()) {
            return Optional.empty();
        }

        if (CACHE.containsKey(dialogueId)) {
            return Optional.ofNullable(CACHE.get(dialogueId));
        }

        ResourceLocation location = new ResourceLocation(ExampleMod.MODID, "dialogues/" + dialogueId + ".json");
        Optional<Resource> resource = server.getResourceManager().getResource(location);
        if (resource.isEmpty()) {
            return Optional.empty();
        }

        try (Reader reader = resource.get().openAsReader()) {
            DialogueTree tree = GSON.fromJson(reader, DialogueTree.class);
            CACHE.put(dialogueId, tree);
            return Optional.ofNullable(tree);
        } catch (Exception exception) {
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    public static String toJson(DialogueTree tree) {
        return GSON.toJson(tree);
    }

    public static DialogueTree fromJson(String json) {
        return GSON.fromJson(json, DialogueTree.class);
    }
}
