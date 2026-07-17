package com.ruan.medieval_fantasy.dialogue.network;

import com.ruan.medieval_fantasy.dialogue.DialogueManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChooseDialogueOptionPacket {

    private final int speakerEntityId;
    private final String dialogueId;
    private final String nodeId;
    private final int optionIndex;

    public ChooseDialogueOptionPacket(int speakerEntityId, String dialogueId, String nodeId, int optionIndex) {
        this.speakerEntityId = speakerEntityId;
        this.dialogueId = dialogueId;
        this.nodeId = nodeId;
        this.optionIndex = optionIndex;
    }

    public static void encode(ChooseDialogueOptionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.speakerEntityId);
        buffer.writeUtf(packet.dialogueId);
        buffer.writeUtf(packet.nodeId);
        buffer.writeVarInt(packet.optionIndex);
    }

    public static ChooseDialogueOptionPacket decode(FriendlyByteBuf buffer) {
        return new ChooseDialogueOptionPacket(buffer.readVarInt(), buffer.readUtf(), buffer.readUtf(), buffer.readVarInt());
    }

    public static void handle(ChooseDialogueOptionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                DialogueManager.chooseOption(player, packet.speakerEntityId, packet.dialogueId, packet.nodeId, packet.optionIndex);
            }
        });
        context.setPacketHandled(true);
    }
}
