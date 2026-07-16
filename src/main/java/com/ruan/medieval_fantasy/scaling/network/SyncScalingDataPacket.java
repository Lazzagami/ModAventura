package com.ruan.medieval_fantasy.scaling.network;

import com.ruan.medieval_fantasy.progression.experience.PlayerExperienceData;
import com.ruan.medieval_fantasy.scaling.PlayerAttribute;
import com.ruan.medieval_fantasy.scaling.PlayerScalingData;
import com.ruan.medieval_fantasy.scaling.client.ClientScalingData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncScalingDataPacket {

    private final int level;
    private final int availablePoints;
    private final Map<PlayerAttribute, Integer> attributes;

    public SyncScalingDataPacket(int level, int availablePoints, Map<PlayerAttribute, Integer> attributes) {
        this.level = level;
        this.availablePoints = availablePoints;
        this.attributes = attributes;
    }

    public static SyncScalingDataPacket from(ServerPlayer player) {
        Map<PlayerAttribute, Integer> values = new EnumMap<>(PlayerAttribute.class);
        for (PlayerAttribute attribute : PlayerAttribute.values()) {
            values.put(attribute, PlayerScalingData.getAttribute(player, attribute));
        }

        return new SyncScalingDataPacket(
                PlayerExperienceData.getLevel(player),
                PlayerScalingData.getAvailablePoints(player),
                values
        );
    }

    public static void encode(SyncScalingDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.level);
        buffer.writeVarInt(packet.availablePoints);
        buffer.writeVarInt(PlayerAttribute.values().length);
        for (PlayerAttribute attribute : PlayerAttribute.values()) {
            buffer.writeEnum(attribute);
            buffer.writeVarInt(packet.attributes.getOrDefault(attribute, 0));
        }
    }

    public static SyncScalingDataPacket decode(FriendlyByteBuf buffer) {
        int level = buffer.readVarInt();
        int availablePoints = buffer.readVarInt();
        int size = buffer.readVarInt();
        Map<PlayerAttribute, Integer> values = new EnumMap<>(PlayerAttribute.class);
        for (int i = 0; i < size; i++) {
            values.put(buffer.readEnum(PlayerAttribute.class), buffer.readVarInt());
        }

        return new SyncScalingDataPacket(level, availablePoints, values);
    }

    public static void handle(SyncScalingDataPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientScalingData.set(packet.level, packet.availablePoints, packet.attributes));
        context.setPacketHandled(true);
    }
}
