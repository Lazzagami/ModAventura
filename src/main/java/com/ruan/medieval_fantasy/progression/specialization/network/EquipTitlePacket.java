package com.ruan.medieval_fantasy.progression.specialization.network;

import com.ruan.medieval_fantasy.progression.specialization.TitleManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class EquipTitlePacket {

    private final String titleId;

    public EquipTitlePacket(String titleId) {
        this.titleId = titleId;
    }

    public static void encode(EquipTitlePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.titleId == null ? "" : packet.titleId);
    }

    public static EquipTitlePacket decode(FriendlyByteBuf buffer) {
        return new EquipTitlePacket(buffer.readUtf());
    }

    public static void handle(EquipTitlePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                TitleManager.equip(player, packet.titleId);
            }
        });
        context.setPacketHandled(true);
    }
}
