package net.fantik.lostdreams.world.feature;

import com.mojang.serialization.Codec;
import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SkyBlockFloatingIslandsFeature extends Feature<NoneFeatureConfiguration> {

    public SkyBlockFloatingIslandsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private enum IslandType {
        DESERT, OCEAN, LAVA, MINING, OBLIVION,
        RED_DESERT, FOREST, BRICK, MUSHROOM
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos base = new BlockPos(origin.getX(), origin.getY(), origin.getZ());
        if (!level.getBlockState(base).isAir()) return false;

        // Кирпичный остров — очень редкий (1 из 50)
        IslandType type;
        if (random.nextInt(50) == 0) {
            type = IslandType.BRICK;
        } else {
            // Остальные 8 типов без BRICK
            IslandType[] common = {
                    IslandType.DESERT, IslandType.OCEAN, IslandType.LAVA,
                    IslandType.MINING, IslandType.OBLIVION, IslandType.RED_DESERT,
                    IslandType.FOREST, IslandType.MUSHROOM
            };
            type = common[random.nextInt(common.length)];
        }

        switch (type) {
            case DESERT     -> generateDesertIsland(level, base, random);
            case OCEAN      -> generateOceanIsland(level, base, random);
            case LAVA       -> generateLavaIsland(level, base, random);
            case MINING     -> generateMiningIsland(level, base, random);
            case OBLIVION   -> generateOblivionIsland(level, base, random);
            case RED_DESERT -> generateRedDesertIsland(level, base, random);
            case FOREST     -> generateForestIsland(level, base, random);
            case BRICK      -> generateBrickIsland(level, base, random);
            case MUSHROOM   -> generateMushroomIsland(level, base, random);
        }

        return true;
    }

    // -----------------------------------------------------------------------
    // Пустынный остров
    // -----------------------------------------------------------------------
    private void generateDesertIsland(WorldGenLevel level, BlockPos base, RandomSource random) {
        int rX = 4 + random.nextInt(8);
        int rZ = 4 + random.nextInt(8);

        buildEllipsoid(level, base, rX, rZ,
                Blocks.SANDSTONE.defaultBlockState(),
                Blocks.SANDSTONE.defaultBlockState(),
                Blocks.STONE.defaultBlockState());

        for (int x = -rX; x <= rX; x++) {
            for (int z = -rZ; z <= rZ; z++) {
                BlockPos sandPos = base.offset(x, 1, z);
                BlockPos belowSand = base.offset(x, 0, z);
                if (level.getBlockState(sandPos).isAir() &&
                        level.getBlockState(belowSand).is(Blocks.SANDSTONE)) {
                    double dist = (double)(x*x)/(rX*rX) + (double)(z*z)/(rZ*rZ);
                    if (dist < 0.7) {
                        level.setBlock(sandPos, Blocks.SAND.defaultBlockState(), 2);
                    }
                }
            }
        }

        int cactusCount = 2 + random.nextInt(5);
        for (int i = 0; i < cactusCount; i++) {
            int cx = random.nextInt(Math.max(1, rX - 2) * 2) - (rX - 2);
            int cz = random.nextInt(Math.max(1, rZ - 2) * 2) - (rZ - 2);
            BlockPos cpos = base.offset(cx, 2, cz);
            if (level.getBlockState(cpos).isAir() &&
                    level.getBlockState(cpos.below()).is(Blocks.SAND)) {
                int height = 1 + random.nextInt(3);
                boolean canPlace = true;
                for (int h = 0; h < height; h++) {
                    if (!level.getBlockState(cpos.above(h)).isAir()) { canPlace = false; break; }
                }
                if (canPlace) {
                    for (int h = 0; h < height; h++) {
                        level.setBlock(cpos.above(h), Blocks.CACTUS.defaultBlockState(), 2);
                    }
                }
            }
        }

        int bushCount = 2 + random.nextInt(4);
        for (int i = 0; i < bushCount; i++) {
            int bx = random.nextInt(Math.max(1, rX - 2) * 2) - (rX - 2);
            int bz = random.nextInt(Math.max(1, rZ - 2) * 2) - (rZ - 2);
            BlockPos bpos = base.offset(bx, 2, bz);
            if (level.getBlockState(bpos).isAir() &&
                    level.getBlockState(bpos.below()).is(Blocks.SAND)) {
                level.setBlock(bpos, Blocks.DEAD_BUSH.defaultBlockState(), 2);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Красная пустыня
    // -----------------------------------------------------------------------
    private void generateRedDesertIsland(WorldGenLevel level, BlockPos base, RandomSource random) {
        int rX = 4 + random.nextInt(8);
        int rZ = 4 + random.nextInt(8);

        buildEllipsoid(level, base, rX, rZ,
                Blocks.RED_SANDSTONE.defaultBlockState(),
                Blocks.RED_SANDSTONE.defaultBlockState(),
                Blocks.STONE.defaultBlockState());

        // Красный песок поверх
        for (int x = -rX; x <= rX; x++) {
            for (int z = -rZ; z <= rZ; z++) {
                BlockPos sandPos = base.offset(x, 1, z);
                BlockPos belowSand = base.offset(x, 0, z);
                if (level.getBlockState(sandPos).isAir() &&
                        level.getBlockState(belowSand).is(Blocks.RED_SANDSTONE)) {
                    double dist = (double)(x*x)/(rX*rX) + (double)(z*z)/(rZ*rZ);
                    if (dist < 0.7) {
                        level.setBlock(sandPos, Blocks.RED_SAND.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Кактусы на красном песке
        int cactusCount = 2 + random.nextInt(5);
        for (int i = 0; i < cactusCount; i++) {
            int cx = random.nextInt(Math.max(1, rX - 2) * 2) - (rX - 2);
            int cz = random.nextInt(Math.max(1, rZ - 2) * 2) - (rZ - 2);
            BlockPos cpos = base.offset(cx, 2, cz);
            if (level.getBlockState(cpos).isAir() &&
                    level.getBlockState(cpos.below()).is(Blocks.RED_SAND)) {
                int height = 1 + random.nextInt(3);
                boolean canPlace = true;
                for (int h = 0; h < height; h++) {
                    if (!level.getBlockState(cpos.above(h)).isAir()) { canPlace = false; break; }
                }
                if (canPlace) {
                    for (int h = 0; h < height; h++) {
                        level.setBlock(cpos.above(h), Blocks.CACTUS.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Мёртвые кусты
        int bushCount = 2 + random.nextInt(4);
        for (int i = 0; i < bushCount; i++) {
            int bx = random.nextInt(Math.max(1, rX - 2) * 2) - (rX - 2);
            int bz = random.nextInt(Math.max(1, rZ - 2) * 2) - (rZ - 2);
            BlockPos bpos = base.offset(bx, 2, bz);
            if (level.getBlockState(bpos).isAir() &&
                    level.getBlockState(bpos.below()).is(Blocks.RED_SAND)) {
                level.setBlock(bpos, Blocks.DEAD_BUSH.defaultBlockState(), 2);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Лесной остров
    // -----------------------------------------------------------------------
    private void generateForestIsland(WorldGenLevel level, BlockPos base, RandomSource random) {
        int rX = 5 + random.nextInt(7);
        int rZ = 5 + random.nextInt(7);

        buildEllipsoid(level, base, rX, rZ,
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState());

        // Деревья — дуб и берёза вперемешку
        int treeCount = 2 + random.nextInt(4);
        int[][] offsets = {{0, 0}, {-3, 2}, {3, -2}, {2, 3}, {-2, -3}, {0, 4}, {4, 0}};
        for (int i = 0; i < treeCount && i < offsets.length; i++) {
            BlockPos treeBase = base.offset(offsets[i][0], 1, offsets[i][1]);
            if (random.nextBoolean()) {
                buildOakTree(level, treeBase, random);
            } else {
                buildBirchTree(level, treeBase, random);
            }
        }

        // Трава и цветы
        int grassCount = 5 + random.nextInt(8);
        for (int i = 0; i < grassCount; i++) {
            int gx = random.nextInt(rX * 2) - rX;
            int gz = random.nextInt(rZ * 2) - rZ;
            BlockPos gpos = base.offset(gx, 1, gz);
            if (level.getBlockState(gpos).isAir() &&
                    level.getBlockState(gpos.below()).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(gpos, Blocks.SHORT_GRASS.defaultBlockState(), 2);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Кирпичный остров (очень редкий)
    // -----------------------------------------------------------------------
    private void generateBrickIsland(WorldGenLevel level, BlockPos base, RandomSource random) {
        int rX = 6 + random.nextInt(5);
        int rZ = 6 + random.nextInt(5);

        buildEllipsoid(level, base, rX, rZ,
                Blocks.BRICKS.defaultBlockState(),
                Blocks.BRICKS.defaultBlockState(),
                Blocks.STONE.defaultBlockState());

        // Дом из грязи в центре
        buildMudHouse(level, base.offset(-2, 1, -2), random);
    }

    // -----------------------------------------------------------------------
    // Грибной остров
    // -----------------------------------------------------------------------
    private void generateMushroomIsland(WorldGenLevel level, BlockPos base, RandomSource random) {
        int rX = 5 + random.nextInt(6);
        int rZ = 5 + random.nextInt(6);

        buildEllipsoid(level, base, rX, rZ,
                Blocks.MYCELIUM.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState());

        // Грибы — красные и коричневые
        int mushroomCount = 3 + random.nextInt(5);
        for (int i = 0; i < mushroomCount; i++) {
            int mx = random.nextInt(rX * 2) - rX;
            int mz = random.nextInt(rZ * 2) - rZ;
            BlockPos mpos = base.offset(mx, 1, mz);
            if (level.getBlockState(mpos).isAir() &&
                    level.getBlockState(mpos.below()).is(Blocks.MYCELIUM)) {
                if (random.nextBoolean()) {
                    level.setBlock(mpos, Blocks.RED_MUSHROOM.defaultBlockState(), 2);
                } else {
                    level.setBlock(mpos, Blocks.BROWN_MUSHROOM.defaultBlockState(), 2);
                }
            }
        }

        // Большой гриб в центре
        if (random.nextBoolean()) {
            buildHugeMushroom(level, base.above(), random);
        }
    }

    // -----------------------------------------------------------------------
    // Океанический остров
    // -----------------------------------------------------------------------
    private void generateOceanIsland(WorldGenLevel level, BlockPos base, RandomSource random) {
        int rX = 5 + random.nextInt(8);
        int rZ = 5 + random.nextInt(8);

        buildEllipsoid(level, base, rX, rZ,
                Blocks.STONE.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                Blocks.STONE.defaultBlockState());

        for (int x = -(rX - 2); x <= rX - 2; x++) {
            for (int z = -(rZ - 2); z <= rZ - 2; z++) {
                double center = (double)(x*x)/((rX-2)*(rX-2)) + (double)(z*z)/((rZ-2)*(rZ-2));
                if (center > 0.6) continue;
                BlockPos wpos = base.offset(x, 0, z);
                if (level.getBlockState(wpos).is(Blocks.STONE)) {
                    level.setBlock(wpos, Blocks.WATER.defaultBlockState(), 2);
                }
                if (random.nextFloat() < 0.4f) {
                    BlockPos spos = base.offset(x, -1, z);
                    if (level.getBlockState(spos).is(Blocks.STONE)) {
                        level.setBlock(spos, Blocks.SEAGRASS.defaultBlockState(), 2);
                    }
                }
            }
        }

        for (int x = -rX; x <= rX; x++) {
            for (int z = -rZ; z <= rZ; z++) {
                BlockPos sandPos = base.offset(x, 1, z);
                BlockPos below = base.offset(x, 0, z);
                if (level.getBlockState(sandPos).isAir() &&
                        level.getBlockState(below).is(Blocks.STONE)) {
                    if (!level.getBlockState(below.below()).isAir()) {
                        level.setBlock(sandPos, Blocks.SAND.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Лавовый остров
    // -----------------------------------------------------------------------
    private void generateLavaIsland(WorldGenLevel level, BlockPos base, RandomSource random) {
        int rX = 4 + random.nextInt(7);
        int rZ = 4 + random.nextInt(7);

        buildEllipsoid(level, base, rX, rZ,
                Blocks.BLACKSTONE.defaultBlockState(),
                Blocks.BASALT.defaultBlockState(),
                Blocks.NETHERRACK.defaultBlockState());

        for (int x = -(rX - 2); x <= rX - 2; x++) {
            for (int z = -(rZ - 2); z <= rZ - 2; z++) {
                double center = (double)(x*x)/((rX-2)*(rX-2)) + (double)(z*z)/((rZ-2)*(rZ-2));
                if (center > 0.4) continue;
                BlockPos lpos = base.offset(x, 0, z);
                if (level.getBlockState(lpos).is(Blocks.BLACKSTONE)) {
                    level.setBlock(lpos, Blocks.LAVA.defaultBlockState(), 2);
                }
            }
        }

        for (int x = -rX + 1; x <= rX - 1; x++) {
            for (int z = -rZ + 1; z <= rZ - 1; z++) {
                double edge = (double)(x*x)/(rX*rX) + (double)(z*z)/(rZ*rZ);
                if (edge < 0.35 || edge > 0.65) continue;
                BlockPos mpos = base.offset(x, 0, z);
                if (level.getBlockState(mpos).is(Blocks.BLACKSTONE)) {
                    level.setBlock(mpos, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
                }
            }
        }

        int towerH = 3 + random.nextInt(5);
        for (int y = 1; y <= towerH; y++) {
            level.setBlock(base.above(y), Blocks.OBSIDIAN.defaultBlockState(), 2);
        }
        level.setBlock(base.above(towerH + 1), Blocks.FIRE.defaultBlockState(), 2);
    }

    // -----------------------------------------------------------------------
    // Шахтёрский остров
    // -----------------------------------------------------------------------
    private void generateMiningIsland(WorldGenLevel level, BlockPos base, RandomSource random) {
        int rX = 5 + random.nextInt(7);
        int rZ = 5 + random.nextInt(7);

        buildEllipsoid(level, base, rX, rZ,
                Blocks.STONE.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                Blocks.DEEPSLATE.defaultBlockState());

        BlockState[] ores = {
                Blocks.COAL_ORE.defaultBlockState(),
                Blocks.IRON_ORE.defaultBlockState(),
                Blocks.COPPER_ORE.defaultBlockState(),
                Blocks.GOLD_ORE.defaultBlockState(),
                Blocks.REDSTONE_ORE.defaultBlockState(),
                Blocks.LAPIS_ORE.defaultBlockState(),
                Blocks.DIAMOND_ORE.defaultBlockState(),
                Blocks.EMERALD_ORE.defaultBlockState()
        };

        for (BlockState ore : ores) {
            int attempts = 0;
            while (attempts < 30) {
                int ox = random.nextInt(rX * 2) - rX;
                int oy = -(random.nextInt(3));
                int oz = random.nextInt(rZ * 2) - rZ;
                BlockPos opos = base.offset(ox, oy, oz);
                if (level.getBlockState(opos).is(Blocks.STONE) ||
                        level.getBlockState(opos).is(Blocks.DEEPSLATE)) {
                    level.setBlock(opos, ore, 2);
                    break;
                }
                attempts++;
            }
        }

        int extra = 8 + random.nextInt(12);
        for (int i = 0; i < extra; i++) {
            int ox = random.nextInt(rX * 2) - rX;
            int oy = -(random.nextInt(3));
            int oz = random.nextInt(rZ * 2) - rZ;
            BlockPos opos = base.offset(ox, oy, oz);
            if (level.getBlockState(opos).is(Blocks.STONE) ||
                    level.getBlockState(opos).is(Blocks.DEEPSLATE)) {
                level.setBlock(opos, ores[random.nextInt(ores.length)], 2);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Острова забвения
    // -----------------------------------------------------------------------
    private void generateOblivionIsland(WorldGenLevel level, BlockPos base, RandomSource random) {
        int rX = 5 + random.nextInt(8);
        int rZ = 5 + random.nextInt(8);

        BlockState knowledgeBlock = switch (random.nextInt(3)) {
            case 0 -> ModBlocks.PINK_KNOWLEDGE_BLOCK.get().defaultBlockState();
            case 1 -> ModBlocks.BLUE_KNOWLEDGE_BLOCK.get().defaultBlockState();
            default -> ModBlocks.GREEN_KNOWLEDGE_BLOCK.get().defaultBlockState();
        };

        buildEllipsoid(level, base, rX, rZ, knowledgeBlock, knowledgeBlock, knowledgeBlock);

        int treeCount = 1 + random.nextInt(3);
        int[][] offsets = {{0, 0}, {-3, 2}, {3, -2}, {2, 3}, {-2, -3}};
        for (int i = 0; i < treeCount && i < offsets.length; i++) {
            buildDuskwillowTree(level, base.offset(offsets[i][0], 1, offsets[i][1]), random);
        }
    }

    // -----------------------------------------------------------------------
    // Вспомогательные методы
    // -----------------------------------------------------------------------
    private void buildEllipsoid(WorldGenLevel level, BlockPos base, int rX, int rZ,
                                BlockState top, BlockState middle, BlockState bottom) {
        int rY = Math.max(3, (rX + rZ) / 4);

        for (int x = -rX; x <= rX; x++) {
            for (int y = -rY; y <= 0; y++) {
                for (int z = -rZ; z <= rZ; z++) {
                    double e = (double)(x*x)/(rX*rX)
                            + (double)(y*y)/(rY*rY)
                            + (double)(z*z)/(rZ*rZ);
                    if (e > 1.0) continue;
                    BlockPos pos = base.offset(x, y, z);
                    if (y == 0) level.setBlock(pos, top, 2);
                    else if (y >= -(rY / 2)) level.setBlock(pos, middle, 2);
                    else level.setBlock(pos, bottom, 2);
                }
            }
        }
    }

    private void buildOakTree(WorldGenLevel level, BlockPos treeBase, RandomSource random) {
        int trunkH = 4 + random.nextInt(2);
        for (int y = 0; y < trunkH; y++) {
            level.setBlock(treeBase.above(y), Blocks.OAK_LOG.defaultBlockState(), 2);
        }
        BlockPos leafCenter = treeBase.above(trunkH - 1);
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 && Math.abs(z) == 2 && y <= 0) continue;
                    BlockPos lpos = leafCenter.offset(x, y, z);
                    if (level.getBlockState(lpos).isAir()) {
                        level.setBlock(lpos, Blocks.OAK_LEAVES.defaultBlockState()
                                .setValue(LeavesBlock.PERSISTENT, false), 2);
                    }
                }
            }
        }
    }

    private void buildBirchTree(WorldGenLevel level, BlockPos treeBase, RandomSource random) {
        int trunkH = 5 + random.nextInt(3);
        for (int y = 0; y < trunkH; y++) {
            level.setBlock(treeBase.above(y), Blocks.BIRCH_LOG.defaultBlockState(), 2);
        }
        BlockPos leafCenter = treeBase.above(trunkH - 1);
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 && Math.abs(z) == 2 && y <= 0) continue;
                    BlockPos lpos = leafCenter.offset(x, y, z);
                    if (level.getBlockState(lpos).isAir()) {
                        level.setBlock(lpos, Blocks.BIRCH_LEAVES.defaultBlockState()
                                .setValue(LeavesBlock.PERSISTENT, false), 2);
                    }
                }
            }
        }
    }

    private void buildDuskwillowTree(WorldGenLevel level, BlockPos treeBase, RandomSource random) {
        int trunkH = 4 + random.nextInt(3);
        for (int y = 0; y < trunkH; y++) {
            level.setBlock(treeBase.above(y), ModBlocks.DUSKWILLOW_LOG.get().defaultBlockState(), 2);
        }
        BlockPos leafCenter = treeBase.above(trunkH - 1);
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 && Math.abs(z) == 2 && y <= 0) continue;
                    BlockPos lpos = leafCenter.offset(x, y, z);
                    if (level.getBlockState(lpos).isAir()) {
                        level.setBlock(lpos, ModBlocks.DUSKWILLOW_LEAVES.get().defaultBlockState()
                                .setValue(LeavesBlock.PERSISTENT, false), 2);
                    }
                }
            }
        }
    }

    private void buildMudHouse(WorldGenLevel level, BlockPos base, RandomSource random) {
        int w = 5, h = 4, d = 5;
        // Стены из грязи
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    boolean isWall = x == 0 || x == w-1 || y == 0 || y == h-1 || z == 0 || z == d-1;
                    if (!isWall) continue;
                    BlockPos pos = base.offset(x, y, z);
                    if (y == h-1) {
                        level.setBlock(pos, Blocks.MUD_BRICKS.defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, Blocks.MUD.defaultBlockState(), 2);
                    }
                }
            }
        }
        // Дверной проём
        level.setBlock(base.offset(2, 1, 0), Blocks.AIR.defaultBlockState(), 2);
        level.setBlock(base.offset(2, 2, 0), Blocks.AIR.defaultBlockState(), 2);
        // Окно
        level.setBlock(base.offset(1, 2, 0), Blocks.GREEN_STAINED_GLASS.defaultBlockState(), 2);
        level.setBlock(base.offset(3, 2, 0), Blocks.GREEN_STAINED_GLASS.defaultBlockState(), 2);

        // Сундук с алмазом внутри
        BlockPos chestPos = base.offset(2, 1, 3);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        net.minecraft.world.level.block.entity.ChestBlockEntity chest =
                (net.minecraft.world.level.block.entity.ChestBlockEntity) level.getBlockEntity(chestPos);
        if (chest != null) {
            chest.setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 1));
        }
    }

    private void buildHugeMushroom(WorldGenLevel level, BlockPos base, RandomSource random) {
        boolean isRed = random.nextBoolean();
        BlockState stemBlock = Blocks.MUSHROOM_STEM.defaultBlockState();
        BlockState capBlock = isRed ? Blocks.RED_MUSHROOM_BLOCK.defaultBlockState()
                : Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState();

        int stemH = 4 + random.nextInt(3);
        for (int y = 0; y < stemH; y++) {
            level.setBlock(base.above(y), stemBlock, 2);
        }

        // Шляпка
        BlockPos capCenter = base.above(stemH);
        int capR = 2 + random.nextInt(2);
        for (int x = -capR; x <= capR; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -capR; z <= capR; z++) {
                    if (Math.abs(x) == capR && Math.abs(z) == capR) continue;
                    if (y == 0 && (Math.abs(x) == capR || Math.abs(z) == capR)) continue;
                    BlockPos cpos = capCenter.offset(x, y, z);
                    if (level.getBlockState(cpos).isAir()) {
                        level.setBlock(cpos, capBlock, 2);
                    }
                }
            }
        }
    }
}