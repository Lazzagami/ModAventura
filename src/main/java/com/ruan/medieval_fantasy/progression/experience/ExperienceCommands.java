package com.ruan.medieval_fantasy.progression.experience;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.scaling.PlayerScalingData;
import com.ruan.medieval_fantasy.scaling.network.ScalingNetworkHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class ExperienceCommands {

    private ExperienceCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("medievalxp")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("get")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> get(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador")))))
                .then(Commands.literal("add")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .then(Commands.argument("quantidade", IntegerArgumentType.integer(1))
                                        .executes(ctx -> add(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador"),
                                                IntegerArgumentType.getInteger(ctx, "quantidade"))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .then(Commands.argument("quantidade", IntegerArgumentType.integer(1))
                                        .executes(ctx -> remove(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador"),
                                                IntegerArgumentType.getInteger(ctx, "quantidade"))))))
                .then(Commands.literal("set")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .then(Commands.argument("quantidade", IntegerArgumentType.integer(0))
                                        .executes(ctx -> set(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador"),
                                                IntegerArgumentType.getInteger(ctx, "quantidade"))))))
                .then(Commands.literal("setlevel")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .then(Commands.argument("nivel", IntegerArgumentType.integer(ExperienceConfig.INITIAL_LEVEL))
                                        .executes(ctx -> setLevel(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador"),
                                                IntegerArgumentType.getInteger(ctx, "nivel"))))))
                .then(Commands.literal("levelup")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .then(Commands.argument("quantidade", IntegerArgumentType.integer(1))
                                        .executes(ctx -> levelUp(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador"),
                                                IntegerArgumentType.getInteger(ctx, "quantidade"))))))
                .then(Commands.literal("reset")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> reset(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador")))))
                .then(Commands.literal("simulatekill")
                        .then(Commands.argument("tipo", StringArgumentType.word())
                                .executes(ctx -> simulateKill(ctx.getSource(), StringArgumentType.getString(ctx, "tipo"))))));
    }

    private static int get(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.literal("XP Medieval de " + player.getName().getString()), false);
        source.sendSuccess(() -> Component.literal("Nivel: " + PlayerExperienceData.getLevel(player)), false);
        source.sendSuccess(() -> Component.literal("XP atual: " + PlayerExperienceData.getCurrentXp(player)), false);
        source.sendSuccess(() -> Component.literal("XP necessario: " + PlayerExperienceManager.getRequiredXp(player)), false);
        source.sendSuccess(() -> Component.literal("XP total: " + PlayerExperienceData.getTotalXp(player)), false);
        source.sendSuccess(() -> Component.literal("Pontos disponiveis: " + PlayerScalingData.getAvailablePoints(player)), false);
        return 1;
    }

    private static int add(CommandSourceStack source, ServerPlayer player, int amount) {
        PlayerExperienceManager.addXp(player, amount, ExperienceSource.COMMAND);
        source.sendSuccess(() -> Component.literal("Adicionado " + amount + " XP."), true);
        return 1;
    }

    private static int remove(CommandSourceStack source, ServerPlayer player, int amount) {
        PlayerExperienceManager.removeXp(player, amount);
        source.sendSuccess(() -> Component.literal("Removido " + amount + " XP atual."), true);
        return 1;
    }

    private static int set(CommandSourceStack source, ServerPlayer player, int amount) {
        PlayerExperienceManager.setXp(player, amount);
        source.sendSuccess(() -> Component.literal("XP atual definido para " + amount + "."), true);
        return 1;
    }

    private static int setLevel(CommandSourceStack source, ServerPlayer player, int level) {
        PlayerExperienceManager.setLevel(player, level);
        source.sendSuccess(() -> Component.literal("Nivel definido para " + PlayerExperienceData.getLevel(player) + "."), true);
        return 1;
    }

    private static int levelUp(CommandSourceStack source, ServerPlayer player, int amount) {
        int target = Math.min(ExperienceConfig.MAX_LEVEL, PlayerExperienceData.getLevel(player) + amount);
        int gained = target - PlayerExperienceData.getLevel(player);
        PlayerExperienceManager.setLevel(player, target);
        PlayerScalingData.addAvailablePoints(player, gained * ExperienceConfig.ATTRIBUTE_POINTS_PER_LEVEL);
        PlayerExperienceManager.sync(player);
        ScalingNetworkHandler.sync(player);
        source.sendSuccess(() -> Component.literal("Nivel aumentado em " + gained + "."), true);
        return 1;
    }

    private static int reset(CommandSourceStack source, ServerPlayer player) {
        PlayerExperienceData.reset(player);
        PlayerScalingData.setAvailablePoints(player, 0);
        PlayerExperienceManager.sync(player);
        ScalingNetworkHandler.sync(player);
        source.sendSuccess(() -> Component.literal("XP medieval resetado."), true);
        return 1;
    }

    private static int simulateKill(CommandSourceStack source, String type) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Use esse comando como jogador."));
            return 0;
        }

        int xp = switch (type.toLowerCase(java.util.Locale.ROOT)) {
            case "common" -> 10;
            case "elite" -> 75;
            case "miniboss" -> 250;
            case "boss" -> 1200;
            default -> 0;
        };

        if (xp <= 0) {
            source.sendFailure(Component.literal("Tipo invalido. Use common, elite, miniboss ou boss."));
            return 0;
        }

        PlayerExperienceManager.addXp(player, xp, ExperienceSource.EVENT);
        source.sendSuccess(() -> Component.literal("Simulado kill " + type + " = " + xp + " XP."), true);
        return 1;
    }
}
