package com.ruan.medieval_fantasy.progression.specialization.network;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.scaling.PlayerAttribute;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class SpecializationNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MODID, "specialization"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId;

    private SpecializationNetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(packetId++, SyncSpecializationPacket.class,
                SyncSpecializationPacket::encode,
                SyncSpecializationPacket::decode,
                SyncSpecializationPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(packetId++, ChoosePassivePacket.class,
                ChoosePassivePacket::encode,
                ChoosePassivePacket::decode,
                ChoosePassivePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(packetId++, EquipTitlePacket.class,
                EquipTitlePacket::encode,
                EquipTitlePacket::decode,
                EquipTitlePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(packetId++, RequestSpecializationPacket.class,
                RequestSpecializationPacket::encode,
                RequestSpecializationPacket::decode,
                RequestSpecializationPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void sync(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), SyncSpecializationPacket.from(player));
    }

    public static void requestSyncFromServer() {
        CHANNEL.sendToServer(new RequestSpecializationPacket());
    }

    public static void choosePassive(PlayerAttribute attribute, int milestone, int option) {
        CHANNEL.sendToServer(new ChoosePassivePacket(attribute, milestone, option));
    }

    public static void equipTitle(String titleId) {
        CHANNEL.sendToServer(new EquipTitlePacket(titleId));
    }
}
