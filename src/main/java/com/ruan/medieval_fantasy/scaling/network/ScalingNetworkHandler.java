package com.ruan.medieval_fantasy.scaling.network;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.scaling.PlayerAttribute;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ScalingNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MODID, "scaling"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId;

    private ScalingNetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(packetId++, SyncScalingDataPacket.class,
                SyncScalingDataPacket::encode,
                SyncScalingDataPacket::decode,
                SyncScalingDataPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(packetId++, RequestScalingDataPacket.class,
                RequestScalingDataPacket::encode,
                RequestScalingDataPacket::decode,
                RequestScalingDataPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        CHANNEL.registerMessage(packetId++, InvestAttributePacket.class,
                InvestAttributePacket::encode,
                InvestAttributePacket::decode,
                InvestAttributePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void sync(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), SyncScalingDataPacket.from(player));
    }

    public static void requestSyncFromServer() {
        CHANNEL.sendToServer(new RequestScalingDataPacket());
    }

    public static void investAttribute(PlayerAttribute attribute) {
        CHANNEL.sendToServer(new InvestAttributePacket(attribute));
    }
}
