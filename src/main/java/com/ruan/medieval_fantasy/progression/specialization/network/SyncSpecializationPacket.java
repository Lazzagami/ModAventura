package com.ruan.medieval_fantasy.progression.specialization.network;

import com.ruan.medieval_fantasy.progression.specialization.PassiveChoiceData;
import com.ruan.medieval_fantasy.progression.specialization.client.ClientSpecializationData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SyncSpecializationPacket {

    private final Map<String, Boolean> pending;
    private final Map<String, String> choices;
    private final Set<String> titles;
    private final String equippedTitle;

    public SyncSpecializationPacket(Map<String, Boolean> pending, Map<String, String> choices, Set<String> titles, String equippedTitle) {
        this.pending = pending;
        this.choices = choices;
        this.titles = titles;
        this.equippedTitle = equippedTitle;
    }

    public static SyncSpecializationPacket from(ServerPlayer player) {
        Map<String, Boolean> pending = new LinkedHashMap<>();
        CompoundTag pendingTag = PassiveChoiceData.pending(player);
        for (String key : pendingTag.getAllKeys()) {
            pending.put(key, pendingTag.getBoolean(key));
        }

        Map<String, String> choices = new LinkedHashMap<>();
        CompoundTag choicesTag = PassiveChoiceData.choices(player);
        for (String key : choicesTag.getAllKeys()) {
            choices.put(key, choicesTag.getString(key));
        }

        return new SyncSpecializationPacket(pending, choices, PassiveChoiceData.unlockedTitles(player), PassiveChoiceData.equippedTitle(player));
    }

    public static void encode(SyncSpecializationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.pending.size());
        packet.pending.forEach((key, value) -> {
            buffer.writeUtf(key);
            buffer.writeBoolean(value);
        });

        buffer.writeVarInt(packet.choices.size());
        packet.choices.forEach((key, value) -> {
            buffer.writeUtf(key);
            buffer.writeUtf(value);
        });

        buffer.writeVarInt(packet.titles.size());
        for (String title : packet.titles) {
            buffer.writeUtf(title);
        }

        buffer.writeUtf(packet.equippedTitle == null ? "" : packet.equippedTitle);
    }

    public static SyncSpecializationPacket decode(FriendlyByteBuf buffer) {
        Map<String, Boolean> pending = new LinkedHashMap<>();
        int pendingSize = buffer.readVarInt();
        for (int i = 0; i < pendingSize; i++) {
            pending.put(buffer.readUtf(), buffer.readBoolean());
        }

        Map<String, String> choices = new LinkedHashMap<>();
        int choiceSize = buffer.readVarInt();
        for (int i = 0; i < choiceSize; i++) {
            choices.put(buffer.readUtf(), buffer.readUtf());
        }

        Set<String> titles = new LinkedHashSet<>();
        int titleSize = buffer.readVarInt();
        for (int i = 0; i < titleSize; i++) {
            titles.add(buffer.readUtf());
        }

        return new SyncSpecializationPacket(pending, choices, titles, buffer.readUtf());
    }

    public static void handle(SyncSpecializationPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> ClientSpecializationData.set(packet.pending, packet.choices, packet.titles, packet.equippedTitle));
        context.setPacketHandled(true);
    }
}
