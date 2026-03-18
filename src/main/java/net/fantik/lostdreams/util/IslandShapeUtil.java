package net.fantik.lostdreams.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class IslandShapeUtil {

    private static final BlockState[] RANDOM_ORES = {
            Blocks.COAL_ORE.defaultBlockState(),
            Blocks.IRON_ORE.defaultBlockState(),
            Blocks.COPPER_ORE.defaultBlockState(),
            Blocks.GOLD_ORE.defaultBlockState(),
            Blocks.REDSTONE_ORE.defaultBlockState(),
            Blocks.LAPIS_ORE.defaultBlockState(),
    };

    /**
     * Возвращает Y координату поверхности острова (для размещения деревьев, грибов и т.д.)
     */
    public static int buildIsland(WorldGenLevel level, BlockPos base,
                                  int rX, int rZ, RandomSource random,
                                  BlockState top, BlockState middle, BlockState bottom) {

        // Редко (10%) — старая эллипсоидная форма
        if (random.nextInt(10) == 0) {
            return buildEllipsoid(level, base, rX, rZ, random, top, middle, bottom);
        }

        int rYTop = Math.max(1, (rX + rZ) / 8);
        int rYBottom = Math.max(5, (rX + rZ) / 2);
        int surfaceY = 0;

        for (int x = -rX; x <= rX; x++) {
            for (int z = -rZ; z <= rZ; z++) {
                double horizDist = Math.sqrt((double)(x*x)/(rX*rX) + (double)(z*z)/(rZ*rZ));
                if (horizDist > 1.0) continue;

                int yTop = (int)(rYTop * (1.0 - horizDist * 0.5));
                int yBottom = -(int)(rYBottom * Math.pow(1.0 - horizDist, 0.5));
                int noise = (int)((random.nextFloat() - 0.5f) * 2.0f);
                yBottom += noise;

                if (x == 0 && z == 0) surfaceY = yTop;

                for (int y = yBottom; y <= yTop; y++) {
                    BlockPos pos = base.offset(x, y, z);
                    double depthRatio = (double)(y - yBottom) / Math.max(1, yTop - yBottom);
                    BlockState state;
                    if (y == yTop) {
                        state = top;
                    } else if (depthRatio > 0.4) {
                        state = middle;
                    } else {
                        state = bottom;
                    }
                    level.setBlock(pos, state, 2);

                    // Руды только глубоко — не на поверхности и не рядом с ней
                    if (y < yTop - 1 && depthRatio < 0.4) {
                        if (random.nextFloat() < 0.06f) {
                            if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) ||
                                    state.is(Blocks.SANDSTONE) || state.is(Blocks.RED_SANDSTONE) ||
                                    state.is(Blocks.NETHERRACK) || state.is(Blocks.BLACKSTONE)) {
                                level.setBlock(pos, RANDOM_ORES[random.nextInt(RANDOM_ORES.length)], 2);
                            }
                        }
                    }
                }
            }
        }
        return surfaceY;
    }

    private static int buildEllipsoid(WorldGenLevel level, BlockPos base,
                                      int rX, int rZ, RandomSource random,
                                      BlockState top, BlockState middle, BlockState bottom) {
        int rY = Math.max(3, (rX + rZ) / 4);
        for (int x = -rX; x <= rX; x++) {
            for (int y = -rY; y <= 0; y++) {
                for (int z = -rZ; z <= rZ; z++) {
                    double e = (double)(x*x)/(rX*rX) + (double)(y*y)/(rY*rY) + (double)(z*z)/(rZ*rZ);
                    if (e > 1.0) continue;
                    BlockPos pos = base.offset(x, y, z);
                    BlockState state;
                    if (y == 0) state = top;
                    else if (y >= -(rY / 2)) state = middle;
                    else state = bottom;
                    level.setBlock(pos, state, 2);
                    // Руды только глубоко
                    if (y < -1 && (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE))) {
                        if (random.nextFloat() < 0.06f) {
                            level.setBlock(pos, RANDOM_ORES[random.nextInt(RANDOM_ORES.length)], 2);
                        }
                    }
                }
            }
        }
        return 0;
    }

    public static boolean canPlace(WorldGenLevel level, BlockPos base) {
        return level.getBlockState(base).isAir();
    }
}