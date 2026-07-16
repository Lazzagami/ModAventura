package com.ruan.medieval_fantasy.combat.heat;

import com.ruan.medieval_fantasy.ExampleMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

public class HeatDamageSource {

    public static final ResourceKey<DamageType> RELIC_OVERHEAT =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(ExampleMod.MODID, "relic_overheat"));

    public static DamageSource source(LivingEntity holder) {
        return new DamageSource(holder.level().registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(RELIC_OVERHEAT), holder);
    }
}
