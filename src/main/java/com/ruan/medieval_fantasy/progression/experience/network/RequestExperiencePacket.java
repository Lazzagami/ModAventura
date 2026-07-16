package com.ruan.medieval_fantasy.progression.experience.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RequestExperiencePacket {

    public static void encode(RequestExperiencePacket packet, FriendlyByteBuf buffer) {
    }

    public static RequestExperiencePacket decode(FriendlyByteBuf buffer) {
        return new RequestExperiencePacket();
    }

    public static void handle(RequestExperiencePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ExperienceNetworkHandler.sync(player);
            }
        });
        context.setPacketHandled(true);
    }
}
