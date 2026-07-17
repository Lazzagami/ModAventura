package com.ruan.medieval_fantasy.origin.network;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.origin.OriginType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class OriginNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MODID, "origin"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId;

    private OriginNetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(packetId++, OpenOriginSelectionPacket.class,
                OpenOriginSelectionPacket::encode,
                OpenOriginSelectionPacket::decode,
                OpenOriginSelectionPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(packetId++, SelectOriginPacket.class,
                SelectOriginPacket::encode,
                SelectOriginPacket::decode,
                SelectOriginPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(packetId++, SyncOriginPacket.class,
                SyncOriginPacket::encode,
                SyncOriginPacket::decode,
                SyncOriginPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void openSelection(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenOriginSelectionPacket());
    }

    public static void select(OriginType origin) {
        CHANNEL.sendToServer(new SelectOriginPacket(origin.getId()));
    }

    public static void sync(ServerPlayer player, OriginType origin, boolean locked) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncOriginPacket(origin.getId(), locked));
    }
}
