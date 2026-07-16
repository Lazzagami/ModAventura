package com.ruan.medieval_fantasy.progression.experience.network;

import com.ruan.medieval_fantasy.progression.experience.PlayerExperienceData;
import com.ruan.medieval_fantasy.progression.experience.PlayerExperienceManager;
import com.ruan.medieval_fantasy.progression.experience.client.ClientExperienceData;
import com.ruan.medieval_fantasy.scaling.PlayerScalingData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncExperiencePacket {

    private final int level;
    private final int currentXp;
    private final int requiredXp;
    private final long totalXp;
    private final int availablePoints;

    public SyncExperiencePacket(int level, int currentXp, int requiredXp, long totalXp, int availablePoints) {
        this.level = level;
        this.currentXp = currentXp;
        this.requiredXp = requiredXp;
        this.totalXp = totalXp;
        this.availablePoints = availablePoints;
    }

    public static SyncExperiencePacket from(ServerPlayer player) {
        return new SyncExperiencePacket(
                PlayerExperienceData.getLevel(player),
                PlayerExperienceData.getCurrentXp(player),
                PlayerExperienceManager.getRequiredXp(player),
                PlayerExperienceData.getTotalXp(player),
                PlayerScalingData.getAvailablePoints(player)
        );
    }

    public static void encode(SyncExperiencePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.level);
        buffer.writeVarInt(packet.currentXp);
        buffer.writeVarInt(packet.requiredXp);
        buffer.writeVarLong(packet.totalXp);
        buffer.writeVarInt(packet.availablePoints);
    }

    public static SyncExperiencePacket decode(FriendlyByteBuf buffer) {
        return new SyncExperiencePacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                buffer.readVarLong(), buffer.readVarInt());
    }

    public static void handle(SyncExperiencePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientExperienceData.set(packet.level, packet.currentXp, packet.requiredXp,
                packet.totalXp, packet.availablePoints));
        context.setPacketHandled(true);
    }
}
