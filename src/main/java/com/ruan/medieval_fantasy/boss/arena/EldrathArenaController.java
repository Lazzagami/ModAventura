package com.ruan.medieval_fantasy.boss.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;

public class EldrathArenaController extends BossArenaController {

    public EldrathArenaController(ServerLevel level, BlockPos center) {
        super(level, center);
    }

    @Override
    public void setState(ArenaState state) {
        this.state = state;
        if (state == ArenaState.PHASE_TWO) {
            intensifyPerimeter(46, 20);
            level.playSound(null, center, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.HOSTILE, 1.8F, 0.65F);
        } else if (state == ArenaState.FINAL_PHASE) {
            intensifyPerimeter(40, 36);
            level.playSound(null, center, SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 2.0F, 0.55F);
        } else if (state == ArenaState.DEFEATED) {
            level.sendParticles(ParticleTypes.ASH, center.getX(), center.getY() + 3.0D, center.getZ(), 180, 24.0D, 4.0D, 24.0D, 0.03D);
        }
    }

    private void intensifyPerimeter(int radius, int points) {
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0D * i) / points;
            int x = center.getX() + (int) Math.round(Math.cos(angle) * radius);
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * radius);
            BlockPos pos = findSurface(new BlockPos(x, center.getY() + 8, z));
            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
                level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                level.sendParticles(ParticleTypes.LARGE_SMOKE, x + 0.5D, pos.getY() + 0.2D, z + 0.5D, 8, 0.4D, 0.15D, 0.4D, 0.02D);
            }
        }
    }

    private BlockPos findSurface(BlockPos start) {
        BlockPos.MutableBlockPos pos = start.mutable();
        while (pos.getY() > level.getMinBuildHeight() + 4 && level.getBlockState(pos).isAir()) {
            pos.move(0, -1, 0);
        }
        return pos.above().immutable();
    }
}
