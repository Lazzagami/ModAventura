package com.ruan.medieval_fantasy.progression.specialization;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PassiveEffectHandler {

    public static final UUID ROBUST_HEART_UUID = UUID.fromString("b9d1f6c0-5812-4e2f-9a11-7db251300001");
    public static final UUID FEROCIOUS_RUNNER_UUID = UUID.fromString("b9d1f6c0-5812-4e2f-9a11-7db251300002");
    public static final UUID SWIFT_BLADE_UUID = UUID.fromString("b9d1f6c0-5812-4e2f-9a11-7db251300003");
    public static final UUID UNSHAKABLE_UUID = UUID.fromString("b9d1f6c0-5812-4e2f-9a11-7db251300004");
    public static final UUID PHANTOM_STEP_UUID = UUID.fromString("b9d1f6c0-5812-4e2f-9a11-7db251300005");
    public static final UUID BATTLE_RHYTHM_UUID = UUID.fromString("b9d1f6c0-5812-4e2f-9a11-7db251300006");

    public static final String PHANTOM_STEP_COOLDOWN = "phantom_step_cooldown";
    public static final String PHANTOM_STEP_TIMER = "phantom_step_timer";
    public static final String BATTLE_RHYTHM_STACKS = "battle_rhythm_stacks";
    public static final String BATTLE_RHYTHM_TIMEOUT = "battle_rhythm_timeout";

    private PassiveEffectHandler() {
    }

    public static void applyPermanentPassives(ServerPlayer player) {
        removeKnownModifiers(player);

        if (PassiveChoiceManager.hasPassive(player, "robust_heart")) {
            addModifier(player.getAttribute(Attributes.MAX_HEALTH), ROBUST_HEART_UUID, "Coração Robusto",
                    SpecializationConfig.ROBUST_HEART_HEALTH, AttributeModifier.Operation.ADDITION);
        }

        if (PassiveChoiceManager.hasPassive(player, "ferocious_runner")) {
            addModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), FEROCIOUS_RUNNER_UUID, "Corredor Feroz",
                    SpecializationConfig.FEROCIOUS_RUNNER_MOVEMENT, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }

        if (PassiveChoiceManager.hasPassive(player, "swift_blade")) {
            addModifier(player.getAttribute(Attributes.ATTACK_SPEED), SWIFT_BLADE_UUID, "Lâmina Veloz",
                    SpecializationConfig.SWIFT_BLADE_ATTACK_SPEED, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }

        if (PassiveChoiceManager.hasPassive(player, "unshakable")) {
            addModifier(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), UNSHAKABLE_UUID, "Inabalável",
                    0.60D, AttributeModifier.Operation.ADDITION);
        }

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    public static void removeTemporaryPassives(ServerPlayer player) {
        removeModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), PHANTOM_STEP_UUID);
        removeModifier(player.getAttribute(Attributes.ATTACK_SPEED), BATTLE_RHYTHM_UUID);
        PassiveChoiceData.runtime(player).remove(PHANTOM_STEP_TIMER);
        PassiveChoiceData.runtime(player).remove(BATTLE_RHYTHM_STACKS);
        PassiveChoiceData.runtime(player).remove(BATTLE_RHYTHM_TIMEOUT);
    }

    public static float modifyOutgoingPhysicalDamage(ServerPlayer player, LivingEntity target, float amount) {
        double multiplier = 1.0D;

        if (PassiveChoiceManager.hasPassive(player, "brutal_strike")) {
            multiplier += SpecializationConfig.BRUTAL_STRIKE_DAMAGE;
        }

        if (PassiveChoiceManager.hasPassive(player, "executor")
                && target.getHealth() <= target.getMaxHealth() * SpecializationConfig.EXECUTOR_TARGET_HEALTH) {
            multiplier += SpecializationConfig.EXECUTOR_DAMAGE;
        }

        return (float) (amount * multiplier);
    }

    public static float modifyIncomingDamage(ServerPlayer player, DamageSource source, float amount) {
        double reduction = 0.0D;

        if (PassiveChoiceManager.hasPassive(player, "iron_wall") && isPhysical(source)) {
            reduction += SpecializationConfig.IRON_WALL_PHYSICAL_REDUCTION;
        }

        if (PassiveChoiceManager.hasPassive(player, "elemental_guardian")
                && (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION))) {
            reduction += 0.15D;
        }

        return (float) (amount * Math.max(0.0D, 1.0D - Math.min(0.75D, reduction)));
    }

    public static float modifyRelicPenalty(ServerPlayer player, float amount) {
        if (PassiveChoiceManager.hasPassive(player, "elemental_domain")) {
            return (float) (amount * (1.0D - SpecializationConfig.OVERHEAT_DAMAGE_REDUCTION));
        }

        return amount;
    }

    public static int modifyRelicThreshold(ServerPlayer player, int baseThreshold) {
        if (!PassiveChoiceManager.hasPassive(player, "disciplined_bearer")) {
            return baseThreshold;
        }

        int delay = (int) Math.round((100 - baseThreshold) * SpecializationConfig.DISCIPLINED_BEARER_THRESHOLD_DELAY);
        return Math.min(100, baseThreshold + delay);
    }

    public static void onPlayerHurt(ServerPlayer player) {
        if (!PassiveChoiceManager.hasPassive(player, "phantom_step")) {
            return;
        }

        long now = player.level().getGameTime();
        long cooldownUntil = PassiveChoiceData.cooldowns(player).getLong(PHANTOM_STEP_COOLDOWN);
        if (cooldownUntil > now) {
            return;
        }

        PassiveChoiceData.cooldowns(player).putLong(PHANTOM_STEP_COOLDOWN, now + SpecializationConfig.PHANTOM_STEP_COOLDOWN_TICKS);
        PassiveChoiceData.runtime(player).putInt(PHANTOM_STEP_TIMER, SpecializationConfig.PHANTOM_STEP_DURATION_TICKS);
        applyTemporaryMovement(player, SpecializationConfig.PHANTOM_STEP_MOVEMENT);
    }

    public static void onMeleeHit(ServerPlayer player) {
        if (!PassiveChoiceManager.hasPassive(player, "battle_rhythm")) {
            return;
        }

        int stacks = Math.min(SpecializationConfig.BATTLE_RHYTHM_MAX_STACKS,
                PassiveChoiceData.runtime(player).getInt(BATTLE_RHYTHM_STACKS) + 1);
        PassiveChoiceData.runtime(player).putInt(BATTLE_RHYTHM_STACKS, stacks);
        PassiveChoiceData.runtime(player).putInt(BATTLE_RHYTHM_TIMEOUT, SpecializationConfig.BATTLE_RHYTHM_TIMEOUT_TICKS);
        applyBattleRhythm(player, stacks);
    }

    public static void tick(ServerPlayer player) {
        int phantomTimer = PassiveChoiceData.runtime(player).getInt(PHANTOM_STEP_TIMER);
        if (phantomTimer > 0) {
            PassiveChoiceData.runtime(player).putInt(PHANTOM_STEP_TIMER, phantomTimer - 1);
            if (phantomTimer == 1) {
                removeModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), PHANTOM_STEP_UUID);
            }
        }

        int battleTimeout = PassiveChoiceData.runtime(player).getInt(BATTLE_RHYTHM_TIMEOUT);
        if (battleTimeout > 0) {
            PassiveChoiceData.runtime(player).putInt(BATTLE_RHYTHM_TIMEOUT, battleTimeout - 1);
            if (battleTimeout == 1) {
                PassiveChoiceData.runtime(player).putInt(BATTLE_RHYTHM_STACKS, 0);
                removeModifier(player.getAttribute(Attributes.ATTACK_SPEED), BATTLE_RHYTHM_UUID);
            }
        }
    }

    public static String activeTitleDisplay(ServerPlayer player) {
        return TitleManager.displayName(PassiveChoiceData.equippedTitle(player));
    }

    private static void removeKnownModifiers(ServerPlayer player) {
        Set<UUID> ids = new HashSet<>();
        ids.add(ROBUST_HEART_UUID);
        ids.add(FEROCIOUS_RUNNER_UUID);
        ids.add(SWIFT_BLADE_UUID);
        ids.add(UNSHAKABLE_UUID);

        for (UUID id : ids) {
            removeModifier(player.getAttribute(Attributes.MAX_HEALTH), id);
            removeModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), id);
            removeModifier(player.getAttribute(Attributes.ATTACK_SPEED), id);
            removeModifier(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), id);
        }
    }

    private static void applyTemporaryMovement(ServerPlayer player, double amount) {
        addModifier(player.getAttribute(Attributes.MOVEMENT_SPEED), PHANTOM_STEP_UUID, "Passo Fantasma",
                amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false, true));
    }

    private static void applyBattleRhythm(ServerPlayer player, int stacks) {
        addModifier(player.getAttribute(Attributes.ATTACK_SPEED), BATTLE_RHYTHM_UUID, "Ritmo de Batalha",
                SpecializationConfig.BATTLE_RHYTHM_ATTACK_SPEED_PER_STACK * stacks,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    private static void addModifier(AttributeInstance attribute, UUID uuid, String name, double amount, AttributeModifier.Operation operation) {
        if (attribute == null) {
            return;
        }

        attribute.removeModifier(uuid);
        if (amount != 0.0D) {
            attribute.addPermanentModifier(new AttributeModifier(uuid, name, amount, operation));
        }
    }

    private static void removeModifier(AttributeInstance attribute, UUID uuid) {
        if (attribute != null) {
            attribute.removeModifier(uuid);
        }
    }

    private static boolean isPhysical(DamageSource source) {
        return !source.is(DamageTypeTags.IS_FIRE)
                && !source.is(DamageTypeTags.IS_EXPLOSION)
                && !source.is(DamageTypeTags.IS_PROJECTILE)
                && !source.is(DamageTypeTags.BYPASSES_ARMOR);
    }
}
