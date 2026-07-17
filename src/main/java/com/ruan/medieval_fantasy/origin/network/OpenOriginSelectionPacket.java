package com.ruan.medieval_fantasy.origin.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenOriginSelectionPacket {

    public static void encode(OpenOriginSelectionPacket packet, FriendlyByteBuf buffer) {
    }

    public static OpenOriginSelectionPacket decode(FriendlyByteBuf buffer) {
        return new OpenOriginSelectionPacket();
    }

    public static void handle(OpenOriginSelectionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.ruan.medieval_fantasy.origin.client.OriginClientHooks.openSelection()));
        context.setPacketHandled(true);
    }
}
