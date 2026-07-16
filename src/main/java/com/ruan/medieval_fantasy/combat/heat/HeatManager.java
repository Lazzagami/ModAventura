package com.ruan.medieval_fantasy.combat.heat;

import com.ruan.medieval_fantasy.combat.heat.network.HeatNetworkHandler;
import com.ruan.medieval_fantasy.entity.custom.CavaleiroDasCinzas;
import com.ruan.medieval_fantasy.item.ModItems;
import com.ruan.medieval_fantasy.scaling.RelicScalingHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class HeatManager {

    public static int addHeat(LivingEntity holder, int amount) {
        if (holder.level().isClientSide() || amount <= 0) {
            return getHeat(holder);
        }

        CompoundTag data = holder.getPersistentData();
        int heat = clamp(getHeat(holder) + amount);
        data.putInt(HeatData.HEAT_TAG, heat);
        data.putInt(HeatData.LAST_ATTACK_TICK_TAG, holder.tickCount);
        syncIfNeeded(holder, true);
        return heat;
    }

    public static int reduceHeat(LivingEntity holder, int amount) {
        if (holder.level().isClientSide() || amount <= 0) {
            return getHeat(holder);
        }

        setHeat(holder, getHeat(holder) - amount);
        return getHeat(holder);
    }

    public static void resetHeat(LivingEntity holder) {
        if (holder.level().isClientSide()) {
            return;
        }

        int previousHeat = getHeat(holder);
        setHeat(holder, 0);
        holder.clearFire();
        holder.removeEffect(MobEffects.WEAKNESS);

        if (previousHeat > 0) {
            playWaterCoolingEffects(holder);
        }
    }

    public static int getHeat(LivingEntity holder) {
        return clamp(holder.getPersistentData().getInt(HeatData.HEAT_TAG));
    }

    public static HeatStage getStage(LivingEntity holder) {
        return getEffectiveStage(holder);
    }

    public static void serverTick(LivingEntity holder) {
        if (holder.level().isClientSide()) {
            return;
        }

        int heat = getHeat(holder);
        if (heat <= 0) {
            removeHeatPenalties(holder);
            syncIfNeeded(holder, false);
            return;
        }

        if (holder.isInWaterOrBubble()) {
            resetHeat(holder);
            return;
        }

        coolFromRain(holder);
        coolPassively(holder);
        applyStageEffects(holder);
        syncIfNeeded(holder, false);
    }

    public static void clearOnDeath(LivingEntity holder) {
        holder.getPersistentData().remove(HeatData.HEAT_TAG);
        holder.getPersistentData().remove(HeatData.LAST_ATTACK_TICK_TAG);
        holder.getPersistentData().remove(HeatData.SELECTED_COOL_TICK_TAG);
        holder.getPersistentData().remove(HeatData.STORED_COOL_TICK_TAG);
        holder.getPersistentData().remove(HeatData.RAIN_COOL_TICK_TAG);
        holder.getPersistentData().remove(HeatData.THERMAL_TICK_TAG);
        holder.getPersistentData().remove(HeatData.SOUND_TICK_TAG);

        if (holder instanceof ServerPlayer player) {
            HeatNetworkHandler.sync(player, 0);
        }
    }

    public static void sync(LivingEntity holder) {
        syncIfNeeded(holder, true);
    }

    public static boolean isHoldingRelic(LivingEntity holder) {
        return isEternalFireBlade(holder.getMainHandItem()) || isEternalFireBlade(holder.getOffhandItem());
    }

    public static boolean hasRelicStored(LivingEntity holder) {
        if (isHoldingRelic(holder)) {
            return true;
        }

        if (holder instanceof Player player) {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                if (isEternalFireBlade(player.getInventory().getItem(slot))) {
                    return true;
                }
            }

            return false;
        }

        return isEternalFireBlade(holder.getItemBySlot(EquipmentSlot.MAINHAND));
    }

    public static boolean isEternalFireBlade(ItemStack stack) {
        return stack.is(ModItems.ETERNAL_FIRE_BLADE.get());
    }

    public static boolean isImmuneToRelicOverheat(LivingEntity holder) {
        return holder instanceof CavaleiroDasCinzas;
    }

    private static void setHeat(LivingEntity holder, int heat) {
        int clamped = clamp(heat);
        holder.getPersistentData().putInt(HeatData.HEAT_TAG, clamped);
        syncIfNeeded(holder, true);
    }

    private static void coolPassively(LivingEntity holder) {
        if (!hasRelicStored(holder)) {
            reduceDroppedCooling(holder);
            return;
        }

        if (isHoldingRelic(holder)) {
            int lastAttackTick = holder.getPersistentData().getInt(HeatData.LAST_ATTACK_TICK_TAG);
            if (holder.tickCount - lastAttackTick < HeatData.NO_ATTACK_COOL_DELAY_TICKS) {
                holder.getPersistentData().putInt(HeatData.SELECTED_COOL_TICK_TAG, 0);
                return;
            }

            int tick = incrementTick(holder, HeatData.SELECTED_COOL_TICK_TAG);
            if (tick >= HeatData.SELECTED_COOL_INTERVAL_TICKS) {
                holder.getPersistentData().putInt(HeatData.SELECTED_COOL_TICK_TAG, 0);
                reduceHeat(holder, 1);
            }

            return;
        }

        reduceStoredCooling(holder);
    }

    private static void reduceDroppedCooling(LivingEntity holder) {
        int tick = incrementTick(holder, HeatData.STORED_COOL_TICK_TAG);
        if (tick >= HeatData.STORED_COOL_INTERVAL_TICKS) {
            holder.getPersistentData().putInt(HeatData.STORED_COOL_TICK_TAG, 0);
            reduceHeat(holder, 2);
        }
    }

    private static void reduceStoredCooling(LivingEntity holder) {
        int tick = incrementTick(holder, HeatData.STORED_COOL_TICK_TAG);
        if (tick >= HeatData.STORED_COOL_INTERVAL_TICKS) {
            holder.getPersistentData().putInt(HeatData.STORED_COOL_TICK_TAG, 0);
            reduceHeat(holder, 1);
        }
    }

    private static void coolFromRain(LivingEntity holder) {
        if (!holder.level().isRainingAt(holder.blockPosition())) {
            holder.getPersistentData().putInt(HeatData.RAIN_COOL_TICK_TAG, 0);
            return;
        }

        int tick = incrementTick(holder, HeatData.RAIN_COOL_TICK_TAG);
        if (tick >= HeatData.RAIN_COOL_INTERVAL_TICKS) {
            holder.getPersistentData().putInt(HeatData.RAIN_COOL_TICK_TAG, 0);
            reduceHeat(holder, HeatData.RAIN_COOL_AMOUNT);
        }
    }

    private static void applyStageEffects(LivingEntity holder) {
        int heat = getHeat(holder);
        HeatStage stage = getEffectiveStage(holder);

        if (stage.ordinal() >= HeatStage.OVERHEATED.ordinal()) {
            holder.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                    RelicScalingHandler.getEffectiveNegativeEffectDuration(holder, 60), 0, false, false, true));
        } else {
            holder.removeEffect(MobEffects.WEAKNESS);
        }

        spawnHeatParticles(holder, stage);
        playHeatingSound(holder, stage);

        if (isImmuneToRelicOverheat(holder)) {
            return;
        }

        if (stage.ordinal() >= HeatStage.COMBUSTION.ordinal()
                && holder.tickCount % HeatData.COMBUSTION_REFRESH_INTERVAL_TICKS == 0) {
            holder.setSecondsOnFire(HeatData.COMBUSTION_SECONDS);
        }

        if (stage == HeatStage.INTERNAL_COMBUSTION) {
            int tick = incrementTick(holder, HeatData.THERMAL_TICK_TAG);
            if (tick >= HeatData.THERMAL_DAMAGE_INTERVAL_TICKS) {
                holder.getPersistentData().putInt(HeatData.THERMAL_TICK_TAG, 0);
                holder.hurt(HeatDamageSource.source(holder),
                        Math.max(0.25F, RelicScalingHandler.getEffectivePenalty(holder, HeatData.THERMAL_DAMAGE)));
            }
        } else {
            holder.getPersistentData().putInt(HeatData.THERMAL_TICK_TAG, 0);
        }
    }

    private static void removeHeatPenalties(LivingEntity holder) {
        holder.removeEffect(MobEffects.WEAKNESS);
    }

    private static void spawnHeatParticles(LivingEntity holder, HeatStage stage) {
        if (!(holder.level() instanceof ServerLevel serverLevel) || !isHoldingRelic(holder)) {
            return;
        }

        if (stage == HeatStage.WARMING && holder.tickCount % 20 == 0) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, holder.getX(), holder.getY() + 1.0D, holder.getZ(), 2, 0.35D, 0.35D, 0.35D, 0.01D);
        } else if (stage == HeatStage.OVERHEATED && holder.tickCount % 10 == 0) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, holder.getX(), holder.getY() + 1.0D, holder.getZ(), 4, 0.45D, 0.45D, 0.45D, 0.01D);
            serverLevel.sendParticles(ParticleTypes.FLAME, holder.getX(), holder.getY() + 1.0D, holder.getZ(), 1, 0.35D, 0.35D, 0.35D, 0.01D);
        } else if (stage.ordinal() >= HeatStage.COMBUSTION.ordinal() && holder.tickCount % 6 == 0) {
            serverLevel.sendParticles(ParticleTypes.FLAME, holder.getX(), holder.getY() + 1.0D, holder.getZ(), 3, 0.45D, 0.55D, 0.45D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.LAVA, holder.getX(), holder.getY() + 0.8D, holder.getZ(), 1, 0.25D, 0.25D, 0.25D, 0.02D);
        }
    }

    private static void playHeatingSound(LivingEntity holder, HeatStage stage) {
        if (stage.ordinal() < HeatStage.WARMING.ordinal()) {
            holder.getPersistentData().putInt(HeatData.SOUND_TICK_TAG, 0);
            return;
        }

        int tick = incrementTick(holder, HeatData.SOUND_TICK_TAG);
        if (tick < 80) {
            return;
        }

        holder.getPersistentData().putInt(HeatData.SOUND_TICK_TAG, 0);
        holder.level().playSound(null, holder.getX(), holder.getY(), holder.getZ(),
                SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.PLAYERS, 0.35F, 1.4F);
    }

    private static void playWaterCoolingEffects(LivingEntity holder) {
        if (holder.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD, holder.getX(), holder.getY() + 1.0D, holder.getZ(), 25, 0.7D, 0.7D, 0.7D, 0.05D);
        }

        holder.level().playSound(null, holder.getX(), holder.getY(), holder.getZ(),
                SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.75F, 1.2F);
    }

    private static int incrementTick(LivingEntity holder, String tag) {
        CompoundTag data = holder.getPersistentData();
        int tick = data.getInt(tag) + 1;
        data.putInt(tag, tick);
        return tick;
    }

    private static void syncIfNeeded(LivingEntity holder, boolean force) {
        if (!(holder instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag data = holder.getPersistentData();
        int heat = getHeat(holder);
        int lastSynced = data.getInt(HeatData.LAST_SYNC_HEAT_TAG);

        if (force || heat != lastSynced) {
            data.putInt(HeatData.LAST_SYNC_HEAT_TAG, heat);
            HeatNetworkHandler.sync(player, heat);
        }
    }

    private static int clamp(int heat) {
        return Math.max(HeatData.MIN_HEAT, Math.min(HeatData.MAX_HEAT, heat));
    }

    private static HeatStage getEffectiveStage(LivingEntity holder) {
        int heat = getHeat(holder);
        if (heat >= RelicScalingHandler.getEffectiveThreshold(holder, HeatData.INTERNAL_COMBUSTION_MIN)) {
            return HeatStage.INTERNAL_COMBUSTION;
        }

        if (heat >= RelicScalingHandler.getEffectiveThreshold(holder, HeatData.COMBUSTION_MIN)) {
            return HeatStage.COMBUSTION;
        }

        if (heat >= RelicScalingHandler.getEffectiveThreshold(holder, HeatData.OVERHEATED_MIN)) {
            return HeatStage.OVERHEATED;
        }

        if (heat >= HeatData.WARMING_MIN) {
            return HeatStage.WARMING;
        }

        return HeatStage.STABLE;
    }
}
