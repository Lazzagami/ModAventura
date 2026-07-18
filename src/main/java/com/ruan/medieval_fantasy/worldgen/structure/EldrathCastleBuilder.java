package com.ruan.medieval_fantasy.worldgen.structure;

import com.ruan.medieval_fantasy.entity.ModEntities;
import com.ruan.medieval_fantasy.entity.custom.CavaleiroDasCinzas;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
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
    private static final int BUILD_CLEAR_RADIUS = TOTAL_SIZE / 2 + 48;
    private static final int BUILD_CLEAR_HEIGHT = 72;
    private static final int SAFETY_CLEAR_RADIUS = TOTAL_SIZE / 2 + 58;

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
        clearCastleFootprint();
        buildFoundationAndFloor();
        buildOuterWalls();
        buildWallwalkAndDefensiveDetails();
        buildCornerTowers();
        buildMainGate();
        buildApproachCorridor();
        buildThroneHall();
        buildSideCorridors();
        buildOuterCastleSilhouette();
        buildThroneHallFacade();
        buildExplorableInteriorRooms();
        buildCourtyardDetails();
        buildStorytellingSetPieces();
        buildSideRuins();
        removeUnsafeFlammableBlocks();

        BlockPos entrance = center.offset(0, 2, -TOTAL_SIZE / 2 - 18);
        BlockPos bossPos = center.offset(0, 1, COURTYARD_SIZE / 2 - 10);
        if (spawnBoss) {
            spawnBoss(bossPos);
        }
        return new BuildResult(center, entrance, bossPos);
    }

    private void clearCastleFootprint() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -BUILD_CLEAR_RADIUS; x <= BUILD_CLEAR_RADIUS; x++) {
            for (int z = -BUILD_CLEAR_RADIUS; z <= BUILD_CLEAR_RADIUS; z++) {
                boolean insideCastle = Math.abs(x) <= BUILD_CLEAR_RADIUS - 8 && Math.abs(z) <= BUILD_CLEAR_RADIUS - 8;
                boolean nearApproach = Math.abs(x) <= 20 && z < -TOTAL_SIZE / 2 && z >= -TOTAL_SIZE / 2 - 48;
                if (!insideCastle && !nearApproach) {
                    continue;
                }

                for (int y = 1; y <= BUILD_CLEAR_HEIGHT; y++) {
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

    private void buildWallwalkAndDefensiveDetails() {
        int radius = TOTAL_SIZE / 2;
        buildHorizontalWallwalk(-radius, Direction.NORTH);
        buildHorizontalWallwalk(radius, Direction.SOUTH);
        buildVerticalWallwalk(-radius, Direction.WEST);
        buildVerticalWallwalk(radius, Direction.EAST);

        for (int x = -radius + 12; x <= radius - 12; x += 12) {
            if (!isGateGap(x)) {
                buildButtress(x, -radius - 1, Direction.NORTH, 12 + random.nextInt(7));
            }
            buildButtress(x, radius + 1, Direction.SOUTH, 10 + random.nextInt(8));
        }

        for (int z = -radius + 14; z <= radius - 14; z += 12) {
            buildButtress(-radius - 1, z, Direction.WEST, 9 + random.nextInt(8));
            buildButtress(radius + 1, z, Direction.EAST, 8 + random.nextInt(10));
        }

        addArrowSlits();
        addGatehouseMachicolations();
    }

    private void buildHorizontalWallwalk(int z, Direction outward) {
        int radius = TOTAL_SIZE / 2;
        int innerOffset = outward == Direction.NORTH ? WALL_THICKNESS : -WALL_THICKNESS;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -radius + 5; x <= radius - 5; x++) {
            if (z < 0 && isGateGap(x)) {
                continue;
            }
            for (int dz = 0; dz != innerOffset; dz += innerOffset > 0 ? 1 : -1) {
                pos.set(center.getX() + x, baseY + WALL_HEIGHT + 1, center.getZ() + z + dz);
                level.setBlock(pos, EldrathCastlePalette.slab(random), 2);
            }

            if (Math.abs(x) % 4 == 0) {
                pos.set(center.getX() + x, baseY + WALL_HEIGHT + 3, center.getZ() + z);
                level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
            } else if (Math.abs(x) % 4 == 2) {
                pos.set(center.getX() + x, baseY + WALL_HEIGHT + 2, center.getZ() + z);
                level.setBlock(pos, EldrathCastlePalette.wallTrim(random), 2);
            }
        }
    }

    private void buildVerticalWallwalk(int x, Direction outward) {
        int radius = TOTAL_SIZE / 2;
        int innerOffset = outward == Direction.WEST ? WALL_THICKNESS : -WALL_THICKNESS;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = -radius + 5; z <= radius - 5; z++) {
            for (int dx = 0; dx != innerOffset; dx += innerOffset > 0 ? 1 : -1) {
                pos.set(center.getX() + x + dx, baseY + WALL_HEIGHT + 1, center.getZ() + z);
                level.setBlock(pos, EldrathCastlePalette.slab(random), 2);
            }

            if (Math.abs(z) % 4 == 0) {
                pos.set(center.getX() + x, baseY + WALL_HEIGHT + 3, center.getZ() + z);
                level.setBlock(pos, EldrathCastlePalette.wall(random), 2);
            } else if (Math.abs(z) % 4 == 2) {
                pos.set(center.getX() + x, baseY + WALL_HEIGHT + 2, center.getZ() + z);
                level.setBlock(pos, EldrathCastlePalette.wallTrim(random), 2);
            }
        }
    }

    private void buildButtress(int x, int z, Direction outward, int height) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 1; y <= height; y++) {
            int depth = y < 5 ? 4 : y < 11 ? 3 : 2;
            int width = y < 7 ? 2 : 1;
            for (int d = 0; d < depth; d++) {
                for (int w = -width; w <= width; w++) {
                    int px = x + outward.getStepX() * d + (outward.getStepZ() != 0 ? w : 0);
                    int pz = z + outward.getStepZ() * d + (outward.getStepX() != 0 ? w : 0);
                    pos.set(center.getX() + px, baseY + y, center.getZ() + pz);
                    level.setBlock(pos, y == height ? EldrathCastlePalette.wallTrim(random) : EldrathCastlePalette.wall(random), 2);
                }
            }
        }
    }

    private void addArrowSlits() {
        int radius = TOTAL_SIZE / 2;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -radius + 16; x <= radius - 16; x += 10) {
            if (!isGateGap(x)) {
                carveArrowSlit(x, -radius + 1, Direction.NORTH, pos);
            }
            carveArrowSlit(x, radius - 1, Direction.SOUTH, pos);
        }
        for (int z = -radius + 18; z <= radius - 18; z += 10) {
            carveArrowSlit(-radius + 1, z, Direction.WEST, pos);
            carveArrowSlit(radius - 1, z, Direction.EAST, pos);
        }
    }

    private void carveArrowSlit(int x, int z, Direction outward, BlockPos.MutableBlockPos pos) {
        for (int y = 7; y <= 10; y++) {
            pos.set(center.getX() + x, baseY + y, center.getZ() + z);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
        int accentX = x + outward.getStepX();
        int accentZ = z + outward.getStepZ();
        pos.set(center.getX() + accentX, baseY + 6, center.getZ() + accentZ);
        level.setBlock(pos, EldrathCastlePalette.stair(random, outward.getOpposite()), 2);
        pos.set(center.getX() + accentX, baseY + 11, center.getZ() + accentZ);
        level.setBlock(pos, EldrathCastlePalette.stair(random, outward), 2);
    }

    private void addGatehouseMachicolations() {
        int north = -TOTAL_SIZE / 2;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -16; x <= 16; x += 2) {
            pos.set(center.getX() + x, baseY + 16, center.getZ() + north - 1);
            level.setBlock(pos, EldrathCastlePalette.stair(random, Direction.SOUTH), 2);
            pos.set(center.getX() + x, baseY + 17, center.getZ() + north - 2);
            level.setBlock(pos, EldrathCastlePalette.wallTrim(random), 2);
        }
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

    private void buildExplorableInteriorRooms() {
        buildArmory(-58, -42);
        buildPrison(42, -38);
        buildCollapsedForge(45, 13);
        buildAshCrypt(-58, 18);
        buildGuardPosts();
    }

    private void buildArmory(int originX, int originZ) {
        buildRoomShell(originX, originZ, 28, 22, 9, Direction.SOUTH);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = originX + 5; x <= originX + 22; x += 4) {
            for (int z = originZ + 5; z <= originZ + 16; z += 6) {
                pos.set(center.getX() + x, baseY + 1, center.getZ() + z);
                level.setBlock(pos, Blocks.IRON_BARS.defaultBlockState(), 3);
                pos.set(center.getX() + x, baseY + 2, center.getZ() + z);
                level.setBlock(pos, Blocks.CHAIN.defaultBlockState(), 3);
            }
        }
        placeRelicPedestal(originX + 14, originZ + 11, Blocks.DAMAGED_ANVIL.defaultBlockState());
    }

    private void buildPrison(int originX, int originZ) {
        buildRoomShell(originX, originZ, 28, 24, 8, Direction.WEST);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = originZ + 4; z <= originZ + 20; z += 8) {
            for (int x = originX + 4; x <= originX + 23; x++) {
                pos.set(center.getX() + x, baseY + 1, center.getZ() + z);
                level.setBlock(pos, Blocks.IRON_BARS.defaultBlockState(), 3);
                pos.set(center.getX() + x, baseY + 2, center.getZ() + z);
                level.setBlock(pos, Blocks.IRON_BARS.defaultBlockState(), 3);
            }
            pos.set(center.getX() + originX + 6, baseY + 3, center.getZ() + z);
            level.setBlock(pos, Blocks.CHAIN.defaultBlockState(), 3);
        }
        placeRelicPedestal(originX + 20, originZ + 12, Blocks.CAULDRON.defaultBlockState());
    }

    private void buildCollapsedForge(int originX, int originZ) {
        buildRoomShell(originX, originZ, 30, 26, 10, Direction.WEST);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = originX + 7; x <= originX + 23; x++) {
            for (int z = originZ + 7; z <= originZ + 18; z++) {
                if (random.nextInt(100) < 20) {
                    pos.set(center.getX() + x, baseY, center.getZ() + z);
                    level.setBlock(pos, EldrathCastlePalette.scorched(random), 2);
                }
            }
        }
        placeRelicPedestal(originX + 14, originZ + 12, Blocks.BLAST_FURNACE.defaultBlockState());
        placeRelicPedestal(originX + 18, originZ + 12, Blocks.ANVIL.defaultBlockState());
        for (int i = 0; i < 7; i++) {
            int x = originX + 6 + random.nextInt(18);
            int z = originZ + 5 + random.nextInt(17);
            pos.set(center.getX() + x, baseY + 1, center.getZ() + z);
            level.setBlock(pos, random.nextBoolean() ? Blocks.FIRE.defaultBlockState() : Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
        }
    }

    private void buildAshCrypt(int originX, int originZ) {
        buildRoomShell(originX, originZ, 30, 28, 7, Direction.EAST);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int z = originZ + 5; z <= originZ + 22; z += 5) {
            for (int x : new int[]{originX + 7, originX + 21}) {
                pos.set(center.getX() + x, baseY + 1, center.getZ() + z);
                level.setBlock(pos, Blocks.CHISELED_DEEPSLATE.defaultBlockState(), 2);
                pos.set(center.getX() + x, baseY + 2, center.getZ() + z);
                level.setBlock(pos, EldrathCastlePalette.wallTrim(random), 2);
            }
        }
        placeRelicPedestal(originX + 14, originZ + 14, Blocks.LODESTONE.defaultBlockState());
    }

    private void buildGuardPosts() {
        buildRoomShell(-24, -63, 16, 16, 8, Direction.SOUTH);
        buildRoomShell(8, -63, 16, 16, 8, Direction.SOUTH);
        placeRelicPedestal(-16, -55, Blocks.SOUL_LANTERN.defaultBlockState());
        placeRelicPedestal(16, -55, Blocks.SOUL_LANTERN.defaultBlockState());
    }

    private void buildRoomShell(int originX, int originZ, int width, int length, int height, Direction doorSide) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                pos.set(center.getX() + originX + x, baseY - 1, center.getZ() + originZ + z);
                level.setBlock(pos, EldrathCastlePalette.scorched(random), 2);
                pos.set(center.getX() + originX + x, baseY, center.getZ() + originZ + z);
                level.setBlock(pos, EldrathCastlePalette.floor(random), 2);

                boolean edge = x == 0 || z == 0 || x == width - 1 || z == length - 1;
                boolean door = isRoomDoor(x, z, width, length, doorSide);
                if (edge && !door) {
                    int brokenHeight = height - (random.nextInt(100) < 22 ? random.nextInt(4) : 0);
                    for (int y = 1; y <= brokenHeight; y++) {
                        if (random.nextInt(100) > 12) {
                            pos.set(center.getX() + originX + x, baseY + y, center.getZ() + originZ + z);
                            level.setBlock(pos, y == brokenHeight && random.nextBoolean() ? EldrathCastlePalette.wallTrim(random) : EldrathCastlePalette.wall(random), 2);
                        }
                    }
                }
            }
        }

        for (int x = 3; x <= width - 4; x += 7) {
            buildPillar(originX + x, originZ + 2, height + 2);
            buildPillar(originX + x, originZ + length - 3, height + 1);
        }
    }

    private boolean isRoomDoor(int x, int z, int width, int length, Direction side) {
        int middleX = width / 2;
        int middleZ = length / 2;
        if (side == Direction.NORTH) return z == 0 && Math.abs(x - middleX) <= 2;
        if (side == Direction.SOUTH) return z == length - 1 && Math.abs(x - middleX) <= 2;
        if (side == Direction.WEST) return x == 0 && Math.abs(z - middleZ) <= 2;
        return x == width - 1 && Math.abs(z - middleZ) <= 2;
    }

    private void placeRelicPedestal(int x, int z, BlockState cap) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        pos.set(center.getX() + x, baseY + 1, center.getZ() + z);
        level.setBlock(pos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
        pos.set(center.getX() + x, baseY + 2, center.getZ() + z);
        level.setBlock(pos, cap, 2);
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

    private void buildStorytellingSetPieces() {
        buildRoyalRoad();
        buildDryWaterChannels();
        buildFallenColumns();
        buildBrokenStandards();
        buildExecutionScar();
        buildOuterStatueBases();
    }

    private void buildRoyalRoad() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int north = -TOTAL_SIZE / 2 + 8;
        int throneZ = COURTYARD_SIZE / 2 + 30;
        for (int z = north; z <= throneZ; z++) {
            for (int x = -4; x <= 4; x++) {
                pos.set(center.getX() + x, baseY, center.getZ() + z);
                BlockState state = Math.abs(x) == 4
                        ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                        : (random.nextInt(100) < 18 ? EldrathCastlePalette.scorched(random) : Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
                level.setBlock(pos, state, 2);
            }
        }

        for (int z = -28; z <= 30; z += 14) {
            buildSwordMemorial(-9, z);
            buildSwordMemorial(9, z);
        }
    }

    private void buildDryWaterChannels() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = -COURTYARD_SIZE / 2 + 8; i <= COURTYARD_SIZE / 2 - 8; i++) {
            for (int offset = -1; offset <= 1; offset++) {
                pos.set(center.getX() + i, baseY, center.getZ() + offset);
                level.setBlock(pos, offset == 0 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : EldrathCastlePalette.topSlab(random), 2);
                pos.set(center.getX() + offset, baseY, center.getZ() + i);
                level.setBlock(pos, offset == 0 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : EldrathCastlePalette.topSlab(random), 2);
            }
        }

        for (int x : new int[]{-COURTYARD_SIZE / 2 + 7, COURTYARD_SIZE / 2 - 7}) {
            for (int z : new int[]{-COURTYARD_SIZE / 2 + 7, COURTYARD_SIZE / 2 - 7}) {
                pos.set(center.getX() + x, baseY + 1, center.getZ() + z);
                level.setBlock(pos, Blocks.IRON_BARS.defaultBlockState(), 3);
            }
        }
    }

    private void buildFallenColumns() {
        buildFallenColumn(-30, -22, Direction.EAST, 13);
        buildFallenColumn(28, 18, Direction.WEST, 12);
        buildFallenColumn(-22, 28, Direction.SOUTH, 10);
        buildFallenColumn(31, -28, Direction.NORTH, 9);
    }

    private void buildFallenColumn(int x, int z, Direction direction, int length) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < length; i++) {
            int px = x + direction.getStepX() * i;
            int pz = z + direction.getStepZ() * i;
            pos.set(center.getX() + px, baseY + 1, center.getZ() + pz);
            level.setBlock(pos, i % 4 == 0 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : EldrathCastlePalette.wall(random), 2);
            if (i % 3 == 0) {
                pos.set(center.getX() + px, baseY, center.getZ() + pz);
                level.setBlock(pos, EldrathCastlePalette.scorched(random), 2);
            }
        }
    }

    private void buildBrokenStandards() {
        for (int x : new int[]{-34, 34}) {
            for (int z : new int[]{-34, 34}) {
                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                for (int y = 1; y <= 7; y++) {
                    pos.set(center.getX() + x, baseY + y, center.getZ() + z);
                    level.setBlock(pos, Blocks.IRON_BARS.defaultBlockState(), 3);
                }
                for (int i = 0; i < 4; i++) {
                    pos.set(center.getX() + x + (x < 0 ? 1 : -1), baseY + 7 - i, center.getZ() + z);
                    level.setBlock(pos, Blocks.CHAIN.defaultBlockState(), 3);
                }
            }
        }
    }

    private void buildExecutionScar() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -11; x <= 11; x++) {
            for (int z = 18; z <= 27; z++) {
                if (Math.abs(x) + random.nextInt(4) < 12) {
                    pos.set(center.getX() + x, baseY, center.getZ() + z);
                    level.setBlock(pos, random.nextInt(100) < 18 ? Blocks.MAGMA_BLOCK.defaultBlockState() : EldrathCastlePalette.scorched(random), 2);
                }
            }
        }
    }

    private void buildOuterStatueBases() {
        for (int x : new int[]{-49, 49}) {
            buildRuinedStatueBase(x, -52, Direction.SOUTH);
            buildRuinedStatueBase(x, 52, Direction.NORTH);
        }
    }

    private void buildRuinedStatueBase(int x, int z, Direction facing) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                pos.set(center.getX() + x + dx, baseY + 1, center.getZ() + z + dz);
                level.setBlock(pos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
            }
        }
        for (int y = 2; y <= 5 + random.nextInt(3); y++) {
            pos.set(center.getX() + x, baseY + y, center.getZ() + z);
            level.setBlock(pos, y % 2 == 0 ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : EldrathCastlePalette.wall(random), 2);
        }
        pos.set(center.getX() + x + facing.getStepX(), baseY + 3, center.getZ() + z + facing.getStepZ());
        level.setBlock(pos, Blocks.CHAIN.defaultBlockState(), 3);
    }

    private void buildSwordMemorial(int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        pos.set(center.getX() + x, baseY + 1, center.getZ() + z);
        level.setBlock(pos, Blocks.IRON_BARS.defaultBlockState(), 3);
        pos.set(center.getX() + x, baseY + 2, center.getZ() + z);
        level.setBlock(pos, Blocks.CHAIN.defaultBlockState(), 3);
        pos.set(center.getX() + x, baseY, center.getZ() + z);
        level.setBlock(pos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
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

    private void removeUnsafeFlammableBlocks() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -SAFETY_CLEAR_RADIUS; x <= SAFETY_CLEAR_RADIUS; x++) {
            for (int z = -SAFETY_CLEAR_RADIUS; z <= SAFETY_CLEAR_RADIUS; z++) {
                boolean insideCastle = Math.abs(x) <= BUILD_CLEAR_RADIUS && Math.abs(z) <= BUILD_CLEAR_RADIUS;
                boolean nearApproach = Math.abs(x) <= 24 && z < -TOTAL_SIZE / 2 && z >= -TOTAL_SIZE / 2 - 54;
                if (!insideCastle && !nearApproach) {
                    continue;
                }

                for (int y = 1; y <= BUILD_CLEAR_HEIGHT; y++) {
                    pos.set(center.getX() + x, baseY + y, center.getZ() + z);
                    Block block = level.getBlockState(pos).getBlock();
                    if (EldrathCastlePalette.isForbidden(block)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    public record BuildResult(BlockPos center, BlockPos entrance, BlockPos bossPosition) {
    }
}
