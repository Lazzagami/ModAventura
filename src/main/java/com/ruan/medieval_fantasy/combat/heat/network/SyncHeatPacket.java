package com.ruan.medieval_fantasy.combat.heat.network;

import com.ruan.medieval_fantasy.combat.heat.client.ClientHeatData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncHeatPacket {

    private final int heat;

    public SyncHeatPacket(int heat) {
        this.heat = heat;
    }

    public static void encode(SyncHeatPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.heat);
    }

    public static SyncHeatPacket decode(FriendlyByteBuf buffer) {
        return new SyncHeatPacket(buffer.readVarInt());
    }

    public static void handle(SyncHeatPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientHeatData.setHeat(packet.heat));
        context.setPacketHandled(true);
    }
}
