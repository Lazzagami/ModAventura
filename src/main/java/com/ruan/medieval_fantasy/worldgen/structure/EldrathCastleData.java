package com.ruan.medieval_fantasy.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class EldrathCastleData extends SavedData {
    private static final String DATA_NAME = "medieval_fantasy_eldrath_castle";

    private boolean generated;
    private BlockPos center = BlockPos.ZERO;
    private BlockPos entrance = BlockPos.ZERO;
    private BlockPos bossPosition = BlockPos.ZERO;

    public static EldrathCastleData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(EldrathCastleData::load, EldrathCastleData::new, DATA_NAME);
    }

    public static EldrathCastleData load(CompoundTag tag) {
        EldrathCastleData data = new EldrathCastleData();
        data.generated = tag.getBoolean("Generated");
        data.center = readPos(tag, "Center");
        data.entrance = readPos(tag, "Entrance");
        data.bossPosition = readPos(tag, "BossPosition");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("Generated", generated);
        writePos(tag, "Center", center);
        writePos(tag, "Entrance", entrance);
        writePos(tag, "BossPosition", bossPosition);
        return tag;
    }

    public boolean isGenerated() {
        return generated;
    }

    public BlockPos getCenter() {
        return center;
    }

    public BlockPos getEntrance() {
        return entrance;
    }

    public BlockPos getBossPosition() {
        return bossPosition;
    }

    public void setCastle(BlockPos center, BlockPos entrance, BlockPos bossPosition) {
        this.generated = true;
        this.center = center;
        this.entrance = entrance;
        this.bossPosition = bossPosition;
        setDirty();
    }

    private static void writePos(CompoundTag tag, String key, BlockPos pos) {
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("X", pos.getX());
        posTag.putInt("Y", pos.getY());
        posTag.putInt("Z", pos.getZ());
        tag.put(key, posTag);
    }

    private static BlockPos readPos(CompoundTag tag, String key) {
        if (!tag.contains(key)) {
            return BlockPos.ZERO;
        }
        CompoundTag posTag = tag.getCompound(key);
        return new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"));
    }
}
