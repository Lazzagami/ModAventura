package com.ruan.medieval_fantasy.scaling;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.progression.specialization.PassiveEffectHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class DamageScalingHandler {

    private DamageScalingHandler() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();

        if (!(attacker instanceof ServerPlayer player)) {
            return;
        }

        if (!isPhysicalPlayerDamage(source, player)) {
            return;
        }

        float scaled = (float) (event.getAmount() * PlayerScalingManager.getPhysicalDamageMultiplier(player));
        LivingEntity target = event.getEntity();
        scaled = PassiveEffectHandler.modifyOutgoingPhysicalDamage(player, target, scaled);
        PassiveEffectHandler.onMeleeHit(player);
        event.setAmount(scaled);
    }

    private static boolean isPhysicalPlayerDamage(DamageSource source, ServerPlayer player) {
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.is(DamageTypeTags.BYPASSES_ARMOR)
                || source.is(DamageTypeTags.IS_PROJECTILE)) {
            return false;
        }

        Entity direct = source.getDirectEntity();
        return direct == player || direct instanceof LivingEntity;
    }
}
