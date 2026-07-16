package com.ruan.medieval_fantasy.progression.specialization;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.scaling.PlayerAttribute;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class SpecializationCommands {

    private SpecializationCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        registerPassives(event.getDispatcher());
        registerTitles(event.getDispatcher());
    }

    private static void registerPassives(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("medievalpassive")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> listPassives(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador")))))
                .then(Commands.literal("choose")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .then(Commands.argument("atributo", StringArgumentType.word())
                                        .then(Commands.argument("marco", IntegerArgumentType.integer(1))
                                                .then(Commands.argument("opcao", IntegerArgumentType.integer(1, 2))
                                                        .executes(ctx -> choose(ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "jogador"),
                                                                StringArgumentType.getString(ctx, "atributo"),
                                                                IntegerArgumentType.getInteger(ctx, "marco"),
                                                                IntegerArgumentType.getInteger(ctx, "opcao"))))))))
                .then(Commands.literal("reset")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .then(Commands.argument("atributo", StringArgumentType.word())
                                        .then(Commands.argument("marco", IntegerArgumentType.integer(1))
                                                .executes(ctx -> reset(ctx.getSource(),
                                                        EntityArgument.getPlayer(ctx, "jogador"),
                                                        StringArgumentType.getString(ctx, "atributo"),
                                                        IntegerArgumentType.getInteger(ctx, "marco")))))))
                .then(Commands.literal("resetall")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> resetAll(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador"))))));
    }

    private static void registerTitles(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("medievaltitle")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .executes(ctx -> listTitles(ctx.getSource(), EntityArgument.getPlayer(ctx, "jogador")))))
                .then(Commands.literal("equip")
                        .then(Commands.argument("jogador", EntityArgument.player())
                                .then(Commands.argument("titulo", StringArgumentType.word())
                                        .executes(ctx -> equipTitle(ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "jogador"),
                                                StringArgumentType.getString(ctx, "titulo")))))));
    }

    private static int listPassives(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.literal("Passivas de " + player.getName().getString()), false);
        for (String key : PassiveChoiceData.pending(player).getAllKeys()) {
            source.sendSuccess(() -> Component.literal("Pendente: " + key), false);
        }
        for (String key : PassiveChoiceData.choices(player).getAllKeys()) {
            source.sendSuccess(() -> Component.literal("Escolhida: " + key + " = " + PassiveChoiceData.choices(player).getString(key)), false);
        }
        return 1;
    }

    private static int choose(CommandSourceStack source, ServerPlayer player, String attributeId, int milestone, int option) {
        PlayerAttribute attribute = parseAttribute(attributeId);
        if (attribute == null) {
            source.sendFailure(Component.literal("Atributo invalido: " + attributeId));
            return 0;
        }

        boolean success = PassiveChoiceManager.choose(player, attribute, milestone, option);
        source.sendSuccess(() -> Component.literal(success ? "Passiva escolhida." : "Nao foi possivel escolher essa passiva."), true);
        return success ? 1 : 0;
    }

    private static int reset(CommandSourceStack source, ServerPlayer player, String attributeId, int milestone) {
        PlayerAttribute attribute = parseAttribute(attributeId);
        if (attribute == null) {
            source.sendFailure(Component.literal("Atributo invalido: " + attributeId));
            return 0;
        }

        PassiveChoiceManager.resetChoice(player, attribute, milestone);
        source.sendSuccess(() -> Component.literal("Escolha resetada."), true);
        return 1;
    }

    private static int resetAll(CommandSourceStack source, ServerPlayer player) {
        PassiveChoiceManager.resetAllChoices(player);
        source.sendSuccess(() -> Component.literal("Todas as escolhas foram resetadas."), true);
        return 1;
    }

    private static int listTitles(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.literal("Titulo equipado: " + TitleManager.displayName(PassiveChoiceData.equippedTitle(player))), false);
        for (String title : PassiveChoiceData.unlockedTitles(player)) {
            source.sendSuccess(() -> Component.literal(title + " = " + TitleManager.displayName(title)), false);
        }
        return 1;
    }

    private static int equipTitle(CommandSourceStack source, ServerPlayer player, String titleId) {
        boolean success = TitleManager.equip(player, "none".equalsIgnoreCase(titleId) ? "" : titleId);
        source.sendSuccess(() -> Component.literal(success ? "Titulo atualizado." : "Titulo nao desbloqueado."), true);
        return success ? 1 : 0;
    }

    private static PlayerAttribute parseAttribute(String id) {
        for (PlayerAttribute attribute : PlayerAttribute.values()) {
            if (attribute.id().equalsIgnoreCase(id) || attribute.name().equalsIgnoreCase(id)) {
                return attribute;
            }
        }
        return null;
    }
}
