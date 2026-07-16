package com.ruan.medieval_fantasy.scaling;

import com.ruan.medieval_fantasy.progression.specialization.PassiveEffectHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public final class PlayerScalingManager {

    public static final UUID VITALITY_HEALTH_UUID = UUID.fromString("a761a5c8-7c49-4d76-8bbf-06a9e43f4a01");
    public static final UUID AGILITY_MOVEMENT_UUID = UUID.fromString("42184b98-7e96-498b-83ac-d78f1098a201");
    public static final UUID AGILITY_ATTACK_SPEED_UUID = UUID.fromString("3625c7da-cb22-4f6e-a18a-1cc6cf4adbd6");

    private PlayerScalingManager() {
    }

    public static void recalculate(ServerPlayer player) {
        applyVitality(player);
        applyAgility(player);
        PassiveEffectHandler.applyPermanentPassives(player);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    public static double getPhysicalDamageMultiplier(ServerPlayer player) {
        int strength = PlayerScalingData.getAttribute(player, PlayerAttribute.STRENGTH);
        return ScalingFormula.physicalDamageMultiplier(strength);
    }

    public static double getDefenseReduction(ServerPlayer player) {
        int defense = PlayerScalingData.getAttribute(player, PlayerAttribute.DEFENSE);
        return ScalingFormula.defenseReduction(defense);
    }

    public static double getAdditionalHealth(ServerPlayer player) {
        int vitality = PlayerScalingData.getAttribute(player, PlayerAttribute.VITALITY);
        return ScalingFormula.additionalHealth(vitality);
    }

    public static double getMovementMultiplier(ServerPlayer player) {
        int agility = PlayerScalingData.getAttribute(player, PlayerAttribute.AGILITY);
        return ScalingFormula.movementMultiplier(agility);
    }

    public static double getAttackSpeedMultiplier(ServerPlayer player) {
        int agility = PlayerScalingData.getAttribute(player, PlayerAttribute.AGILITY);
        return ScalingFormula.attackSpeedMultiplier(agility);
    }

    private static void applyVitality(ServerPlayer player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        maxHealth.removeModifier(VITALITY_HEALTH_UUID);
        double bonus = getAdditionalHealth(player);
        if (bonus > 0.0D) {
            maxHealth.addPermanentModifier(new AttributeModifier(
                    VITALITY_HEALTH_UUID,
                    "Medieval Fantasy Vitality",
                    bonus,
                    AttributeModifier.Operation.ADDITION
            ));
        }
    }

    private static void applyAgility(ServerPlayer player) {
        int agility = PlayerScalingData.getAttribute(player, PlayerAttribute.AGILITY);
        applyMultiplier(player.getAttribute(Attributes.MOVEMENT_SPEED), AGILITY_MOVEMENT_UUID,
                "Medieval Fantasy Agility Movement", ScalingFormula.movementMultiplier(agility) - 1.0D);
        applyMultiplier(player.getAttribute(Attributes.ATTACK_SPEED), AGILITY_ATTACK_SPEED_UUID,
                "Medieval Fantasy Agility Attack Speed", ScalingFormula.attackSpeedMultiplier(agility) - 1.0D);
    }

    private static void applyMultiplier(AttributeInstance attribute, UUID uuid, String name, double amount) {
        if (attribute == null) {
            return;
        }

        attribute.removeModifier(uuid);
        if (amount > 0.0D) {
            attribute.addPermanentModifier(new AttributeModifier(uuid, name, amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }
}
