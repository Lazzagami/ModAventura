package com.ruan.medieval_fantasy.combat.damage;

import com.ruan.medieval_fantasy.ExampleMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class DamageNumber {

    private static final String TAG_DAMAGE_NUMBER = "medieval_fantasy_damage_number";
    private static final String TAG_AGE = "medieval_fantasy_damage_number_age";
    private static final String TAG_RISE_SPEED = "medieval_fantasy_damage_number_rise_speed";
    private static final String TAG_DRIFT_X = "medieval_fantasy_damage_number_drift_x";
    private static final String TAG_DRIFT_Z = "medieval_fantasy_damage_number_drift_z";
    private static final String TAG_LIFE_TICKS = "medieval_fantasy_damage_number_life_ticks";
    private static final int DEFAULT_LIFE_TICKS = 25;
    private static final double DEFAULT_RISE_SPEED = 0.035D;

    public static void spawn(ServerLevel level, Entity target, Component text) {
        spawn(level, target, text, 0.35D, DEFAULT_LIFE_TICKS, DEFAULT_RISE_SPEED);
    }

    public static void spawnBurn(ServerLevel level, Entity target, Component text) {
        spawn(level, target, text, 1.15D, 38, 0.052D);
    }

    private static void spawn(ServerLevel level, Entity target, Component text, double heightOffset, int lifeTicks, double riseSpeed) {
        if (target == null || target.isRemoved()) {
            return;
        }

        ArmorStand display = new ArmorStand(level, target.getX(), target.getY() + target.getBbHeight() + heightOffset, target.getZ());

        CompoundTag armorStandData = new CompoundTag();
        display.saveWithoutId(armorStandData);
        armorStandData.putBoolean("Invisible", true);
        armorStandData.putBoolean("Invulnerable", true);
        armorStandData.putBoolean("NoGravity", true);
        armorStandData.putBoolean("Marker", true);
        armorStandData.putBoolean("Small", true);
        armorStandData.putBoolean("Silent", true);
        display.load(armorStandData);

        double offsetX = (level.random.nextDouble() - 0.5D) * 1.1D;
        double offsetZ = (level.random.nextDouble() - 0.5D) * 1.1D;
        double driftX = offsetX * 0.006D;
        double driftZ = offsetZ * 0.006D;
        display.setPos(target.getX() + offsetX, target.getY() + target.getBbHeight() + heightOffset, target.getZ() + offsetZ);
        display.setCustomName(text);
        display.setCustomNameVisible(true);
        display.getPersistentData().putBoolean(TAG_DAMAGE_NUMBER, true);
        display.getPersistentData().putInt(TAG_AGE, 0);
        display.getPersistentData().putInt(TAG_LIFE_TICKS, lifeTicks);
        display.getPersistentData().putDouble(TAG_RISE_SPEED, riseSpeed);
        display.getPersistentData().putDouble(TAG_DRIFT_X, driftX);
        display.getPersistentData().putDouble(TAG_DRIFT_Z, driftZ);

        level.addFreshEntity(display);
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
            if (!(entity instanceof ArmorStand) || entity.isRemoved()) {
                continue;
            }

            CompoundTag data;
            try {
                data = entity.getPersistentData();
            } catch (NullPointerException ignored) {
                continue;
            }

            if (!data.getBoolean(TAG_DAMAGE_NUMBER)) {
                continue;
            }

            int age = data.getInt(TAG_AGE) + 1;

            int lifeTicks = data.contains(TAG_LIFE_TICKS) ? data.getInt(TAG_LIFE_TICKS) : DEFAULT_LIFE_TICKS;

            if (age >= lifeTicks) {
                entity.discard();
                continue;
            }

            data.putInt(TAG_AGE, age);
            entity.setDeltaMovement(Vec3.ZERO);
            double riseSpeed = data.contains(TAG_RISE_SPEED) ? data.getDouble(TAG_RISE_SPEED) : DEFAULT_RISE_SPEED;
            entity.setPos(entity.getX() + data.getDouble(TAG_DRIFT_X), entity.getY() + riseSpeed, entity.getZ() + data.getDouble(TAG_DRIFT_Z));
        }
    }
}
