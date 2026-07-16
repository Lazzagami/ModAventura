package com.ruan.medieval_fantasy.scaling.network;

import com.ruan.medieval_fantasy.progression.specialization.PassiveChoiceManager;
import com.ruan.medieval_fantasy.progression.experience.network.ExperienceNetworkHandler;
import com.ruan.medieval_fantasy.scaling.PlayerAttribute;
import com.ruan.medieval_fantasy.scaling.PlayerScalingData;
import com.ruan.medieval_fantasy.scaling.PlayerScalingManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InvestAttributePacket {

    private final PlayerAttribute attribute;

    public InvestAttributePacket(PlayerAttribute attribute) {
        this.attribute = attribute;
    }

    public static void encode(InvestAttributePacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.attribute);
    }

    public static InvestAttributePacket decode(FriendlyByteBuf buffer) {
        return new InvestAttributePacket(buffer.readEnum(PlayerAttribute.class));
    }

    public static void handle(InvestAttributePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            if (PlayerScalingData.investPoint(player, packet.attribute)) {
                PlayerScalingManager.recalculate(player);
                PassiveChoiceManager.refreshUnlocks(player);
            }
            ScalingNetworkHandler.sync(player);
            ExperienceNetworkHandler.sync(player);
        });
        context.setPacketHandled(true);
    }
}
