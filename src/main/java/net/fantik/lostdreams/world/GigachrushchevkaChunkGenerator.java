package net.fantik.lostdreams.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fantik.lostdreams.LostDreams;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;

import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Генератор с детерминированной спиральной лестницей в центральной шахте 3x3.
 * Лестница строится без рандома (фиксированный порядок шагов N→E→S→W, +1 по Y каждый шаг),
 * начинается не с 0-го этажа (с baseY+1). После каждого марша (floorHeight-1 ступеней)
 * создается площадка 3x3, вырезается дверь и отверстие в потолке. Перекрытия вне шахты не ломаются.
 * Просто вставьте этот класс вместо старого, метод buildStairs вызывается в месте старой генерации лестницы.
 */
public class GigachrushchevkaChunkGenerator extends ChunkGenerator {

    public static final MapCodec<GigachrushchevkaChunkGenerator> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
                            Codec.LONG.fieldOf("seed").forGetter(g -> g.seed)
                    ).apply(instance, GigachrushchevkaChunkGenerator::new));

    private final BiomeSource biomeSource;
    private final long seed;

    public GigachrushchevkaChunkGenerator(BiomeSource biomeSource, long seed) {
        super(biomeSource);
        this.biomeSource = biomeSource;
        this.seed = seed;
    }

    public static void register(RegisterEvent event) {
        event.register(Registries.CHUNK_GENERATOR, helper -> {
            helper.register(
                    ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "gigachrushchevka"),
                    CODEC
            );
        });
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager sm, ChunkAccess chunk) {
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;

        final int districtSize = 64;
        final int cellSize = 16;
        final int baseY = 128;

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = cx * 16 + lx;
                int wz = cz * 16 + lz;

                RandomSource rand = RandomSource.create(seed + wx * 34187L + wz * 13289L);

                // Поля и земля
                boolean water = (wx % 6 == 0);
                for (int y = -64; y < 0; y++) {
                    BlockPos p = new BlockPos(wx, y, wz);
                    if (y < -3) {
                        setIfInChunk(chunk, p, cx, cz, Blocks.STONE.defaultBlockState());
                    } else {
                        setIfInChunk(chunk, p, cx, cz, Blocks.DIRT.defaultBlockState());
                    }
                }
                if (isInChunk(wx, wz, cx, cz)) {
                    BlockPos top = new BlockPos(wx, 0, wz);
                    if (water) {
                        chunk.setBlockState(top, Blocks.WATER.defaultBlockState(), false);
                    } else {
                        chunk.setBlockState(top, Blocks.FARMLAND.defaultBlockState(), false);
                        chunk.setBlockState(top.above(),
                                Blocks.POTATOES.defaultBlockState().setValue(CropBlock.AGE, 7), false);
                        chunk.setBlockState(top.above(2),
                                Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), false);
                    }
                }

                // Микрорайон: вычисляем ячейку
                int dx = Math.floorDiv(wx, districtSize);
                int dz = Math.floorDiv(wz, districtSize);
                int localX = Math.floorMod(wx, districtSize);
                int localZ = Math.floorMod(wz, districtSize);
                int cellX = localX / cellSize;
                int cellZ = localZ / cellSize;
                boolean isYard = (cellX == 1 && cellZ == 1);

                RandomSource dRand = RandomSource.create(seed + dx * 9999L + dz * 8888L);
                int floors = 15 + dRand.nextInt(6);
                int floorHeight = 4 + dRand.nextInt(2);
                int height = floors * floorHeight;

                int bx = dx * districtSize + cellSize * cellX;
                int bz = dz * districtSize + cellSize * cellZ;

                boolean insideCell = wx >= bx && wx < bx + cellSize && wz >= bz && wz < bz + cellSize;
                if (!insideCell || isYard) {
                    continue;
                }

                // Лестничная шахта 3x3 (центр)
                int stairCenterX = bx + cellSize / 2;
                int stairCenterZ = bz + cellSize / 2;
                boolean inShaft = (Math.abs(wx - stairCenterX) <= 1 && Math.abs(wz - stairCenterZ) <= 1);

                // Стены и перекрытия
                for (int y = baseY; y < baseY + height; y++) {
                    boolean wall = (wx == bx || wx == bx + cellSize - 1 ||
                            wz == bz || wz == bz + cellSize - 1);
                    BlockPos pos = new BlockPos(wx, y, wz);
                    if (wall) {
                        if (!inShaft) {
                            if (y % floorHeight == floorHeight - 2) {
                                setIfInChunk(chunk, pos, cx, cz, Blocks.GLASS.defaultBlockState());
                            } else {
                                setIfInChunk(chunk, pos, cx, cz, Blocks.STONE_BRICKS.defaultBlockState());
                            }
                        } else {
                            setIfInChunk(chunk, pos, cx, cz, Blocks.AIR.defaultBlockState());
                        }
                    }
                    if (y % floorHeight == 0) {
                        if (inShaft) {
                            setIfInChunk(chunk, pos, cx, cz, Blocks.AIR.defaultBlockState());
                        } else {
                            setIfInChunk(chunk, pos, cx, cz, Blocks.SMOOTH_STONE.defaultBlockState());
                        }
                    }
                }

                // Строим лестницу только в центре шахты
                if (wx == stairCenterX && wz == stairCenterZ) {
                    buildStairs(chunk, cx, cz, stairCenterX, stairCenterZ, baseY, floors, floorHeight);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    /**
     * Строит винтовую лестницу в центре шахты 3x3.
     * Шаг = +1 по Y и смещение (N, E, S, W) поочередно. После каждого марша
     * (floorHeight-1 ступеней) делаем площадку 3x3, дверь (2 блока высоты) и отверстие наверху.
     */
    private void buildStairs(ChunkAccess chunk, int chunkX, int chunkZ,
                             int centerX, int centerZ,
                             int baseY, int floors, int floorHeight) {
        // Начальные координаты: центр шахты, один блок над базовым Y
        int sx = centerX;
        int sz = centerZ;
        int sy = baseY + 1;

        // Направления шагов (N, E, S, W)
        Direction[] dirs = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

        for (int floor = 0; floor < floors; floor++) {
            // Марш лестницы: floorHeight-1 ступеней
            for (int step = 0; step < floorHeight - 1; step++) {
                Direction dir = dirs[step % 4];
                BlockPos pos = new BlockPos(sx, sy, sz);
                setIfInChunk(chunk, pos, chunkX, chunkZ,
                        Blocks.OAK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, dir));
                setIfInChunk(chunk, pos.below(), chunkX, chunkZ, Blocks.STONE.defaultBlockState());
                // движение вверх и смещение
                sy += 1;
                sx += dir.getStepX();
                sz += dir.getStepZ();
            }
            // скорректировать на уровень последней ступени
            sy -= 1;
            // площадка 3x3
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = new BlockPos(sx + dx, sy, sz + dz);
                    setIfInChunk(chunk, p, chunkX, chunkZ, Blocks.SMOOTH_STONE.defaultBlockState());
                }
            }
            // дверной проем (2 блока высоты) перед лестницей
            Direction doorDir = dirs[(floorHeight - 1) % 4];
            int doorX = sx + doorDir.getStepX();
            int doorZ = sz + doorDir.getStepZ();
            for (int h = 0; h < 2; h++) {
                BlockPos door = new BlockPos(doorX, sy + h, doorZ);
                setIfInChunk(chunk, door, chunkX, chunkZ, Blocks.AIR.defaultBlockState());
            }
            // отверстие в потолке (3x3) над площадкой
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = new BlockPos(sx + dx, sy + 1, sz + dz);
                    setIfInChunk(chunk, p, chunkX, chunkZ, Blocks.AIR.defaultBlockState());
                }
            }
            // переходим к следующему маршу (sy уже на верхнем уровне предыдущей площадки)
        }
    }

    // Помощь: проверка, что координаты в пределах этого чанка
    private static boolean isInChunk(int x, int z, int chunkX, int chunkZ) {
        return (x >> 4) == chunkX && (z >> 4) == chunkZ;
    }

    // Помощь: ставим блок, если позиция принадлежит этому чанку
    private static void setIfInChunk(ChunkAccess chunk, BlockPos pos, int chunkX, int chunkZ, BlockState state) {
        if ((pos.getX() >> 4) == chunkX && (pos.getZ() >> 4) == chunkZ) {
            chunk.setBlockState(pos, state, false);
        }
    }

    @Override public void buildSurface(WorldGenRegion level, StructureManager sm, RandomState random, ChunkAccess chunk) {}
    @Override public void applyCarvers(WorldGenRegion level, long seed, RandomState random,
                                       BiomeManager bm, StructureManager sm,
                                       ChunkAccess chunk, GenerationStep.Carving step) {}
    @Override public void spawnOriginalMobs(WorldGenRegion level) {}

    @Override public int getBaseHeight(int x, int z, Heightmap.Types type,
                                       LevelHeightAccessor level, RandomState random) {
        return 64;
    }
    @Override public NoiseColumn getBaseColumn(int x, int z,
                                               LevelHeightAccessor level, RandomState random) {
        return new NoiseColumn(-64, new BlockState[0]);
    }
    @Override public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {}
    @Override public int getGenDepth() { return 640; }
    @Override public int getSeaLevel() { return 0; }
    @Override public int getMinY() { return -64; }
}
