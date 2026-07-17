package com.ruan.medieval_fantasy.origin.network;

import com.ruan.medieval_fantasy.origin.OriginManager;
import com.ruan.medieval_fantasy.origin.OriginType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SelectOriginPacket {

    private final String originId;

    public SelectOriginPacket(String originId) {
        this.originId = originId;
    }

    public static void encode(SelectOriginPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.originId);
    }

    public static SelectOriginPacket decode(FriendlyByteBuf buffer) {
        return new SelectOriginPacket(buffer.readUtf());
    }

    public static void handle(SelectOriginPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                OriginManager.selectOrigin(player, OriginType.fromId(packet.originId));
            }
        });
        context.setPacketHandled(true);
    }
}
