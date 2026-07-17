package com.ruan.medieval_fantasy.dialogue.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenDialoguePacket {

    private final String treeJson;
    private final String nodeId;
    private final int speakerEntityId;

    public OpenDialoguePacket(String treeJson, String nodeId, int speakerEntityId) {
        this.treeJson = treeJson;
        this.nodeId = nodeId;
        this.speakerEntityId = speakerEntityId;
    }

    public static void encode(OpenDialoguePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.treeJson, 262144);
        buffer.writeUtf(packet.nodeId);
        buffer.writeVarInt(packet.speakerEntityId);
    }

    public static OpenDialoguePacket decode(FriendlyByteBuf buffer) {
        return new OpenDialoguePacket(buffer.readUtf(262144), buffer.readUtf(), buffer.readVarInt());
    }

    public static void handle(OpenDialoguePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.ruan.medieval_fantasy.dialogue.client.DialogueClientHooks.open(packet.treeJson, packet.nodeId, packet.speakerEntityId)));
        context.setPacketHandled(true);
    }
}
