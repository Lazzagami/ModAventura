package com.ruan.medieval_fantasy.worldgen.structure;

import com.ruan.medieval_fantasy.entity.ModEntities;
import com.ruan.medieval_fantasy.entity.custom.CavaleiroDasCinzas;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;

public class EldrathCastleBuilder {
    public static final int TOTAL_SIZE = 142;
    public static final int COURTYARD_SIZE = 74;
    public static final int WALL_THICKNESS = 5;
    public static final int WALL_HEIGHT = 23;
    public static final int FREE_AIR_HEIGHT = 28;
    private static final int OUTER_TOWER_RADIUS = 10;

    private final ServerLevel level;
    private final RandomSource random;
    private final BlockPos center;
    private final int baseY;

    public EldrathCastleBuilder(ServerLevel level, BlockPos center) {
        this.level = level;
        this.random = level.getRandom();
        this.center = center;
        this.baseY = center.getY();
    }

    public BuildResult build(boolean spawnBoss) {
        clearMainCourt();
        buildFoundationAndFloor();
        buildOuterWalls();
        buildCornerTowers();
        buildMainGate();
        buildApproachCorridor();
        buildThroneHall();
        buildSideCorridors();
        buildOuterCastleSilhouette();
        buildThroneHallFacade();
        buildCourtyardDetails();
        buildSideRuins();

        BlockPos entrance = center.offset(0, 2, -TOTAL_SIZE / 2 - 18);
        BlockPos bossPos = center.offset(0, 1, COURTYARD_SIZE / 2 - 10);
        if (spawnBoss) {
            spawnBoss(bossPos);
        }
        return new BuildResult(center, entrance, bossPos);
    }

