package net.fantik.lostdreams.world;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.util.AsteroidGroupUtil;
import net.fantik.lostdreams.util.AsteroidUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SurrealAsteroidsChunkGenerator extends ChunkGenerator {

    public static final MapCodec<SurrealAsteroidsChunkGenerator> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource)
                    ).apply(instance, SurrealAsteroidsChunkGenerator::new));

    // Шаг сетки астероидов в чанках — каждые N чанков один потенциальный астероид
    private static final int GRID_STEP = 2;
    // Максимальный радиус
    private static final int MAX_RADIUS = 30;

    public SurrealAsteroidsChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public static void register(RegisterEvent event) {
        event.register(
                net.minecraft.core.registries.Registries.CHUNK_GENERATOR,
                ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "surreal_asteroids"),
                () -> CODEC
        );
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState random,
                                                        StructureManager structureManager,
                                                        ChunkAccess chunk) {
        generateAsteroids(chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    private void generateAsteroids(ChunkAccess chunk) {
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        // 1. Вычисляем координаты ЯЧЕЙКИ сетки, в которой находится текущий чанк
        int currentGridX = Math.floorDiv(chunkX, GRID_STEP);
        int currentGridZ = Math.floorDiv(chunkZ, GRID_STEP);

        // 2. Задаем радиус поиска в ЯЧЕЙКАХ сетки, а не в чанках.
        // 1 ячейка = GRID_STEP * 16 = 32 блока.
        // Радиус 2 ячейки = 64 блока во все стороны. Этого с запасом хватит для MAX_RADIUS (30) + смещения групп.
        // Если группы разлетаются еще дальше, увеличь до 3.
        int gridSearchRadius = 2;

        for (int gridX = currentGridX - gridSearchRadius; gridX <= currentGridX + gridSearchRadius; gridX++) {
            for (int gridZ = currentGridZ - gridSearchRadius; gridZ <= currentGridZ + gridSearchRadius; gridZ++) {

                // Сид привязан строго к ячейке сетки, поэтому астероид всегда будет на одном месте
                long seed = (long) gridX * 341873128712L + (long) gridZ * 132897987541L;
                RandomSource rand = new XoroshiroRandomSource(seed);

                // Шанс появления астероида в этой ячейке
                if (rand.nextFloat() > 0.6f) continue;

                int cellMinX = gridX * GRID_STEP * 16;
                int cellMinZ = gridZ * GRID_STEP * 16;
                int cellSize = GRID_STEP * 16;

                int asteroidX = cellMinX + rand.nextInt(cellSize);
                int asteroidY = -32 + rand.nextInt(224);
                int asteroidZ = cellMinZ + rand.nextInt(cellSize);

                BlockPos base = new BlockPos(asteroidX, asteroidY, asteroidZ);

                AsteroidUtil.AsteroidType type =
                        AsteroidUtil.AsteroidType.values()[rand.nextInt(AsteroidUtil.AsteroidType.values().length)];
                AsteroidUtil.ShapeType shape =
                        AsteroidUtil.ShapeType.values()[rand.nextInt(AsteroidUtil.ShapeType.values().length)];

                float sizeRoll = rand.nextFloat();
                int baseR;
                if (sizeRoll < 0.35f) {
                    baseR = 4 + rand.nextInt(4);        // маленькие (4-7)
                } else if (sizeRoll < 0.65f) {
                    baseR = 8 + rand.nextInt(6);        // средние (8-14)
                } else if (sizeRoll < 0.88f) {
                    baseR = 14 + rand.nextInt(6);       // большие (14-19)
                } else {
                    baseR = 19 + rand.nextInt(6);       // огромные (19-24)
                }

                int r = Math.min(MAX_RADIUS, AsteroidUtil.getRadius(shape, baseR));

                // Двойной астероид
                if (rand.nextFloat() < 0.15f) {
                    int offset = 4 + rand.nextInt(8);
                    BlockPos second = base.offset(
                            rand.nextInt(offset) - offset / 2,
                            rand.nextInt(offset) - offset / 2,
                            rand.nextInt(offset) - offset / 2
                    );
                    AsteroidUtil.generateAsteroidInChunk(chunk, second, r, shape, type, rand);
                }

                // Группы астероидов
                if (rand.nextFloat() < 0.25f) {
                    boolean sameShape = rand.nextBoolean();
                    boolean sameColor = !sameShape;
                    AsteroidGroupUtil.generateGroup(chunk, base, r, rand, sameShape, sameColor);
                } else {
                    AsteroidUtil.generateAsteroidInChunk(chunk, base, r, shape, type, rand);
                }
            }
        }
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random,
                             BiomeManager biomeManager, StructureManager structureManager,
                             ChunkAccess chunk, GenerationStep.Carving step) {}

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager,
                             RandomState random, ChunkAccess chunk) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {}

    @Override
    public int getGenDepth() { return 384; }

    @Override
    public int getSeaLevel() { return -63; }

    @Override
    public int getMinY() { return -64; }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type,
                             LevelHeightAccessor level, RandomState random) { return 0; }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level,
                                     RandomState random) {
        return new NoiseColumn(0, new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {}
}