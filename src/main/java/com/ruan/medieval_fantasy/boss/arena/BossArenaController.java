package com.ruan.medieval_fantasy.boss.arena;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public abstract class BossArenaController {
    protected final ServerLevel level;
    protected final BlockPos center;
    protected ArenaState state = ArenaState.DORMANT;

    protected BossArenaController(ServerLevel level, BlockPos center) {
        this.level = level;
        this.center = center;
    }

    public ArenaState getState() {
        return state;
    }

    public abstract void setState(ArenaState state);
}
