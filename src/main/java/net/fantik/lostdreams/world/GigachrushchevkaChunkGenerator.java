package net.fantik.lostdreams.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fantik.lostdreams.LostDreams;

import net.minecraft.core.BlockPos;
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
 * Облегченный генератор мира "Lost Dreams".
 * Генерирует только бесконечные картофельные поля и систему освещения над ними.
 * Сами хрущевки теперь должны добавляться через JSON структуры (Jigsaw).
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

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = cx * 16 + lx;
                int wz = cz * 16 + lz;

                // 1. Генерация недр (камень и земля)
                for (int y = -64; y < 0; y++) {
                    BlockPos p = new BlockPos(wx, y, wz);
                    if (y < -3) {
                        setBlockInChunk(chunk, p, Blocks.STONE.defaultBlockState());
                    } else {
                        setBlockInChunk(chunk, p, Blocks.DIRT.defaultBlockState());
                    }
                }

                // 2. Генерация поверхности (Картофельные поля и вода)
                BlockPos surfacePos = new BlockPos(wx, 0, wz);

                // Рандомная логика для грядок (каждый 6-й ряд - вода для полива)
                if (wx % 6 == 0) {
                    setBlockInChunk(chunk, surfacePos, Blocks.WATER.defaultBlockState());
                } else {
                    // Пашня
                    setBlockInChunk(chunk, surfacePos, Blocks.FARMLAND.defaultBlockState());

                    // Созревший картофель (Age 7)
                    setBlockInChunk(chunk, surfacePos.above(),
                            Blocks.POTATOES.defaultBlockState().setValue(CropBlock.AGE, 7));

                    // Магический свет над полями (чтобы картошка росла в пустоте)
                    setBlockInChunk(chunk, surfacePos.above(2),
                            Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15));
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    /**
     * Безопасная установка блока в чанк.
     */
    private void setBlockInChunk(ChunkAccess chunk, BlockPos pos, BlockState state) {
        chunk.setBlockState(pos, state, false);
    }

    @Override public void buildSurface(WorldGenRegion level, StructureManager sm, RandomState random, ChunkAccess chunk) {}
    @Override public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager bm, StructureManager sm, ChunkAccess chunk, GenerationStep.Carving step) {}
    @Override public void spawnOriginalMobs(WorldGenRegion level) {}

    @Override public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return 1; // Поверхность на уровне 0
    }

    @Override public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        return new NoiseColumn(-64, new BlockState[0]);
    }

    @Override public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {}
    @Override public int getGenDepth() { return 640; }
    @Override public int getSeaLevel() { return 0; }
    @Override public int getMinY() { return -64; }
}