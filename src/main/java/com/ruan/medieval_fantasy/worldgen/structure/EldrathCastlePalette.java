package com.ruan.medieval_fantasy.worldgen.structure;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class EldrathCastlePalette {

    private EldrathCastlePalette() {
    }

    public static BlockState floor(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 34) return Blocks.STONE_BRICKS.defaultBlockState();
        if (roll < 58) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        if (roll < 70) return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if (roll < 82) return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        if (roll < 92) return Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        if (roll < 98) return Blocks.SMOOTH_BASALT.defaultBlockState();
        return Blocks.MAGMA_BLOCK.defaultBlockState();
    }

    public static BlockState wall(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 38) return Blocks.STONE_BRICKS.defaultBlockState();
        if (roll < 62) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        if (roll < 74) return Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        if (roll < 86) return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if (roll < 94) return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        return Blocks.BASALT.defaultBlockState();
    }

    public static BlockState scorched(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 45) return Blocks.BLACKSTONE.defaultBlockState();
        if (roll < 75) return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if (roll < 90) return Blocks.BASALT.defaultBlockState();
        return Blocks.SMOOTH_BASALT.defaultBlockState();
    }

    public static BlockState metal(RandomSource random) {
        return random.nextBoolean() ? Blocks.IRON_BARS.defaultBlockState() : Blocks.CHAIN.defaultBlockState();
    }

    public static boolean isForbidden(Block block) {
        return block.defaultBlockState().ignitedByLava()
                || block == Blocks.OAK_PLANKS
                || block == Blocks.SPRUCE_PLANKS
                || block == Blocks.BIRCH_PLANKS
                || block == Blocks.JUNGLE_PLANKS
                || block == Blocks.ACACIA_PLANKS
                || block == Blocks.DARK_OAK_PLANKS
                || block == Blocks.MANGROVE_PLANKS
                || block == Blocks.CHERRY_PLANKS
                || block == Blocks.BAMBOO_PLANKS;
    }
}
