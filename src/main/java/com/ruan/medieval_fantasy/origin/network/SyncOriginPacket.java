package com.ruan.medieval_fantasy.origin.network;

import com.ruan.medieval_fantasy.origin.OriginType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncOriginPacket {

    private final String originId;
    private final boolean locked;

    public SyncOriginPacket(String originId, boolean locked) {
        this.originId = originId;
        this.locked = locked;
    }

    public static void encode(SyncOriginPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.originId);
        buffer.writeBoolean(packet.locked);
    }

    public static SyncOriginPacket decode(FriendlyByteBuf buffer) {
        return new SyncOriginPacket(buffer.readUtf(), buffer.readBoolean());
    }

    public static void handle(SyncOriginPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.ruan.medieval_fantasy.origin.client.ClientOriginData.set(OriginType.fromId(packet.originId), packet.locked)));
        context.setPacketHandled(true);
    }
}
