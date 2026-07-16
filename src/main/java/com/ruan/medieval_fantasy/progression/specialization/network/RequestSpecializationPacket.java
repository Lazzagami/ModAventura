package com.ruan.medieval_fantasy.progression.specialization.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestSpecializationPacket {

    public static void encode(RequestSpecializationPacket packet, FriendlyByteBuf buffer) {
    }

    public static RequestSpecializationPacket decode(FriendlyByteBuf buffer) {
        return new RequestSpecializationPacket();
    }

    public static void handle(RequestSpecializationPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                SpecializationNetworkHandler.sync(player);
            }
        });
        context.setPacketHandled(true);
    }
}
