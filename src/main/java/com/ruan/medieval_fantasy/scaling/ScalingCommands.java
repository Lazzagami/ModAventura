package com.ruan.medieval_fantasy.scaling;

import com.mojang.brigadier.CommandDispatcher;
import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.progression.experience.network.ExperienceNetworkHandler;
import com.ruan.medieval_fantasy.progression.specialization.PassiveChoiceManager;
import com.ruan.medieval_fantasy.scaling.network.ScalingNetworkHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class ScalingCommands {

    private ScalingCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("medievalscaling")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("stats")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> sendStats(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador")))))
                .then(Commands.literal("damage")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> sendDamage(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador")))))
                .then(Commands.literal("defense")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> sendDefense(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador")))))
                .then(Commands.literal("recalculate")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> recalculate(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador")))))
                .then(Commands.literal("addpoints")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .then(Commands.argument("quantidade", IntegerArgumentType.integer(1))
                                        .executes(ctx -> addPoints(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "jogador"),
                                                IntegerArgumentType.getInteger(ctx, "quantidade"))))))
                .then(Commands.literal("bossinfo")
                        .executes(ctx -> bossInfo(ctx.getSource()))));
    }

    private static int sendStats(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.literal("Scaling de " + player.getName().getString()), false);
        source.sendSuccess(() -> Component.literal("Nivel: " + PlayerScalingData.getLevel(player)), false);
        source.sendSuccess(() -> Component.literal("Pontos disponiveis: " + PlayerScalingData.getAvailablePoints(player)), false);
        for (PlayerAttribute attribute : PlayerAttribute.values()) {
            source.sendSuccess(() -> Component.literal(attribute.id() + ": " + PlayerScalingData.getAttribute(player, attribute)), false);
        }

        source.sendSuccess(() -> Component.literal("Vida adicional: +" + PlayerScalingManager.getAdditionalHealth(player) / 2.0D + " coracoes"), false);
        source.sendSuccess(() -> Component.literal("Dano fisico: +" + percent(PlayerScalingManager.getPhysicalDamageMultiplier(player) - 1.0D)), false);
        source.sendSuccess(() -> Component.literal("Reducao defensiva: " + percent(PlayerScalingManager.getDefenseReduction(player))), false);
        source.sendSuccess(() -> Component.literal("Movimento: +" + percent(PlayerScalingManager.getMovementMultiplier(player) - 1.0D)), false);
        source.sendSuccess(() -> Component.literal("Velocidade de ataque: +" + percent(PlayerScalingManager.getAttackSpeedMultiplier(player) - 1.0D)), false);
        source.sendSuccess(() -> Component.literal("Controle de reliquias: " + percent(RelicScalingHandler.getPenaltyReduction(player))), false);
        return 1;
    }

    private static int sendDamage(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.literal("Multiplicador de dano fisico: x" + format(PlayerScalingManager.getPhysicalDamageMultiplier(player))), false);
        return 1;
    }

    private static int sendDefense(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.literal("Reducao por defesa: " + percent(PlayerScalingManager.getDefenseReduction(player))), false);
        source.sendSuccess(() -> Component.literal("Dano minimo final: " + ScalingConfig.MIN_FINAL_DAMAGE), false);
        return 1;
    }

    private static int recalculate(CommandSourceStack source, ServerPlayer player) {
        PlayerScalingManager.recalculate(player);
        PassiveChoiceManager.refreshUnlocks(player);
        source.sendSuccess(() -> Component.literal("Scaling recalculado para " + player.getName().getString()), true);
        return 1;
    }

    private static int addPoints(CommandSourceStack source, ServerPlayer player, int amount) {
        PlayerScalingData.addAvailablePoints(player, amount);
        ScalingNetworkHandler.sync(player);
        ExperienceNetworkHandler.sync(player);
        source.sendSuccess(() -> Component.literal("Adicionados " + amount + " pontos para " + player.getName().getString()), true);
        return 1;
    }

    private static int bossInfo(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Boss scaling: raio " + ScalingConfig.BOSS_PARTICIPATION_RADIUS
                + ", vida +1%/nivel medio ate +100%, dano +0.35%/nivel medio ate +35%."), false);
        source.sendSuccess(() -> Component.literal("Multiplayer vida: 1p x1.0, 2p x1.6, 3p x2.1, 4p+ x2.5. Dano +8% por jogador extra."), false);
        return 1;
    }

    private static String percent(double value) {
        return format(value * 100.0D) + "%";
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
