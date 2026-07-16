package com.ruan.medieval_fantasy;

import com.ruan.medieval_fantasy.scaling.ScalingConfig;
import com.ruan.medieval_fantasy.progression.experience.ExperienceConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("What you want the introduction message to be for the magic number")
            .define("magicNumberIntroduction", "The magic number is... ");

    // a list of strings that are treated as resource locations for items
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("A list of items to log on common setup.")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    private static final ForgeConfigSpec.DoubleValue SCALING_HEALTH_PER_VITALITY_PAIR = BUILDER
            .comment("Vida adicional a cada 2 pontos de Vitalidade.")
            .defineInRange("scaling.healthPerVitalityPair", 1.0D, 0.0D, 1000.0D);
    private static final ForgeConfigSpec.DoubleValue SCALING_STRENGTH_DAMAGE_PER_POINT = BUILDER
            .comment("Bonus multiplicativo de dano fisico por ponto de Forca.")
            .defineInRange("scaling.strengthPhysicalDamagePerPoint", 0.005D, 0.0D, 10.0D);
    private static final ForgeConfigSpec.DoubleValue SCALING_DEFENSE_CURVE = BUILDER
            .comment("Constante da curva de retorno decrescente da Defesa.")
            .defineInRange("scaling.defenseCurve", 150.0D, 1.0D, 100000.0D);
    private static final ForgeConfigSpec.DoubleValue SCALING_DEFENSE_MAX_REDUCTION = BUILDER
            .comment("Reducao maxima concedida apenas pela Defesa.")
            .defineInRange("scaling.defenseMaxReduction", 0.80D, 0.0D, 0.99D);
    private static final ForgeConfigSpec.DoubleValue SCALING_MIN_FINAL_DAMAGE = BUILDER
            .comment("Dano minimo final apos reducoes de scaling.")
            .defineInRange("scaling.minFinalDamage", 0.5D, 0.0D, 1000.0D);
    private static final ForgeConfigSpec.DoubleValue SCALING_AGILITY_MOVEMENT_PER_POINT = BUILDER
            .comment("Bonus multiplicativo de movimento por ponto de Agilidade.")
            .defineInRange("scaling.agilityMovementPerPoint", 0.0025D, 0.0D, 10.0D);
    private static final ForgeConfigSpec.DoubleValue SCALING_AGILITY_ATTACK_SPEED_PER_POINT = BUILDER
            .comment("Bonus multiplicativo de velocidade de ataque por ponto de Agilidade.")
            .defineInRange("scaling.agilityAttackSpeedPerPoint", 0.0015D, 0.0D, 10.0D);
    private static final ForgeConfigSpec.DoubleValue SCALING_RELIC_CONTROL_CURVE = BUILDER
            .comment("Constante da curva de Controle de Reliquias.")
            .defineInRange("scaling.relicControlCurve", 200.0D, 1.0D, 100000.0D);
    private static final ForgeConfigSpec.DoubleValue SCALING_RELIC_MAX_REDUCTION = BUILDER
            .comment("Reducao maxima dos custos de reliquias.")
            .defineInRange("scaling.relicMaxPenaltyReduction", 0.75D, 0.0D, 0.99D);
    private static final ForgeConfigSpec.DoubleValue SCALING_BOSS_PARTICIPATION_RADIUS = BUILDER
            .comment("Raio para considerar jogadores no scaling de boss.")
            .defineInRange("scaling.bossParticipationRadius", 64.0D, 1.0D, 1024.0D);
    private static final ForgeConfigSpec.DoubleValue SCALING_BOSS_HEALTH_PER_LEVEL = BUILDER
            .comment("Vida extra do boss por nivel medio do grupo.")
            .defineInRange("scaling.bossHealthPerAverageLevel", 0.01D, 0.0D, 10.0D);
    private static final ForgeConfigSpec.DoubleValue SCALING_BOSS_DAMAGE_PER_LEVEL = BUILDER
            .comment("Dano extra do boss por nivel medio do grupo.")
            .defineInRange("scaling.bossDamagePerAverageLevel", 0.0035D, 0.0D, 10.0D);
    private static final ForgeConfigSpec.DoubleValue SCALING_BOSS_MAX_HEALTH_BONUS = BUILDER
            .comment("Limite de vida extra por nivel medio.")
            .defineInRange("scaling.bossMaxHealthLevelBonus", 1.0D, 0.0D, 100.0D);
    private static final ForgeConfigSpec.DoubleValue SCALING_BOSS_MAX_DAMAGE_BONUS = BUILDER
            .comment("Limite de dano extra por nivel medio.")
            .defineInRange("scaling.bossMaxDamageLevelBonus", 0.35D, 0.0D, 100.0D);
    private static final ForgeConfigSpec.DoubleValue SCALING_BOSS_DAMAGE_PER_EXTRA_PLAYER = BUILDER
            .comment("Dano extra por jogador adicional.")
            .defineInRange("scaling.bossDamagePerExtraPlayer", 0.08D, 0.0D, 10.0D);

    private static final ForgeConfigSpec.IntValue EXPERIENCE_MAX_LEVEL = BUILDER
            .comment("Nivel maximo do sistema de XP proprio do mod.")
            .defineInRange("experience.maxLevel", 100, 1, 10000);
    private static final ForgeConfigSpec.IntValue EXPERIENCE_POINTS_PER_LEVEL = BUILDER
            .comment("Pontos de atributo ganhos por nivel.")
            .defineInRange("experience.attributePointsPerLevel", 1, 0, 1000);
    private static final ForgeConfigSpec.IntValue EXPERIENCE_FORMULA_BASE = BUILDER
            .comment("Base da formula de XP: base + nivel*linear + nivel^2*quadratic.")
            .defineInRange("experience.formulaBase", 100, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue EXPERIENCE_FORMULA_LINEAR = BUILDER
            .comment("Componente linear da formula de XP.")
            .defineInRange("experience.formulaLinear", 35, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue EXPERIENCE_FORMULA_QUADRATIC = BUILDER
            .comment("Componente quadratico da formula de XP.")
            .defineInRange("experience.formulaQuadratic", 4, 0, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue EXPERIENCE_PARTICIPATION_DISTANCE = BUILDER
            .comment("Distancia maxima para receber XP por participacao em combate.")
            .defineInRange("experience.participationDistance", 32.0D, 1.0D, 1024.0D);
    private static final ForgeConfigSpec.IntValue EXPERIENCE_PARTICIPATION_TIMEOUT = BUILDER
            .comment("Tempo maximo desde o ultimo dano causado para receber XP, em ticks.")
            .defineInRange("experience.participationTimeoutTicks", 300, 1, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.DoubleValue EXPERIENCE_SPAWNER_MULTIPLIER = BUILDER
            .comment("Multiplicador de XP para mobs vindos de spawner vanilla.")
            .defineInRange("experience.spawnerMultiplier", 0.25D, 0.0D, 100.0D);
    private static final ForgeConfigSpec.DoubleValue EXPERIENCE_SUMMON_MULTIPLIER = BUILDER
            .comment("Multiplicador de XP para entidades invocadas/summons.")
            .defineInRange("experience.summonMultiplier", 0.0D, 0.0D, 100.0D);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean logDirtBlock;
    public static int magicNumber;
    public static String magicNumberIntroduction;
    public static Set<Item> items;

    private static boolean validateItemName(final Object obj)
    {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        logDirtBlock = LOG_DIRT_BLOCK.get();
        magicNumber = MAGIC_NUMBER.get();
        magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

        // convert the list of strings into a set of items
        items = ITEM_STRINGS.get().stream()
                .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                .collect(Collectors.toSet());

        ScalingConfig.HEALTH_PER_VITALITY_PAIR = SCALING_HEALTH_PER_VITALITY_PAIR.get();
        ScalingConfig.STRENGTH_PHYSICAL_DAMAGE_PER_POINT = SCALING_STRENGTH_DAMAGE_PER_POINT.get();
        ScalingConfig.DEFENSE_CURVE = SCALING_DEFENSE_CURVE.get();
        ScalingConfig.DEFENSE_MAX_REDUCTION = SCALING_DEFENSE_MAX_REDUCTION.get();
        ScalingConfig.MIN_FINAL_DAMAGE = SCALING_MIN_FINAL_DAMAGE.get();
        ScalingConfig.AGILITY_MOVEMENT_PER_POINT = SCALING_AGILITY_MOVEMENT_PER_POINT.get();
        ScalingConfig.AGILITY_ATTACK_SPEED_PER_POINT = SCALING_AGILITY_ATTACK_SPEED_PER_POINT.get();
        ScalingConfig.RELIC_CONTROL_CURVE = SCALING_RELIC_CONTROL_CURVE.get();
        ScalingConfig.RELIC_CONTROL_MAX_PENALTY_REDUCTION = SCALING_RELIC_MAX_REDUCTION.get();
        ScalingConfig.BOSS_PARTICIPATION_RADIUS = SCALING_BOSS_PARTICIPATION_RADIUS.get();
        ScalingConfig.BOSS_HEALTH_PER_AVERAGE_LEVEL = SCALING_BOSS_HEALTH_PER_LEVEL.get();
        ScalingConfig.BOSS_DAMAGE_PER_AVERAGE_LEVEL = SCALING_BOSS_DAMAGE_PER_LEVEL.get();
        ScalingConfig.BOSS_MAX_HEALTH_LEVEL_BONUS = SCALING_BOSS_MAX_HEALTH_BONUS.get();
        ScalingConfig.BOSS_MAX_DAMAGE_LEVEL_BONUS = SCALING_BOSS_MAX_DAMAGE_BONUS.get();
        ScalingConfig.BOSS_DAMAGE_PER_EXTRA_PLAYER = SCALING_BOSS_DAMAGE_PER_EXTRA_PLAYER.get();

        ExperienceConfig.MAX_LEVEL = EXPERIENCE_MAX_LEVEL.get();
        ExperienceConfig.ATTRIBUTE_POINTS_PER_LEVEL = EXPERIENCE_POINTS_PER_LEVEL.get();
        ExperienceConfig.XP_BASE = EXPERIENCE_FORMULA_BASE.get();
        ExperienceConfig.XP_LINEAR = EXPERIENCE_FORMULA_LINEAR.get();
        ExperienceConfig.XP_QUADRATIC = EXPERIENCE_FORMULA_QUADRATIC.get();
        ExperienceConfig.PARTICIPATION_DISTANCE = EXPERIENCE_PARTICIPATION_DISTANCE.get();
        ExperienceConfig.PARTICIPATION_TIMEOUT_TICKS = EXPERIENCE_PARTICIPATION_TIMEOUT.get();
        ExperienceConfig.SPAWNER_XP_MULTIPLIER = EXPERIENCE_SPAWNER_MULTIPLIER.get();
        ExperienceConfig.SUMMON_XP_MULTIPLIER = EXPERIENCE_SUMMON_MULTIPLIER.get();
    }
}
