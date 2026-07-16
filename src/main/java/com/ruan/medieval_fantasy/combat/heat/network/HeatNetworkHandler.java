package com.ruan.medieval_fantasy.combat.heat.network;

import com.ruan.medieval_fantasy.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class HeatNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MODID, "heat"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId;

    public static void register() {
        CHANNEL.registerMessage(packetId++, SyncHeatPacket.class,
                SyncHeatPacket::encode,
                SyncHeatPacket::decode,
                SyncHeatPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sync(ServerPlayer player, int heat) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncHeatPacket(heat));
    }
}
