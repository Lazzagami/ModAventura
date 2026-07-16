package com.ruan.medieval_fantasy.scaling.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestScalingDataPacket {

    public static void encode(RequestScalingDataPacket packet, FriendlyByteBuf buffer) {
    }

    public static RequestScalingDataPacket decode(FriendlyByteBuf buffer) {
        return new RequestScalingDataPacket();
    }

    public static void handle(RequestScalingDataPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ScalingNetworkHandler.sync(player);
            }
        });
        context.setPacketHandled(true);
    }
}
