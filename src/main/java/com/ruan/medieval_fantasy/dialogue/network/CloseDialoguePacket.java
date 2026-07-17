package com.ruan.medieval_fantasy.dialogue.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CloseDialoguePacket {

    public static void encode(CloseDialoguePacket packet, FriendlyByteBuf buffer) {
    }

    public static CloseDialoguePacket decode(FriendlyByteBuf buffer) {
        return new CloseDialoguePacket();
    }

    public static void handle(CloseDialoguePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.ruan.medieval_fantasy.dialogue.client.DialogueClientHooks.close()));
        context.setPacketHandled(true);
    }
}
