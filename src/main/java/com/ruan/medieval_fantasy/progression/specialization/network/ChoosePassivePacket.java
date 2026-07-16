package com.ruan.medieval_fantasy.progression.specialization.network;

import com.ruan.medieval_fantasy.progression.specialization.PassiveChoiceManager;
import com.ruan.medieval_fantasy.scaling.PlayerAttribute;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChoosePassivePacket {

    private final PlayerAttribute attribute;
    private final int milestone;
    private final int option;

    public ChoosePassivePacket(PlayerAttribute attribute, int milestone, int option) {
        this.attribute = attribute;
        this.milestone = milestone;
        this.option = option;
    }

    public static void encode(ChoosePassivePacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.attribute);
        buffer.writeVarInt(packet.milestone);
        buffer.writeVarInt(packet.option);
    }

    public static ChoosePassivePacket decode(FriendlyByteBuf buffer) {
        return new ChoosePassivePacket(buffer.readEnum(PlayerAttribute.class), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(ChoosePassivePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PassiveChoiceManager.choose(player, packet.attribute, packet.milestone, packet.option);
                SpecializationNetworkHandler.sync(player);
            }
        });
        context.setPacketHandled(true);
    }
}
