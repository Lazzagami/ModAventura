package com.ruan.medieval_fantasy.progression.specialization;

import com.ruan.medieval_fantasy.scaling.PlayerAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PassiveRegistry {

    private static final Map<String, PassiveDefinition> BY_ID = new LinkedHashMap<>();
    private static final Map<String, List<PassiveDefinition>> BY_MILESTONE = new LinkedHashMap<>();

    static {
        registerInitialPassives();
    }

    private PassiveRegistry() {
    }

    public static void register(PassiveDefinition definition) {
        BY_ID.put(definition.id(), definition);
        BY_MILESTONE.computeIfAbsent(key(definition.attribute(), definition.milestone()), ignored -> new ArrayList<>())
                .add(definition);
    }

    public static Optional<PassiveDefinition> byId(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static Optional<PassiveDefinition> option(PlayerAttribute attribute, int milestone, int option) {
        return options(attribute, milestone).stream()
                .filter(definition -> definition.option() == option)
                .findFirst();
    }

    public static List<PassiveDefinition> options(PlayerAttribute attribute, int milestone) {
        return BY_MILESTONE.getOrDefault(key(attribute, milestone), List.of()).stream()
                .filter(definition -> !definition.reserved())
                .sorted(Comparator.comparingInt(PassiveDefinition::option))
                .toList();
    }

    public static Collection<PassiveDefinition> all() {
        return BY_ID.values();
    }

    public static String key(PlayerAttribute attribute, int milestone) {
        return attribute.id() + "." + milestone;
    }

    public static int[] milestones() {
        return SpecializationConfig.MILESTONES;
    }

    private static void registerInitialPassives() {
        agility();
        vitality();
        strength();
        defense();
        relicControl();
        reservedMilestones();
    }

    private static void agility() {
        register(PassiveDefinition.builder("ferocious_runner", PlayerAttribute.AGILITY, 15, 1)
                .title("ferocious_runner", "Corredor Feroz")
                .display("Corredor Feroz", "+15% velocidade de movimento.")
                .effect(PassiveType.PERMANENT_ATTRIBUTE, SpecializationConfig.FEROCIOUS_RUNNER_MOVEMENT)
                .modifier("36d5c4a8-5664-464d-9f4b-9d90d6d01a01")
                .build());
        register(PassiveDefinition.builder("swift_blade", PlayerAttribute.AGILITY, 15, 2)
                .title("swift_blade", "Lâmina Veloz")
                .display("Lâmina Veloz", "+15% velocidade de ataque.")
                .effect(PassiveType.PERMANENT_ATTRIBUTE, SpecializationConfig.SWIFT_BLADE_ATTACK_SPEED)
                .modifier("36d5c4a8-5664-464d-9f4b-9d90d6d01a02")
                .build());
        register(PassiveDefinition.builder("phantom_step", PlayerAttribute.AGILITY, 30, 1)
                .title("phantom_step", "Passo Fantasma")
                .display("Passo Fantasma", "Ao receber dano: +20% movimento por 3s. Cooldown 10s.")
                .effect(PassiveType.ON_HURT_BUFF, SpecializationConfig.PHANTOM_STEP_MOVEMENT)
                .timing(SpecializationConfig.PHANTOM_STEP_DURATION_TICKS, SpecializationConfig.PHANTOM_STEP_COOLDOWN_TICKS)
                .modifier("36d5c4a8-5664-464d-9f4b-9d90d6d01a03")
                .build());
        register(PassiveDefinition.builder("battle_rhythm", PlayerAttribute.AGILITY, 30, 2)
                .title("blade_dancer", "Dançarino das Lâminas")
                .display("Ritmo de Batalha", "Acertos corpo a corpo dão +2% ataque por stack, até 5.")
                .effect(PassiveType.COMBO_ATTACK_SPEED, SpecializationConfig.BATTLE_RHYTHM_ATTACK_SPEED_PER_STACK)
                .timing(SpecializationConfig.BATTLE_RHYTHM_TIMEOUT_TICKS, 0)
                .stacks(SpecializationConfig.BATTLE_RHYTHM_MAX_STACKS)
                .modifier("36d5c4a8-5664-464d-9f4b-9d90d6d01a04")
                .build());
        register(PassiveDefinition.builder("untouchable", PlayerAttribute.AGILITY, 50, 1)
                .title("untouchable", "Intocável")
                .display("Intocável", "Preparado para integração com um futuro sistema de esquiva.")
                .effect(PassiveType.RESERVED, 0.15D)
                .build());
        register(PassiveDefinition.builder("blade_dance", PlayerAttribute.AGILITY, 50, 2)
                .title("blade_dance", "Dança das Lâminas")
                .display("Dança das Lâminas", "Preparado para ataques rápidos temporários sem dano extra direto.")
                .effect(PassiveType.RESERVED, 0.0D)
                .build());
    }

    private static void vitality() {
        register(PassiveDefinition.builder("robust_heart", PlayerAttribute.VITALITY, 15, 1)
                .title("robust_heart", "Coração Robusto")
                .display("Coração Robusto", "+3 corações de vida máxima.")
                .effect(PassiveType.PERMANENT_ATTRIBUTE, SpecializationConfig.ROBUST_HEART_HEALTH)
                .modifier("36d5c4a8-5664-464d-9f4b-9d90d6d01b01")
                .build());
        register(PassiveDefinition.builder("persistent_blood", PlayerAttribute.VITALITY, 15, 2)
                .title("persistent_blood", "Sangue Persistente")
                .display("Sangue Persistente", "Após 8s sem dano, regenera 1 vida a cada 4s.")
                .effect(PassiveType.RESERVED, 0.0D)
                .build());
        register(PassiveDefinition.builder("survivor", PlayerAttribute.VITALITY, 30, 1)
                .title("survivor", "Sobrevivente")
                .display("Sobrevivente", "Preparado para +20% cura recebida abaixo de 25% da vida.")
                .effect(PassiveType.RESERVED, 0.20D)
                .build());
        register(PassiveDefinition.builder("last_breath", PlayerAttribute.VITALITY, 30, 2)
                .title("last_breath", "Último Fôlego")
                .display("Último Fôlego", "Dano fatal deixa meio coração uma vez a cada 5 minutos.")
                .effect(PassiveType.RESERVED, 0.0D)
                .timing(20, 6000)
                .build());
        register(PassiveDefinition.builder("living_colossus", PlayerAttribute.VITALITY, 50, 1)
                .title("living_colossus", "Colosso Vivo")
                .display("Colosso Vivo", "Preparado para resistência a knockback baseada na vida máxima.")
                .effect(PassiveType.RESERVED, 0.0D)
                .build());
        register(PassiveDefinition.builder("ancestral_vitality", PlayerAttribute.VITALITY, 50, 2)
                .title("ancestral_vitality", "Vitalidade Ancestral")
                .display("Vitalidade Ancestral", "Preparado para absorção após tempo com vida cheia.")
                .effect(PassiveType.RESERVED, 0.0D)
                .build());
    }

    private static void strength() {
        register(PassiveDefinition.builder("brutal_strike", PlayerAttribute.STRENGTH, 15, 1)
                .title("brutal_strike", "Golpe Brutal")
                .display("Golpe Brutal", "+15% dano físico.")
                .effect(PassiveType.CONDITIONAL_DAMAGE, SpecializationConfig.BRUTAL_STRIKE_DAMAGE)
                .build());
        register(PassiveDefinition.builder("executor", PlayerAttribute.STRENGTH, 15, 2)
                .title("executor", "Executor")
                .display("Executor", "+25% dano físico contra inimigos abaixo de 20% da vida.")
                .effect(PassiveType.CONDITIONAL_DAMAGE, SpecializationConfig.EXECUTOR_DAMAGE)
                .build());
        register(PassiveDefinition.builder("guard_breaker", PlayerAttribute.STRENGTH, 30, 1)
                .title("guard_breaker", "Quebra-Guardas")
                .display("Quebra-Guardas", "Preparado para ignorar 15% da armadura do alvo.")
                .effect(PassiveType.RESERVED, 0.15D)
                .build());
        register(PassiveDefinition.builder("devastating_impact", PlayerAttribute.STRENGTH, 30, 2)
                .title("devastating_impact", "Impacto Devastador")
                .display("Impacto Devastador", "Preparado para ataques carregados mais pesados.")
                .effect(PassiveType.RESERVED, 0.10D)
                .build());
        register(PassiveDefinition.builder("growing_fury", PlayerAttribute.STRENGTH, 50, 1)
                .title("growing_fury", "Fúria Crescente")
                .display("Fúria Crescente", "Preparado para stacks de dano ao receber dano.")
                .effect(PassiveType.RESERVED, 0.03D)
                .build());
        register(PassiveDefinition.builder("weapon_master", PlayerAttribute.STRENGTH, 50, 2)
                .title("weapon_master", "Mestre de Armas")
                .display("Mestre de Armas", "Preparado para reduzir penalidades de armas pesadas.")
                .effect(PassiveType.RESERVED, 0.25D)
                .build());
    }

    private static void defense() {
        register(PassiveDefinition.builder("iron_wall", PlayerAttribute.DEFENSE, 15, 1)
                .title("iron_wall", "Muralha de Ferro")
                .display("Muralha de Ferro", "+15% resistência a dano físico.")
                .effect(PassiveType.CONDITIONAL_DAMAGE, SpecializationConfig.IRON_WALL_PHYSICAL_REDUCTION)
                .build());
        register(PassiveDefinition.builder("elemental_guardian", PlayerAttribute.DEFENSE, 15, 2)
                .title("elemental_guardian", "Guardião Elemental")
                .display("Guardião Elemental", "15% resistência a fogo e efeitos elementais preparados.")
                .effect(PassiveType.CONDITIONAL_DAMAGE, 0.15D)
                .build());
        register(PassiveDefinition.builder("unshakable", PlayerAttribute.DEFENSE, 30, 1)
                .title("unshakable", "Inabalável")
                .display("Inabalável", "+60% resistência a knockback.")
                .effect(PassiveType.PERMANENT_ATTRIBUTE, 0.60D)
                .modifier("36d5c4a8-5664-464d-9f4b-9d90d6d01d01")
                .build());
        register(PassiveDefinition.builder("retaliation", PlayerAttribute.DEFENSE, 30, 2)
                .title("retaliator", "Retaliador")
                .display("Retaliação", "Preparado para fortalecer o próximo ataque após resistir um golpe.")
                .effect(PassiveType.RESERVED, 0.15D)
                .build());
        register(PassiveDefinition.builder("living_fortress", PlayerAttribute.DEFENSE, 50, 1)
                .title("living_fortress", "Fortaleza Viva")
                .display("Fortaleza Viva", "Preparado para ganhar defesa ao permanecer parado.")
                .effect(PassiveType.RESERVED, 0.15D)
                .build());
        register(PassiveDefinition.builder("adaptive_resistance", PlayerAttribute.DEFENSE, 50, 2)
                .title("adaptive_resistance", "Resistência Adaptativa")
                .display("Resistência Adaptativa", "Preparado para resistir danos repetidos do mesmo tipo.")
                .effect(PassiveType.RESERVED, 0.04D)
                .build());
    }

    private static void relicControl() {
        register(PassiveDefinition.builder("disciplined_bearer", PlayerAttribute.RELIC_CONTROL, 15, 1)
                .title("disciplined_bearer", "Portador Disciplinado")
                .display("Portador Disciplinado", "Penalidades críticas de relíquias começam 15% mais tarde.")
                .effect(PassiveType.CONDITIONAL_DAMAGE, SpecializationConfig.DISCIPLINED_BEARER_THRESHOLD_DELAY)
                .build());
        register(PassiveDefinition.builder("ancestral_conductor", PlayerAttribute.RELIC_CONTROL, 15, 2)
                .title("ancestral_conductor", "Condutor Ancestral")
                .display("Condutor Ancestral", "Preparado para recuperação 15% mais rápida de medidores negativos.")
                .effect(PassiveType.RESERVED, 0.15D)
                .build());
        register(PassiveDefinition.builder("elemental_domain", PlayerAttribute.RELIC_CONTROL, 30, 1)
                .title("elemental_domain", "Domínio Elemental")
                .display("Domínio Elemental", "-20% dano térmico e consequências elementais das próprias relíquias.")
                .effect(PassiveType.CONDITIONAL_DAMAGE, SpecializationConfig.OVERHEAT_DAMAGE_REDUCTION)
                .build());
        register(PassiveDefinition.builder("incorruptible_mind", PlayerAttribute.RELIC_CONTROL, 30, 2)
                .title("incorruptible_mind", "Mente Incorruptível")
                .display("Mente Incorruptível", "Preparado para reduzir duração de efeitos negativos de relíquia.")
                .effect(PassiveType.RESERVED, 0.20D)
                .build());
        register(PassiveDefinition.builder("relic_master", PlayerAttribute.RELIC_CONTROL, 50, 1)
                .title("relic_master", "Mestre das Relíquias")
                .display("Mestre das Relíquias", "Preparado para recuperação +25% ao guardar relíquias.")
                .effect(PassiveType.RESERVED, 0.25D)
                .build());
        register(PassiveDefinition.builder("legendary_bond", PlayerAttribute.RELIC_CONTROL, 50, 2)
                .title("legendary_bond", "Vínculo Lendário")
                .display("Vínculo Lendário", "Preparado para pequenos benefícios temáticos em estado crítico.")
                .effect(PassiveType.RESERVED, 0.10D)
                .build());
    }

    private static void reservedMilestones() {
        for (PlayerAttribute attribute : List.of(PlayerAttribute.VITALITY, PlayerAttribute.STRENGTH,
                PlayerAttribute.DEFENSE, PlayerAttribute.AGILITY, PlayerAttribute.RELIC_CONTROL)) {
            register(PassiveDefinition.builder(attribute.id() + "_75_reserved", attribute, 75, 0)
                    .display("Marco 75", "Reservado para expansão.")
                    .effect(PassiveType.RESERVED, 0.0D)
                    .build());
            register(PassiveDefinition.builder(attribute.id() + "_100_reserved", attribute, 100, 0)
                    .display("Marco 100", "Reservado para expansão.")
                    .effect(PassiveType.RESERVED, 0.0D)
                    .build());
        }
    }
}
