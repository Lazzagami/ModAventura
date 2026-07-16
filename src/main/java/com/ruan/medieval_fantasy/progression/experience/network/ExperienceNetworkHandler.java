package com.ruan.medieval_fantasy.progression.experience.network;

import com.ruan.medieval_fantasy.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ExperienceNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MODID, "experience"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId;

    private ExperienceNetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(packetId++, SyncExperiencePacket.class,
                SyncExperiencePacket::encode,
                SyncExperiencePacket::decode,
                SyncExperiencePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(packetId++, RequestExperiencePacket.class,
                RequestExperiencePacket::encode,
                RequestExperiencePacket::decode,
                RequestExperiencePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void sync(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), SyncExperiencePacket.from(player));
    }

    public static void requestSyncFromServer() {
        CHANNEL.sendToServer(new RequestExperiencePacket());
    }
}
