package com.ruan.medieval_fantasy.sound.client;

import com.ruan.medieval_fantasy.entity.custom.CavaleiroDasCinzas;
import com.ruan.medieval_fantasy.sound.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class AshKnightBattleMusicSound extends AbstractTickableSoundInstance {

    private CavaleiroDasCinzas boss;

    public AshKnightBattleMusicSound(CavaleiroDasCinzas boss) {
        super(ModSounds.ASH_KNIGHT_BATTLE.get(), SoundSource.MUSIC, RandomSource.create());
        this.boss = boss;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.72F;
        this.pitch = 1.0F;
        updatePosition();
    }

    @Override
    public void tick() {
        if (boss == null || !boss.shouldPlayCombatMusicClient()) {
            stop();
            return;
        }

        updatePosition();
    }

    public void setBoss(CavaleiroDasCinzas boss) {
        this.boss = boss;
        updatePosition();
    }

    private void updatePosition() {
        if (boss == null) {
            return;
        }

        this.x = boss.getX();
        this.y = boss.getY();
        this.z = boss.getZ();
    }
}
