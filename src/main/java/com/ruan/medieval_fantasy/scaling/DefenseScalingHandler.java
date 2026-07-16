package com.ruan.medieval_fantasy.scaling;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.progression.specialization.PassiveEffectHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class DefenseScalingHandler {

    private DefenseScalingHandler() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }

        double reduction = PlayerScalingManager.getDefenseReduction(player);
        float reduced = (float) (event.getAmount() * (1.0D - reduction));
        reduced = PassiveEffectHandler.modifyIncomingDamage(player, event.getSource(), reduced);
        event.setAmount(Math.max((float) ScalingConfig.MIN_FINAL_DAMAGE, reduced));
        PassiveEffectHandler.onPlayerHurt(player);
    }
}
