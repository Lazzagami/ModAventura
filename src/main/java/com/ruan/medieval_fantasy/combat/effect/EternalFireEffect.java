package com.ruan.medieval_fantasy.combat.effect;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.combat.damage.DamageNumber;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
    private static final int FIRE_SECONDS = 4;
    private static final int DAMAGE_INTERVAL_TICKS = 20;

    public static int addStacks(LivingEntity target, int amount) {
        if (target.level().isClientSide() || amount <= 0) {
            return getStacks(target);
        }

        int stacks = getStacks(target) + amount;
        target.getPersistentData().putInt(STACKS_TAG, stacks);
        target.setSecondsOnFire(FIRE_SECONDS);
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

            if (!living.isOnFire()) {
                continue;
            }

            CompoundTag data = living.getPersistentData();
            int tick = data.getInt(TICK_TAG) + 1;
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

    public static boolean isFireDamage(net.minecraft.world.damagesource.DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE);
    }
}
