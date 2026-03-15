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

    private enum IslandType { DESERT, OCEAN, LAVA, MINING, OBLIVION }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        BlockPos base = new BlockPos(origin.getX(), origin.getY(), origin.getZ());
        if (!level.getBlockState(base).isAir()) return false;

        IslandType type = IslandType.values()[random.nextInt(IslandType.values().length)];

        switch (type) {
            case DESERT   -> generateDesertIsland(level, base, random);
            case OCEAN    -> generateOceanIsland(level, base, random);
            case LAVA     -> generateLavaIsland(level, base, random);
            case MINING   -> generateMiningIsland(level, base, random);
            case OBLIVION -> generateOblivionIsland(level, base, random);
        }

        return true;
    }

    // -----------------------------------------------------------------------
    // Пустынный остров
    // -----------------------------------------------------------------------
    private void generateDesertIsland(WorldGenLevel level, BlockPos base, RandomSource random) {
        int rX = 4 + random.nextInt(8);
        int rZ = 4 + random.nextInt(8);

        // Основа из песчаника — не падает
        buildEllipsoid(level, base, rX, rZ,
                Blocks.SANDSTONE.defaultBlockState(),
                Blocks.SANDSTONE.defaultBlockState(),
                Blocks.STONE.defaultBlockState());

        // Песок только там где под ним есть песчаник на том же y=0 уровне
        for (int x = -rX; x <= rX; x++) {
            for (int z = -rZ; z <= rZ; z++) {
                BlockPos sandPos = base.offset(x, 1, z);
                BlockPos belowSand = base.offset(x, 0, z);
                // Кладём песок только если под ним твёрдый песчаник
                if (level.getBlockState(sandPos).isAir() &&
                        level.getBlockState(belowSand).is(Blocks.SANDSTONE)) {
                    // Проверяем что хотя бы один горизонтальный сосед тоже твёрдый
                    boolean supported = level.getBlockState(belowSand.north()).is(Blocks.SANDSTONE) ||
                            level.getBlockState(belowSand.south()).is(Blocks.SANDSTONE) ||
                            level.getBlockState(belowSand.east()).is(Blocks.SANDSTONE) ||
                            level.getBlockState(belowSand.west()).is(Blocks.SANDSTONE);
                    // На краях не кладём песок — только в центре
                    double dist = (double)(x*x)/(rX*rX) + (double)(z*z)/(rZ*rZ);
                    if (dist < 0.7) {
                        level.setBlock(sandPos, Blocks.SAND.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Кактусы — только на песке
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

        // Мёртвые кусты — только на песке
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
    // Океанический остров (вода не выливается)
    // -----------------------------------------------------------------------
    private void generateOceanIsland(WorldGenLevel level, BlockPos base, RandomSource random) {
        int rX = 5 + random.nextInt(8);
        int rZ = 5 + random.nextInt(8);

        // Каменная основа — весь эллипсоид из камня включая верх
        buildEllipsoid(level, base, rX, rZ,
                Blocks.STONE.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                Blocks.STONE.defaultBlockState());

        // Вода только строго внутри — центральная часть
        for (int x = -(rX - 2); x <= rX - 2; x++) {
            for (int z = -(rZ - 2); z <= rZ - 2; z++) {
                double center = (double)(x*x)/((rX-2)*(rX-2)) + (double)(z*z)/((rZ-2)*(rZ-2));
                if (center > 0.6) continue;
                BlockPos wpos = base.offset(x, 0, z);
                if (level.getBlockState(wpos).is(Blocks.STONE)) {
                    level.setBlock(wpos, Blocks.WATER.defaultBlockState(), 2);
                }
                // Водоросли на дне
                if (random.nextFloat() < 0.4f) {
                    BlockPos spos = base.offset(x, -1, z);
                    if (level.getBlockState(spos).is(Blocks.STONE)) {
                        level.setBlock(spos, Blocks.SEAGRASS.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Песок только на верхнем слое где НЕТ воды и есть опора снизу
        for (int x = -rX; x <= rX; x++) {
            for (int z = -rZ; z <= rZ; z++) {
                BlockPos sandPos = base.offset(x, 1, z);
                BlockPos below = base.offset(x, 0, z);
                if (level.getBlockState(sandPos).isAir() &&
                        level.getBlockState(below).is(Blocks.STONE)) {
                    // Проверяем что блок ниже имеет опору — не на краю
                    boolean hasSupport =
                            !level.getBlockState(below.below()).isAir();
                    if (hasSupport) {
                        level.setBlock(sandPos, Blocks.SAND.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Лавовый остров (лава не выливается)
    // -----------------------------------------------------------------------
    private void generateLavaIsland(WorldGenLevel level, BlockPos base, RandomSource random) {
        int rX = 4 + random.nextInt(7);
        int rZ = 4 + random.nextInt(7);

        buildEllipsoid(level, base, rX, rZ,
                Blocks.BLACKSTONE.defaultBlockState(),
                Blocks.BASALT.defaultBlockState(),
                Blocks.NETHERRACK.defaultBlockState());

        // Лава только в центре — окружена блоками со всех сторон
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

        // Магма вокруг лавы
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

        // Обсидиановая башня с огнём
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
        int rY = 3;
        for (int x = -rX; x <= rX; x++) {
            for (int y = -rY; y <= 0; y++) {
                for (int z = -rZ; z <= rZ; z++) {
                    double e = (double)(x*x)/(rX*rX) + (double)(y*y)/(rY*rY) + (double)(z*z)/(rZ*rZ);
                    if (e > 1.0) continue;
                    BlockPos pos = base.offset(x, y, z);
                    if (y == 0) level.setBlock(pos, top, 2);
                    else if (y >= -2) level.setBlock(pos, middle, 2);
                    else level.setBlock(pos, bottom, 2);
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
}