    private void clearMainCourt() {
        int radius = COURTYARD_SIZE / 2 + 8;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 1; y <= FREE_AIR_HEIGHT; y++) {
                    pos.set(center.getX() + x, baseY + y, center.getZ() + z);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private void buildFoundationAndFloor() {
        int radius = TOTAL_SIZE / 2;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                pos.set(center.getX() + x, baseY - 1, center.getZ() + z);
                level.setBlock(pos, EldrathCastlePalette.scorched(random), 2);

                pos.set(center.getX() + x, baseY, center.getZ() + z);
                level.setBlock(pos, EldrathCastlePalette.floor(random), 2);
            }
        }
    }

    private void buildOuterWalls() {
        int radius = TOTAL_SIZE / 2;
        for (int x = -radius; x <= radius; x++) {
            for (int t = 0; t < WALL_THICKNESS; t++) {
                wallColumn(x, -radius + t, WALL_HEIGHT, isGateGap(x));
                wallColumn(x, radius - t, WALL_HEIGHT - random.nextInt(5), false);
            }
        }

        for (int z = -radius; z <= radius; z++) {
            for (int t = 0; t < WALL_THICKNESS; t++) {
                wallColumn(-radius + t, z, WALL_HEIGHT - random.nextInt(4), false);
                wallColumn(radius - t, z, WALL_HEIGHT - random.nextInt(7), false);
            }
        }

        addBrokenWallGaps();
    }

    private boolean isGateGap(int x) {
        return Math.abs(x) <= 9;
    }

    private void wallColumn(int x, int z, int height, boolean gateGap) {
        if (gateGap) {
            return;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int brokenTop = random.nextInt(100) < 12 ? random.nextInt(7) : 0;
        for (int y = 1; y <= height - brokenTop; y++) {
            pos.set(center.getX() + x, baseY + y, center.getZ() + z);
            level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
        }
    }

    private void addBrokenWallGaps() {
        carveRuinBreach(-TOTAL_SIZE / 2 + 2, -25, 18, 11);
        carveRuinBreach(TOTAL_SIZE / 2 - 2, 32, 16, 13);
        carveRuinBreach(-34, TOTAL_SIZE / 2 - 2, 20, 10);
    }

    private void carveRuinBreach(int fixed, int start, int width, int height) {
        boolean verticalWall = Math.abs(fixed) > TOTAL_SIZE / 3;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < width; i++) {
            for (int y = 3; y < height; y++) {
                for (int t = -1; t <= 1; t++) {
                    int x = verticalWall ? fixed : start + i;
                    int z = verticalWall ? start + i : fixed;
                    pos.set(center.getX() + x + (verticalWall ? t : 0), baseY + y, center.getZ() + z + (verticalWall ? 0 : t));
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    private void buildCornerTowers() {
        int r = TOTAL_SIZE / 2 - 9;
        tower(center.offset(-r, 0, -r), 28, 8, 0);
        tower(center.offset(r, 0, -r), 17, 8, 9);
        tower(center.offset(-r, 0, r), 22, 8, 4);
        tower(center.offset(r, 0, r), 31, 8, 14);
    }

    private void tower(BlockPos towerCenter, int height, int radius, int brokenSide) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 1; y <= height; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    int dist = Math.max(Math.abs(x), Math.abs(z));
                    boolean shell = dist >= radius - 1;
                    boolean broken = y > height - brokenSide && x > 1 && z > 0;
                    if (shell && !broken) {
                        pos.set(towerCenter.getX() + x, baseY + y, towerCenter.getZ() + z);
                        level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                    } else if (!shell && y < height - 2) {
                        pos.set(towerCenter.getX() + x, baseY + y, towerCenter.getZ() + z);
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private void buildMainGate() {
        int north = -TOTAL_SIZE / 2;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -15; x <= 15; x++) {
            for (int z = north; z <= north + WALL_THICKNESS + 1; z++) {
                for (int y = 1; y <= 24; y++) {
                    boolean arch = Math.abs(x) <= 9 && y <= 13;
                    if (!arch) {
                        pos.set(center.getX() + x, baseY + y, center.getZ() + z);
                        level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                    }
                }
            }
        }

        BlockState bars = Blocks.IRON_BARS.defaultBlockState()
                .setValue(IronBarsBlock.NORTH, true)
                .setValue(IronBarsBlock.SOUTH, true);
        for (int x = -8; x <= 8; x++) {
            for (int y = 1; y <= 11; y++) {
                pos.set(center.getX() + x, baseY + y, center.getZ() + north + 2);
                level.setBlock(pos, bars, 3);
            }
        }

        for (int x : new int[]{-13, 13}) {
            for (int y = 13; y <= 22; y++) {
                pos.set(center.getX() + x, baseY + y, center.getZ() + north + 3);
                level.setBlock(pos, Blocks.CHAIN.defaultBlockState(), 3);
            }
        }
    }

    private void buildApproachCorridor() {
        int startZ = -TOTAL_SIZE / 2 - 38;
        int endZ = -TOTAL_SIZE / 2 + 2;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = startZ; z <= endZ; z++) {
            for (int x = -11; x <= 11; x++) {
                pos.set(center.getX() + x, baseY, center.getZ() + z);
                level.setBlock(pos, EldrathCastlePalette.floor(random), 2);
                if (Math.abs(x) >= 10) {
                    for (int y = 1; y <= 5; y++) {
                        pos.set(center.getX() + x, baseY + y, center.getZ() + z);
                        level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                    }
                }
            }
        }
    }

    private void buildSideCorridors() {
        buildSideCorridor(-1);
        buildSideCorridor(1);
        buildCrossPassage(-52);
        buildCrossPassage(56);
    }

    private void buildSideCorridor(int side) {
        int innerX = side * (COURTYARD_SIZE / 2 + 8);
        int outerX = side * (TOTAL_SIZE / 2 - 9);
        int minX = Math.min(innerX, outerX);
        int maxX = Math.max(innerX, outerX);
        int startZ = -TOTAL_SIZE / 2 + 16;
        int endZ = TOTAL_SIZE / 2 - 18;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                pos.set(center.getX() + x, baseY, center.getZ() + z);
                level.setBlock(pos, EldrathCastlePalette.floor(random), 2);

                if (Math.abs(x - innerX) <= 1 || Math.abs(x - outerX) <= 1) {
                    int broken = random.nextInt(100) < 18 ? random.nextInt(4) : 0;
                    for (int y = 1; y <= 5 - broken; y++) {
                        pos.set(center.getX() + x, baseY + y, center.getZ() + z);
                        level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                    }
                }

                if ((z - startZ) % 9 == 0 && (x == innerX + side * 3 || x == outerX - side * 3)) {
                    buildPillar(x, z, 10 + random.nextInt(5));
                }
            }
        }

        for (int z = startZ + 4; z <= endZ - 4; z += 9) {
            buildBrokenArch(innerX + side * 3, z, outerX - side * 3, 9 + random.nextInt(3));
        }
    }

    private void buildCrossPassage(int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -TOTAL_SIZE / 2 + 12; x <= TOTAL_SIZE / 2 - 12; x++) {
            if (Math.abs(x) < COURTYARD_SIZE / 2 + 5) {
                continue;
            }
            for (int dz = -4; dz <= 4; dz++) {
                pos.set(center.getX() + x, baseY, center.getZ() + z + dz);
                level.setBlock(pos, EldrathCastlePalette.floor(random), 2);
                if (Math.abs(dz) == 4) {
                    for (int y = 1; y <= 4; y++) {
                        pos.set(center.getX() + x, baseY + y, center.getZ() + z + dz);
                        level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                    }
                }
            }
        }
    }

    private void buildBrokenArch(int xA, int z, int xB, int height) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = Math.min(xA, xB);
        int maxX = Math.max(xA, xB);
        for (int x = minX; x <= maxX; x++) {
            boolean nearSupport = Math.abs(x - minX) <= 1 || Math.abs(x - maxX) <= 1;
            boolean archTop = !nearSupport && random.nextInt(100) > 22;
            if (nearSupport) {
                for (int y = 1; y <= height; y++) {
                    pos.set(center.getX() + x, baseY + y, center.getZ() + z);
                    level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                }
            } else if (archTop) {
                for (int y = height - 1; y <= height + 1; y++) {
                    pos.set(center.getX() + x, baseY + y, center.getZ() + z);
                    level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                }
            }
        }
    }

    private void buildOuterCastleSilhouette() {
        int north = -TOTAL_SIZE / 2;
        int south = TOTAL_SIZE / 2;
        int east = TOTAL_SIZE / 2;
        int west = -TOTAL_SIZE / 2;

        buildOuterGateTowers(north);
        buildOuterWatchTower(west - 18, -42, 26, 2);
        buildOuterWatchTower(east + 18, -30, 22, 7);
        buildOuterWatchTower(west - 18, 38, 18, 10);
        buildOuterWatchTower(east + 18, 44, 30, 4);
        buildOuterWatchTower(-42, south + 18, 24, 5);
        buildOuterWatchTower(42, south + 18, 28, 12);

        buildOuterCurtainWall(west - 18, -42, west, -42, 9);
        buildOuterCurtainWall(east, -30, east + 18, -30, 8);
        buildOuterCurtainWall(west - 18, 38, west, 38, 7);
        buildOuterCurtainWall(east, 44, east + 18, 44, 10);
        buildOuterCurtainWall(-42, south, -42, south + 18, 8);
        buildOuterCurtainWall(42, south, 42, south + 18, 8);

        buildBurnedPlaza(-42, north - 22, 22, 16);
        buildBurnedPlaza(42, north - 22, 22, 16);
    }

    private void buildOuterGateTowers(int north) {
        buildOuterWatchTower(-32, north - 18, 31, 3);
        buildOuterWatchTower(32, north - 18, 27, 11);
        buildOuterCurtainWall(-32, north - 18, -14, north, 12);
        buildOuterCurtainWall(14, north, 32, north - 18, 12);
        buildBurnedPlaza(0, north - 22, 30, 20);
    }

    private void buildOuterWatchTower(int x, int z, int height, int brokenSide) {
        BlockPos towerCenter = center.offset(x, 0, z);
        buildTowerFootprint(x, z, OUTER_TOWER_RADIUS + 3);
        tower(towerCenter, height, OUTER_TOWER_RADIUS, brokenSide);
        addTowerBattlements(towerCenter, height, OUTER_TOWER_RADIUS);
        addTowerChains(towerCenter, height, OUTER_TOWER_RADIUS);
    }

    private void buildTowerFootprint(int x, int z, int radius) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) <= radius) {
                    pos.set(center.getX() + x + dx, baseY - 1, center.getZ() + z + dz);
                    level.setBlock(pos, EldrathCastlePalette.scorched(random), 2);
                    pos.set(center.getX() + x + dx, baseY, center.getZ() + z + dz);
                    level.setBlock(pos, EldrathCastlePalette.floor(random), 2);
                }
            }
        }
    }

    private void addTowerBattlements(BlockPos towerCenter, int height, int radius) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                int dist = Math.max(Math.abs(x), Math.abs(z));
                if (dist == radius && (Math.abs(x + z) % 3 == 0) && random.nextInt(100) > 18) {
                    for (int y = height + 1; y <= height + 3; y++) {
                        pos.set(towerCenter.getX() + x, baseY + y, towerCenter.getZ() + z);
                        level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                    }
                }
            }
        }
    }

    private void addTowerChains(BlockPos towerCenter, int height, int radius) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int[] anchor : new int[][]{{radius, 0}, {-radius, 0}, {0, radius}, {0, -radius}}) {
            if (random.nextBoolean()) {
                int length = 6 + random.nextInt(7);
                for (int y = height - 2; y >= Math.max(3, height - length); y--) {
                    pos.set(towerCenter.getX() + anchor[0], baseY + y, towerCenter.getZ() + anchor[1]);
                    level.setBlock(pos, Blocks.CHAIN.defaultBlockState(), 3);
                }
            }
        }
    }

    private void buildOuterCurtainWall(int x1, int z1, int x2, int z2, int height) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(z2 - z1));
        if (steps == 0) {
            return;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / steps;
            int z = z1 + (z2 - z1) * i / steps;
            for (int t = -1; t <= 1; t++) {
                boolean mostlyHorizontal = Math.abs(x2 - x1) >= Math.abs(z2 - z1);
                int px = x + (mostlyHorizontal ? 0 : t);
                int pz = z + (mostlyHorizontal ? t : 0);
                pos.set(center.getX() + px, baseY, center.getZ() + pz);
                level.setBlock(pos, EldrathCastlePalette.floor(random), 2);
                for (int y = 1; y <= height; y++) {
                    if (random.nextInt(100) > 8 + y) {
                        pos.set(center.getX() + px, baseY + y, center.getZ() + pz);
                        level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                    }
                }
            }
        }
    }

    private void buildBurnedPlaza(int originX, int originZ, int width, int length) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -width / 2; x <= width / 2; x++) {
            for (int z = -length / 2; z <= length / 2; z++) {
                pos.set(center.getX() + originX + x, baseY - 1, center.getZ() + originZ + z);
                level.setBlock(pos, EldrathCastlePalette.scorched(random), 2);
                pos.set(center.getX() + originX + x, baseY, center.getZ() + originZ + z);
                level.setBlock(pos, random.nextInt(100) < 20 ? EldrathCastlePalette.scorched(random) : EldrathCastlePalette.floor(random), 2);
            }
        }
    }

    private void buildThroneHall() {
        int startZ = COURTYARD_SIZE / 2 + 4;
        int endZ = TOTAL_SIZE / 2 - 10;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = startZ; z <= endZ; z++) {
            for (int x = -24; x <= 24; x++) {
                boolean sideWall = Math.abs(x) >= 22;
                boolean backWall = z >= endZ - 2;
                if (sideWall || backWall) {
                    int height = backWall ? 18 : 14 + random.nextInt(8);
                    for (int y = 1; y <= height; y++) {
                        if (random.nextInt(100) > 10) {
                            pos.set(center.getX() + x, baseY + y, center.getZ() + z);
                            level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                        }
                    }
                }
            }
        }

        buildPillar(-17, startZ + 8, 15);
        buildPillar(17, startZ + 8, 13);
        buildPillar(-17, startZ + 24, 11);
        buildPillar(17, startZ + 24, 16);
        buildStoneThrone(0, endZ - 8);
    }

    private void buildThroneHallFacade() {
        int z = TOTAL_SIZE / 2 - 6;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -34; x <= 34; x++) {
            for (int y = 1; y <= 26; y++) {
                boolean doorway = Math.abs(x) <= 8 && y <= 13;
                boolean brokenTop = y > 20 && random.nextInt(100) < 32;
                boolean archedTop = Math.abs(x) <= 12 && y >= 14 && y <= 18;
                if (!doorway && !brokenTop && !archedTop) {
                    pos.set(center.getX() + x, baseY + y, center.getZ() + z);
                    level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                }
            }
        }

        for (int x : new int[]{-31, -23, 23, 31}) {
            buildPillar(x, z - 3, 23 + random.nextInt(5));
        }

        for (int x = -6; x <= 6; x += 6) {
            for (int y = 13; y <= 23; y++) {
                pos.set(center.getX() + x, baseY + y, center.getZ() + z - 1);
                level.setBlock(pos, Blocks.CHAIN.defaultBlockState(), 3);
            }
        }

        buildBrokenArch(-18, z - 5, 18, 17);
        buildFutureThroneRoomFootprint();
    }

    private void buildFutureThroneRoomFootprint() {
        int startZ = TOTAL_SIZE / 2 - 2;
        int endZ = TOTAL_SIZE / 2 + 36;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = startZ; z <= endZ; z++) {
            for (int x = -32; x <= 32; x++) {
                pos.set(center.getX() + x, baseY - 1, center.getZ() + z);
                level.setBlock(pos, EldrathCastlePalette.scorched(random), 2);
                pos.set(center.getX() + x, baseY, center.getZ() + z);
                level.setBlock(pos, EldrathCastlePalette.floor(random), 2);
                boolean sideWall = Math.abs(x) >= 29;
                boolean backWall = z >= endZ - 2;
                if (sideWall || backWall) {
                    int height = backWall ? 15 : 9 + random.nextInt(7);
                    for (int y = 1; y <= height; y++) {
                        if (random.nextInt(100) > 20) {
                            pos.set(center.getX() + x, baseY + y, center.getZ() + z);
                            level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                        }
                    }
                }
            }
        }

        buildPillar(-22, startZ + 12, 13);
        buildPillar(22, startZ + 12, 13);
        buildPillar(-22, startZ + 27, 10);
        buildPillar(22, startZ + 27, 16);
    }

    private void buildPillar(int x, int z, int height) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 1; y <= height; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    pos.set(center.getX() + x + dx, baseY + y, center.getZ() + z + dz);
                    level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                }
            }
        }
    }

    private void buildStoneThrone(int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                pos.set(center.getX() + x + dx, baseY + 1, center.getZ() + z + dz);
                level.setBlock(pos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
            }
        }
        for (int y = 2; y <= 6; y++) {
            for (int dx = -3; dx <= 3; dx++) {
                pos.set(center.getX() + x + dx, baseY + y, center.getZ() + z + 2);
                level.setBlock(pos, Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
            }
        }
    }

    private void buildCourtyardDetails() {
        for (int i = 0; i < 60; i++) {
            int x = random.nextInt(COURTYARD_SIZE) - COURTYARD_SIZE / 2;
            int z = random.nextInt(COURTYARD_SIZE) - COURTYARD_SIZE / 2;
            if (Math.abs(x) < 12 && Math.abs(z) < 12) {
                continue;
            }
            BlockPos pos = center.offset(x, 1, z);
            int roll = random.nextInt(100);
            if (roll < 36) {
                level.setBlock(pos, Blocks.IRON_BARS.defaultBlockState(), 3);
            } else if (roll < 58) {
                level.setBlock(pos, Blocks.CHAIN.defaultBlockState(), 3);
            } else if (roll < 74) {
                level.setBlock(pos.below(), Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
            } else if (roll < 88) {
                level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
            } else {
                level.setBlock(pos, Blocks.BLACKSTONE_WALL.defaultBlockState(), 3);
            }
        }

        for (int i = 0; i < 18; i++) {
            int x = random.nextInt(COURTYARD_SIZE + 8) - (COURTYARD_SIZE + 8) / 2;
            int z = random.nextInt(COURTYARD_SIZE + 8) - (COURTYARD_SIZE + 8) / 2;
            buildCrater(x, z, 2 + random.nextInt(3));
        }
    }

    private void buildCrater(int x, int z, int radius) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius && random.nextBoolean()) {
                    pos.set(center.getX() + x + dx, baseY, center.getZ() + z + dz);
                    level.setBlock(pos, EldrathCastlePalette.scorched(random), 2);
                }
            }
        }
    }

    private void buildSideRuins() {
        ruinRoom(-48, -18, 22, 24);
        ruinRoom(47, -12, 20, 22);
        ruinRoom(-42, 38, 28, 18);
        ruinRoom(42, 34, 24, 20);
    }

    private void ruinRoom(int originX, int originZ, int width, int length) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                boolean edge = x == 0 || z == 0 || x == width - 1 || z == length - 1;
                if (edge) {
                    int height = 3 + random.nextInt(8);
                    for (int y = 1; y <= height; y++) {
                        if (random.nextInt(100) > 18) {
                            pos.set(center.getX() + originX + x, baseY + y, center.getZ() + originZ + z);
                            level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
                        }
                    }
                }
            }
        }
    }

    private void spawnBoss(BlockPos bossPos) {
        CavaleiroDasCinzas boss = ModEntities.CAVALEIRO_DAS_CINZAS.get().create(level);
        if (boss == null) {
            return;
        }
        boss.moveTo(bossPos.getX() + 0.5D, bossPos.getY(), bossPos.getZ() + 0.5D, 180.0F, 0.0F);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(bossPos), MobSpawnType.STRUCTURE, null, null);
        boss.setPersistenceRequired();
        level.addFreshEntity(boss);
    }

    public record BuildResult(BlockPos center, BlockPos entrance, BlockPos bossPosition) {
    }
}
