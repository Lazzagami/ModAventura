package com.ruan.medieval_fantasy.dialogue.network;

import com.ruan.medieval_fantasy.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class DialogueNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MODID, "dialogue"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId;

    private DialogueNetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(packetId++, OpenDialoguePacket.class,
                OpenDialoguePacket::encode,
                OpenDialoguePacket::decode,
                OpenDialoguePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(packetId++, ChooseDialogueOptionPacket.class,
                ChooseDialogueOptionPacket::encode,
                ChooseDialogueOptionPacket::decode,
                ChooseDialogueOptionPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(packetId++, CloseDialoguePacket.class,
                CloseDialoguePacket::encode,
                CloseDialoguePacket::decode,
                CloseDialoguePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void open(ServerPlayer player, String treeJson, String nodeId, int speakerEntityId) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenDialoguePacket(treeJson, nodeId, speakerEntityId));
    }

    public static void close(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CloseDialoguePacket());
    }

    public static void choose(int speakerEntityId, String dialogueId, String nodeId, int optionIndex) {
        CHANNEL.sendToServer(new ChooseDialogueOptionPacket(speakerEntityId, dialogueId, nodeId, optionIndex));
    }
}
