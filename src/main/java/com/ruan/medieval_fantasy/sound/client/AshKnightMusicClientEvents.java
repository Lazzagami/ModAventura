package com.ruan.medieval_fantasy.sound.client;

import com.ruan.medieval_fantasy.ExampleMod;
import com.ruan.medieval_fantasy.entity.custom.CavaleiroDasCinzas;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public final class AshKnightMusicClientEvents {

    private static final double MAX_MUSIC_DISTANCE_SQR = 160.0D * 160.0D;
    private static AshKnightBattleMusicSound currentMusic;
    private static int rescanCooldown;

    private AshKnightMusicClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            stopCurrent(minecraft);
            return;
        }

        if (currentMusic != null && currentMusic.isStopped()) {
            currentMusic = null;
        }

        if (rescanCooldown > 0) {
            rescanCooldown--;
            return;
        }
        rescanCooldown = currentMusic == null ? 5 : 20;

        CavaleiroDasCinzas activeBoss = findActiveBoss(minecraft);
        if (activeBoss == null) {
            stopCurrent(minecraft);
            return;
        }

        if (currentMusic == null) {
            currentMusic = new AshKnightBattleMusicSound(activeBoss);
            minecraft.getSoundManager().play(currentMusic);
        } else {
            currentMusic.setBoss(activeBoss);
        }
    }

    private static CavaleiroDasCinzas findActiveBoss(Minecraft minecraft) {
        CavaleiroDasCinzas closest = null;
        double closestDistance = MAX_MUSIC_DISTANCE_SQR;

        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof CavaleiroDasCinzas boss) || !boss.shouldPlayCombatMusicClient()) {
                continue;
            }

            double distance = boss.distanceToSqr(minecraft.player);
            if (distance < closestDistance) {
                closest = boss;
                closestDistance = distance;
            }
        }

        return closest;
    }

    private static void stopCurrent(Minecraft minecraft) {
        if (currentMusic == null) {
            return;
        }

        minecraft.getSoundManager().stop(currentMusic);
        currentMusic = null;
        rescanCooldown = 0;
    }
}
