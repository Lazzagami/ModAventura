package com.ruan.medieval_fantasy.worldgen.structure;

import com.mojang.brigadier.CommandDispatcher;
import com.ruan.medieval_fantasy.ExampleMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class EldrathCastleCommands {

    private EldrathCastleCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("eldrathcastle")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("build")
                        .executes(ctx -> buildInFrontOfPlayer(ctx.getSource())))
                .then(Commands.literal("build_here")
                        .executes(ctx -> buildHere(ctx.getSource())))
                .then(Commands.literal("tp")
                        .executes(ctx -> teleportToEntrance(ctx.getSource())))
                .then(Commands.literal("where")
                        .executes(ctx -> showLocation(ctx.getSource()))));
    }

    private static int buildInFrontOfPlayer(CommandSourceStack source) {
        ServerPlayer player = getPlayer(source);
        if (player == null) {
            return 0;
        }

        ServerLevel level = player.serverLevel();
        EldrathCastleBuilder builder = new EldrathCastleBuilder(level, player.blockPosition().offset(0, -1, 96));
        EldrathCastleBuilder.BuildResult result = builder.build(true);
        EldrathCastleData.get(level).setCastle(result.center(), result.entrance(), result.bossPosition());
        teleport(player, level, result.entrance());

        source.sendSuccess(() -> Component.literal("Castelo em Ruinas de Eldrath construido. Voce foi levado para a entrada."), true);
        source.sendSuccess(() -> Component.literal("Centro: " + format(result.center()) + " | Boss: " + format(result.bossPosition())), false);
        return 1;
    }

    private static int buildHere(CommandSourceStack source) {
        ServerPlayer player = getPlayer(source);
        if (player == null) {
            return 0;
        }

        ServerLevel level = player.serverLevel();
        EldrathCastleBuilder builder = new EldrathCastleBuilder(level, player.blockPosition().below());
        EldrathCastleBuilder.BuildResult result = builder.build(true);
        EldrathCastleData.get(level).setCastle(result.center(), result.entrance(), result.bossPosition());

        source.sendSuccess(() -> Component.literal("Castelo em Ruinas de Eldrath construido ao redor da posicao atual."), true);
        source.sendSuccess(() -> Component.literal("Use /eldrathcastle tp para ir para a entrada."), false);
        return 1;
    }

    private static int teleportToEntrance(CommandSourceStack source) {
        ServerPlayer player = getPlayer(source);
        if (player == null) {
            return 0;
        }

        ServerLevel level = player.serverLevel();
        EldrathCastleData data = EldrathCastleData.get(level);
        if (!data.isGenerated()) {
            source.sendFailure(Component.literal("Nenhum Castelo de Eldrath foi registrado ainda. Use /eldrathcastle build primeiro."));
            return 0;
        }

        teleport(player, level, data.getEntrance());
        source.sendSuccess(() -> Component.literal("Teleportado para a entrada de Eldrath."), false);
        return 1;
    }

    private static int showLocation(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        EldrathCastleData data = EldrathCastleData.get(level);
        if (!data.isGenerated()) {
            source.sendFailure(Component.literal("Nenhum Castelo de Eldrath foi registrado ainda."));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("Entrada: " + format(data.getEntrance())), false);
        source.sendSuccess(() -> Component.literal("Centro: " + format(data.getCenter())), false);
        source.sendSuccess(() -> Component.literal("Boss: " + format(data.getBossPosition())), false);
        return 1;
    }

    private static ServerPlayer getPlayer(CommandSourceStack source) {
        try {
            return source.getPlayerOrException();
        } catch (Exception exception) {
            source.sendFailure(Component.literal("Use esse comando dentro do jogo como jogador."));
            return null;
        }
    }

    private static void teleport(ServerPlayer player, ServerLevel level, net.minecraft.core.BlockPos pos) {
        player.teleportTo(level, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, player.getYRot(), player.getXRot());
    }

    private static String format(net.minecraft.core.BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }
}
