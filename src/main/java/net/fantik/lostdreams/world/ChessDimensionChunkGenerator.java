package net.fantik.lostdreams.world;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ChessDimensionChunkGenerator extends ChunkGenerator {

    // ── Codec ─────────────────────────────────────────────────────────────────
    public static final MapCodec<ChessDimensionChunkGenerator> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source")
                            .forGetter(generator -> generator.biomeSource)
            ).apply(instance, ChessDimensionChunkGenerator::new));

    public ChessDimensionChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }




    /*
     * =========================
     * НАСТРОЙКИ МИРА
     * =========================
     */

    // Нижняя граница мира
    private static final int MIN_Y = -64;

    // Высота генерации мира
    private static final int GEN_DEPTH = 384;

    // Средняя высота поверхности
    private static final int BASE_HEIGHT = 25;

    // Максимальная дополнительная высота рельефа
    private static final int TERRAIN_HEIGHT = 75;

    // Размер шахматной клетки
    private static final int CHESS_SIZE = 4;

    // Частота больших изменений рельефа
    private static final double TERRAIN_SCALE = 0.004;

    // Частота мелких деталей
    private static final double DETAIL_SCALE = 0.025;

    /*
     * Пещеры
     */

    private static final double CAVE_REGION_SCALE = 0.006;
    private static final double CAVE_REGION_THRESHOLD = 0.75;



    /*
     * Монолиты
     */

    // Сколько чанков занимает одна ячейка поиска монолита
    private static final int MONOLITH_GRID = 8;

    // Шанс монолита в одной ячейке
    private static final float MONOLITH_CHANCE = 0.50f;

    // Минимальная высота монолита
    private static final int MONOLITH_MIN_HEIGHT = 18;

    // Максимальная высота монолита
    private static final int MONOLITH_MAX_HEIGHT = 90;

    // Минимальный размер монолита
    private static final int MONOLITH_MIN_SIZE = 8;

    // Максимальный размер монолита
    private static final int MONOLITH_MAX_SIZE = 15;

    /*
     * Блоки
     */

    private static final BlockState WHITE =
            ModBlocks.CHESS_WHITE_PART.get().defaultBlockState();

    private static final BlockState BLACK =
            ModBlocks.CHESS_BLACK_PART.get().defaultBlockState();

    private static final BlockState GRAY =
            ModBlocks.CHESS_GRAY_PART.get().defaultBlockState();

    private static final BlockState LIGHT_GRAY =
            ModBlocks.CHESS_LIGHTGRAY_PART.get().defaultBlockState();

    private static final BlockState AIR =
            Blocks.AIR.defaultBlockState();





    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }



    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Blender blender,
            RandomState random,
            StructureManager structureManager,
            ChunkAccess chunk) {

        generateChunk(chunk, random);

        return CompletableFuture.completedFuture(chunk);
    }


    private void generateChunk(
            ChunkAccess chunk,
            RandomState random) {

        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();

        PositionalRandomFactory noiseRandom =
                random.getOrCreateRandomFactory(
                        ResourceLocation.fromNamespaceAndPath(
                                LostDreams.MOD_ID,
                                "chess_world"
                        )
                );

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {

                int x = startX + localX;
                int z = startZ + localZ;

                int surfaceY =
                        getTerrainHeight(
                                x,
                                z,
                                noiseRandom
                        );

                for (int y = MIN_Y; y <= surfaceY; y++) {

                    BlockPos pos =
                            new BlockPos(x, y, z);

                    if (isCave(
                            x,
                            y,
                            z,
                            noiseRandom,
                            surfaceY
                    )) {

                        chunk.setBlockState(
                                pos,
                                AIR,
                                false
                        );

                        continue;
                    }

                    if (y >= surfaceY - 3) {

                        chunk.setBlockState(
                                pos,
                                getChessBlock(x, z),
                                false
                        );

                    } else {

                        chunk.setBlockState(
                                pos,
                                getUndergroundChessBlock(x, z),
                                false
                        );
                    }
                }
            }
        }

        generateMonoliths(
                chunk,
                noiseRandom
        );
    }


    /*
     * =========================
     * РЕЛЬЕФ
     * =========================
     */

    private int getTerrainHeight(
            int x,
            int z,
            PositionalRandomFactory randomFactory) {

        double large =
                noise2D(
                        x * TERRAIN_SCALE,
                        z * TERRAIN_SCALE,
                        randomFactory
                );

        double detail =
                noise2D(
                        x * DETAIL_SCALE,
                        z * DETAIL_SCALE,
                        randomFactory
                );

        double terrain =
                large * 0.85
                        + detail * 0.15;

        int height =
                BASE_HEIGHT
                        + (int) (
                        terrain * TERRAIN_HEIGHT
                );

        return Mth.clamp(
                height,
                0,
                110
        );
    }


    /*
     * =========================
     * ПЕЩЕРЫ
     * =========================
     */

    private boolean isCave(
            int x,
            int y,
            int z,
            PositionalRandomFactory randomFactory,
            int surfaceY) {

        // Не создаём пещеры у самого низа мира
        if (y < MIN_Y + 8) {
            return false;
        }

        // За пределами terrain ничего не генерируем
        if (y > surfaceY) {
            return false;
        }

        double caveRegion =
                noise2D(
                        x * CAVE_REGION_SCALE,
                        z * CAVE_REGION_SCALE,
                        randomFactory
                );

        if (caveRegion < CAVE_REGION_THRESHOLD) {
            return false;
        }
        /*
         * =========================================================
         * 1. БОЛЬШИЕ КАМЕРЫ
         * =========================================================
         *
         * Низкая частота по X/Y/Z создаёт большие плавные
         * полости вместо вертикальных ям.
         */

        double chamberNoise =
                noise3D(
                        x * 0.012,
                        y * 0.010,
                        z * 0.012,
                        randomFactory
                );

        boolean chamber =
                Math.abs(chamberNoise) > 0.84;


        /*
         * =========================================================
         * 2. SPAGHETTI-ТУННЕЛЬ
         * =========================================================
         *
         * Используем два независимых шума.
         *
         * Чтобы блок стал частью тоннеля, оба шума должны
         * находиться близко к нулю.
         *
         * Это создаёт тонкие извилистые тоннели вместо
         * огромных вертикальных полостей.
         */

        double spaghettiA =
                noise3D(
                        x * 0.035,
                        y * 0.022,
                        z * 0.035,
                        randomFactory
                );

        double spaghettiB =
                noise3D(
                        x * 0.041 + 137.0,
                        y * 0.025 + 73.0,
                        z * 0.041 + 251.0,
                        randomFactory
                );

        double spaghettiDistance =
                Math.min(
                        Math.abs(spaghettiA),
                        Math.abs(spaghettiB)
                );

        boolean spaghetti =
                spaghettiDistance < 0.085;


        /*
         * =========================================================
         * 3. ДОПОЛНИТЕЛЬНЫЕ ИЗВИЛИСТЫЕ ТОННЕЛИ
         * =========================================================
         *
         * Более крупный шум иногда расширяет spaghetti,
         * создавая естественные соединения и небольшие
         * ответвления.
         */

        double tunnelShape =
                noise3D(
                        x * 0.018,
                        y * 0.016,
                        z * 0.018,
                        randomFactory
                );

        boolean tunnelExpansion =
                spaghetti
                        && Math.abs(tunnelShape) > 0.18;


        /*
         * =========================================================
         * 4. ОБЫЧНАЯ ПОДЗЕМНАЯ СИСТЕМА
         * =========================================================
         *
         * Камеры и тоннели объединяются в одну систему.
         */

        if (chamber || spaghetti || tunnelExpansion) {
            return true;
        }


        /*
         * =========================================================
         * 5. ВЫХОД НА ПОВЕРХНОСТЬ
         * =========================================================
         *
         * ВАЖНО:
         *
         * Мы НЕ создаём отдельный surface noise.
         *
         * Выход разрешён только рядом с уже существующим
         * spaghetti-тоннелем.
         */

        int depthFromSurface =
                surfaceY - y;

        if (depthFromSurface <= 14
                && depthFromSurface >= 0) {

            /*
             * Проверяем несколько точек ниже текущего блока.
             *
             * Это позволяет тоннелю постепенно подниматься
             * к поверхности, а не создавать дырку напрямую.
             */

            boolean tunnelBelow = false;

            for (int depth = 2; depth <= 10; depth += 2) {

                int checkY =
                        y - depth;

                if (checkY <= MIN_Y + 8) {
                    break;
                }

                double checkA =
                        noise3D(
                                x * 0.035,
                                checkY * 0.022,
                                z * 0.035,
                                randomFactory
                        );

                double checkB =
                        noise3D(
                                x * 0.041 + 137.0,
                                checkY * 0.025 + 73.0,
                                z * 0.041 + 251.0,
                                randomFactory
                        );

                double checkDistance =
                        Math.min(
                                Math.abs(checkA),
                                Math.abs(checkB)
                        );

                if (checkDistance < 0.105) {
                    tunnelBelow = true;
                    break;
                }
            }

            /*
             * Без тоннеля под нами выхода быть не может.
             */
            if (tunnelBelow) {

                /*
                 * Направление выхода слегка смещаем по X/Z,
                 * чтобы отверстия не выглядели вертикальными
                 * шахтами.
                 */

                double exitA =
                        noise2D(
                                x * 0.018 + z * 0.011,
                                z * 0.018 - x * 0.009,
                                randomFactory
                        );

                double exitB =
                        noise2D(
                                x * 0.031 - 42.0,
                                z * 0.031 + 91.0,
                                randomFactory
                        );

                /*
                 * Очень редкий выход.
                 *
                 * Оба шума должны попасть в подходящую область.
                 */
                boolean exitCandidate =
                        Math.abs(exitA) > 0.82
                                && Math.abs(exitB) > 0.55;

                /*
                 * Чем ближе к поверхности, тем легче тоннелю
                 * окончательно пробиться наружу.
                 */
                if (exitCandidate) {

                    double depthFactor =
                            (14.0 - depthFromSurface) / 14.0;

                    double finalNoise =
                            noise2D(
                                    x * 0.055,
                                    z * 0.055,
                                    randomFactory
                            );

                    double threshold =
                            0.92 - depthFactor * 0.12;

                    if (Math.abs(finalNoise) > threshold) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    /*
     * =========================
     * ШАХМАТНАЯ СЕТКА
     * =========================
     */

    private BlockState getChessBlock(
            int x,
            int z) {

        /*
         * floorDiv нужен специально:
         * он правильно работает и для
         * отрицательных координат.
         */
        int chessX = Math.floorDiv(x, CHESS_SIZE);
        int chessZ = Math.floorDiv(z, CHESS_SIZE);

        boolean white =
                ((chessX + chessZ) & 1) == 0;

        return white ? WHITE : BLACK;
    }


    private BlockState getUndergroundChessBlock(
            int x,
            int z) {

        int chessX = Math.floorDiv(x, CHESS_SIZE);
        int chessZ = Math.floorDiv(z, CHESS_SIZE);

        boolean light =
                ((chessX + chessZ) & 1) == 0;

        return light ? LIGHT_GRAY : GRAY;
    }


    /*
     * =========================
     * МОНОЛИТЫ
     * =========================
     */

    private void generateMonoliths(
            ChunkAccess chunk,
            PositionalRandomFactory randomFactory) {

        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        int gridX =
                Math.floorDiv(
                        chunkX,
                        MONOLITH_GRID
                );

        int gridZ =
                Math.floorDiv(
                        chunkZ,
                        MONOLITH_GRID
                );

        for (int gx = gridX - 1; gx <= gridX + 1; gx++) {
            for (int gz = gridZ - 1; gz <= gridZ + 1; gz++) {

                RandomSource random =
                        randomFactory.at(
                                gx,
                                0,
                                gz
                        );

                if (random.nextFloat() > MONOLITH_CHANCE) {
                    continue;
                }

                int cellMinX =
                        gx * MONOLITH_GRID * 16;

                int cellMinZ =
                        gz * MONOLITH_GRID * 16;

                int cellSize =
                        MONOLITH_GRID * 16;

                int centerX =
                        cellMinX
                                + random.nextInt(cellSize);

                int centerZ =
                        cellMinZ
                                + random.nextInt(cellSize);

                int size =
                        MONOLITH_MIN_SIZE
                                + random.nextInt(
                                MONOLITH_MAX_SIZE
                                        - MONOLITH_MIN_SIZE
                                        + 1
                        );

                int height =
                        MONOLITH_MIN_HEIGHT
                                + random.nextInt(
                                MONOLITH_MAX_HEIGHT
                                        - MONOLITH_MIN_HEIGHT
                                        + 1
                        );

                boolean hollow =
                        random.nextFloat() < 0.30f;

                int baseY = getLowestTerrainUnderMonolith(
                        centerX,
                        centerZ,
                        size,
                        randomFactory
                );

                generateMonolith(
                        chunk,
                        centerX,
                        baseY,
                        centerZ,
                        size,
                        height,
                        hollow
                );
            }
        }
    }

    private int getLowestTerrainUnderMonolith(
            int centerX,
            int centerZ,
            int size,
            PositionalRandomFactory randomFactory) {

        int minX = centerX - size / 2;
        int maxX = centerX + size / 2;

        int minZ = centerZ - size / 2;
        int maxZ = centerZ + size / 2;

        int lowestY = Integer.MAX_VALUE;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {

                int terrainY = getTerrainHeight(
                        x,
                        z,
                        randomFactory
                );

                lowestY = Math.min(lowestY, terrainY);
            }
        }

        return lowestY;
    }


    private void generateMonolith(
            ChunkAccess chunk,
            int centerX,
            int baseY,
            int centerZ,
            int size,
            int height,
            boolean hollow) {

        int minX =
                centerX - size / 2;

        int maxX =
                centerX + size / 2;

        int minZ =
                centerZ - size / 2;

        int maxZ =
                centerZ + size / 2;

        /*
         * Проверяем только область монолита,
         * которая может попасть в текущий чанк.
         */
        int chunkMinX =
                chunk.getPos().getMinBlockX();

        int chunkMaxX =
                chunkMinX + 15;

        int chunkMinZ =
                chunk.getPos().getMinBlockZ();

        int chunkMaxZ =
                chunkMinZ + 15;

        for (int x = minX; x <= maxX; x++) {

            if (x < chunkMinX || x > chunkMaxX) {
                continue;
            }

            for (int z = minZ; z <= maxZ; z++) {

                if (z < chunkMinZ || z > chunkMaxZ) {
                    continue;
                }

                for (
                        int y = baseY;
                        y <= baseY + height;
                        y++
                ) {



                    BlockState state;

                    int topY = baseY + height;

                    if (y == topY ) {

                        /*
                         * Наружная поверхность —
                         * шахматный узор.
                         */
                        state = getChessBlock(x, z);

                    }

                    else if (y < topY ) {

                        state = getUndergroundChessBlock(x, z);

                    }
                    else {
                        state = AIR;
                    }


                    chunk.setBlockState(
                            new BlockPos(x, y, z),
                            state,
                            false
                    );
                }
            }
        }
    }


    /*
     * =========================
     * SEED-ЗАВИСИМЫЙ 2D NOISE
     * =========================
     */

    private double noise2D(
            double x,
            double z,
            PositionalRandomFactory randomFactory) {

        int x0 = Mth.floor(x);
        int z0 = Mth.floor(z);

        int x1 = x0 + 1;
        int z1 = z0 + 1;

        double fx = fade(x - x0);
        double fz = fade(z - z0);

        double v00 =
                randomValue(
                        x0,
                        0,
                        z0,
                        randomFactory
                );

        double v10 =
                randomValue(
                        x1,
                        0,
                        z0,
                        randomFactory
                );

        double v01 =
                randomValue(
                        x0,
                        0,
                        z1,
                        randomFactory
                );

        double v11 =
                randomValue(
                        x1,
                        0,
                        z1,
                        randomFactory
                );

        double a =
                Mth.lerp(
                        fx,
                        v00,
                        v10
                );

        double b =
                Mth.lerp(
                        fx,
                        v01,
                        v11
                );

        return Mth.lerp(
                fz,
                a,
                b
        );
    }


    /*
     * =========================
     * SEED-ЗАВИСИМЫЙ 3D NOISE
     * =========================
     */

    private double noise3D(
            double x,
            double y,
            double z,
            PositionalRandomFactory randomFactory) {

        int x0 = Mth.floor(x);
        int y0 = Mth.floor(y);
        int z0 = Mth.floor(z);

        int x1 = x0 + 1;
        int y1 = y0 + 1;
        int z1 = z0 + 1;

        double fx = fade(x - x0);
        double fy = fade(y - y0);
        double fz = fade(z - z0);

        double v000 =
                randomValue(x0, y0, z0, randomFactory);

        double v100 =
                randomValue(x1, y0, z0, randomFactory);

        double v010 =
                randomValue(x0, y1, z0, randomFactory);

        double v110 =
                randomValue(x1, y1, z0, randomFactory);

        double v001 =
                randomValue(x0, y0, z1, randomFactory);

        double v101 =
                randomValue(x1, y0, z1, randomFactory);

        double v011 =
                randomValue(x0, y1, z1, randomFactory);

        double v111 =
                randomValue(x1, y1, z1, randomFactory);

        double x00 =
                Mth.lerp(fx, v000, v100);

        double x10 =
                Mth.lerp(fx, v010, v110);

        double x01 =
                Mth.lerp(fx, v001, v101);

        double x11 =
                Mth.lerp(fx, v011, v111);

        double y0Value =
                Mth.lerp(fy, x00, x10);

        double y1Value =
                Mth.lerp(fy, x01, x11);

        return Mth.lerp(
                fz,
                y0Value,
                y1Value
        );
    }


    /*
     * =========================
     * ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ
     * =========================
     */

    private double fade(double value) {

        /*
         * Smoothstep.
         */
        return value * value *
                (3.0 - 2.0 * value);
    }


    private double randomValue(
            int x,
            int y,
            int z,
            PositionalRandomFactory randomFactory) {

        RandomSource random =
                randomFactory.at(x, y, z);

        return random.nextDouble() * 2.0 - 1.0;
    }


    private double randomValue3D(
            int x,
            int y,
            int z,
            long seed) {

        long value =
                seed;

        value +=
                (long) x * 341873128712L;

        value +=
                (long) y * 132897987541L;

        value +=
                (long) z * 42317861L;

        value =
                (value ^ (value >> 30))
                        * 0xbf58476d1ce4e5b9L;

        value =
                (value ^ (value >> 27))
                        * 0x94d049bb133111ebL;

        value ^=
                value >> 31;

        return (value / (double) Long.MAX_VALUE);
    }


    /*
     * =========================
     * ОБЯЗАТЕЛЬНЫЕ МЕТОДЫ
     * =========================
     */

    @Override
    public void applyCarvers(
            WorldGenRegion level,
            long seed,
            RandomState random,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk,
            GenerationStep.Carving step) {

        /*
         * Пусто.
         *
         * Пещеры создаются нами самостоятельно
         * в generateChunk().
         */
    }


    @Override
    public void buildSurface(
            WorldGenRegion level,
            StructureManager structureManager,
            RandomState random,
            ChunkAccess chunk) {

        /*
         * Пусто.
         *
         * Поверхность уже создаётся
         * внутри fillFromNoise().
         */
    }


    @Override
    public void spawnOriginalMobs(
            WorldGenRegion level) {

    }


    @Override
    public int getGenDepth() {
        return GEN_DEPTH;
    }


    @Override
    public int getSeaLevel() {
        return -63;
    }


    @Override
    public int getMinY() {
        return MIN_Y;
    }




    @Override
    public int getBaseHeight(
            int x,
            int z,
            Heightmap.Types type,
            LevelHeightAccessor level,
            RandomState random) {

        PositionalRandomFactory randomFactory =
                random.getOrCreateRandomFactory(
                        ResourceLocation.fromNamespaceAndPath(
                                LostDreams.MOD_ID,
                                "chess_world"
                        )
                );

        return getTerrainHeight(
                x,
                z,
                randomFactory
        );
    }


    @Override
    public NoiseColumn getBaseColumn(
            int x,
            int z,
            LevelHeightAccessor level,
            RandomState random) {

        PositionalRandomFactory randomFactory =
                random.getOrCreateRandomFactory(
                        ResourceLocation.fromNamespaceAndPath(
                                LostDreams.MOD_ID,
                                "chess_world"
                        )
                );

        int height =
                getTerrainHeight(
                        x,
                        z,
                        randomFactory
                );

        int length =
                level.getMaxBuildHeight() - MIN_Y;

        BlockState[] states =
                new BlockState[length];

        for (int i = 0; i < length; i++) {

            int y = MIN_Y + i;

            if (y > height) {

                states[i] = AIR;

            } else {

                states[i] =
                        getChessBlock(x, z);
            }
        }

        return new NoiseColumn(
                MIN_Y,
                states
        );
    }


    @Override
    public void addDebugScreenInfo(
            List<String> info,
            RandomState random,
            BlockPos pos) {

        info.add(
                "Chess World"
        );


    }


}
