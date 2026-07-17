package com.ruan.medieval_fantasy.combat.effect;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.combat.damage.DamageNumber;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class EternalFireEffect {

    private static final String STACKS_TAG = "eternal_fire_stacks";
    private static final String TICK_TAG = "eternal_fire_tick";
    private static final int DAMAGE_INTERVAL_TICKS = 20;

    public static int addStacks(LivingEntity target, int amount) {
        if (target.level().isClientSide() || amount <= 0) {
            return getStacks(target);
        }

        int stacks = getStacks(target) + amount;
        target.getPersistentData().putInt(STACKS_TAG, stacks);
        target.clearFire();
        if (target.level() instanceof ServerLevel serverLevel) {
            spawnEternalFireParticles(serverLevel, target, Math.min(10, 3 + stacks));
        }
        return stacks;
    }

    public static int getStacks(LivingEntity target) {
        return target.getPersistentData().getInt(STACKS_TAG);
    }

    public static void clear(LivingEntity target) {
        CompoundTag data = target.getPersistentData();
        data.remove(STACKS_TAG);
        data.remove(TICK_TAG);
        target.clearFire();
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide()) {
            return;
        }

        if (!(event.level instanceof ServerLevel level)) {
            return;
        }

        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
                continue;
            }

            int stacks = getStacks(living);
            if (stacks <= 0) {
                continue;
            }

            if (living.isInWaterOrRain()) {
                clear(living);
                continue;
            }

            CompoundTag data = living.getPersistentData();
            int tick = data.getInt(TICK_TAG) + 1;
            if (tick % 5 == 0) {
                spawnEternalFireParticles(level, living, Math.min(6, 2 + stacks / 2));
                living.clearFire();
            }

            if (tick < DAMAGE_INTERVAL_TICKS) {
                data.putInt(TICK_TAG, tick);
                continue;
            }

            data.putInt(TICK_TAG, 0);
            float burnDamage = stacks;

            if (living.hurt(living.damageSources().onFire(), burnDamage)) {
                DamageNumber.spawnBurn(level, living, Component.literal("🔥 -" + (int) burnDamage)
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            }
        }
    }

    private static void spawnEternalFireParticles(ServerLevel level, LivingEntity living, int count) {
        level.sendParticles(ParticleTypes.SMALL_FLAME,
                living.getX(),
                living.getY() + living.getBbHeight() * 0.55D,
                living.getZ(),
                count,
                living.getBbWidth() * 0.35D,
                living.getBbHeight() * 0.35D,
                living.getBbWidth() * 0.35D,
                0.015D);
        level.sendParticles(ParticleTypes.SMOKE,
                living.getX(),
                living.getY() + living.getBbHeight() * 0.45D,
                living.getZ(),
                Math.max(1, count / 2),
                living.getBbWidth() * 0.25D,
                living.getBbHeight() * 0.25D,
                living.getBbWidth() * 0.25D,
                0.01D);
    }

    public static boolean isFireDamage(net.minecraft.world.damagesource.DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE);
    }
}
