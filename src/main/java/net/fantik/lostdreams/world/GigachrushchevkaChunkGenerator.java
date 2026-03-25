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

    // =========================
    // 🌍 ОСНОВНАЯ ГЕНЕРАЦИЯ
    // =========================

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                        StructureManager sm, ChunkAccess chunk) {

        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;

        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {

                int wx = cx * 16 + lx;
                int wz = cz * 16 + lz;

                RandomSource rand = RandomSource.create(seed + wx * 341873128712L + wz * 132897987541L);

                // =========================
                // 🥔 ПОЛЯ
                // =========================

                boolean water = (wx % 6 == 0);

                for (int y = -64; y < 128; y++) {

                    if (y < 0) {
                        chunk.setBlockState(new BlockPos(wx, y, wz),
                                Blocks.DIRT.defaultBlockState(), false);
                    }

                    if (y == 0) {

                        if (water) {
                            chunk.setBlockState(new BlockPos(wx, y, wz),
                                    Blocks.WATER.defaultBlockState(), false);
                        } else {
                            chunk.setBlockState(new BlockPos(wx, y, wz),
                                    Blocks.FARMLAND.defaultBlockState(), false);

                            BlockState potato = Blocks.POTATOES.defaultBlockState()
                                    .setValue(CropBlock.AGE, 7);

                            chunk.setBlockState(new BlockPos(wx, y + 1, wz),
                                    potato, false);

                        }
                    }
                }

                // =========================
                // 🏢 МИКРОРАЙОНЫ
                // =========================

                int districtSize = 96;

                int dx = Math.floorDiv(wx, districtSize);
                int dz = Math.floorDiv(wz, districtSize);

                RandomSource dRand = RandomSource.create(seed + dx * 9999L + dz * 8888L);

                int buildings = 3 + dRand.nextInt(3);

                for (int i = 0; i < buildings; i++) {

                    int bx = dx * districtSize + 10 + dRand.nextInt(60);
                    int bz = dz * districtSize + 10 + dRand.nextInt(60);

                    int size = 10 + dRand.nextInt(6);

                    int floors = 15 + dRand.nextInt(6);
                    int height = floors * 3;

                    if (!(wx >= bx && wx < bx + size && wz >= bz && wz < bz + size)) continue;

                    for (int y = 128; y < 128 + height; y++) {

                        boolean wall =
                                wx == bx || wx == bx + size - 1 ||
                                        wz == bz || wz == bz + size - 1;

                        if (wall) {

                            boolean window = (y % 3 == 1) && dRand.nextFloat() > 0.3f;

                            if (window) {
                                chunk.setBlockState(new BlockPos(wx, y, wz),
                                        Blocks.GLASS.defaultBlockState(), false);
                            } else {
                                chunk.setBlockState(new BlockPos(wx, y, wz),
                                        Blocks.STONE_BRICKS.defaultBlockState(), false);
                            }

                            // балконы
                            if (y % 3 == 1 && dRand.nextFloat() > 0.75f) {
                                BlockPos out = new BlockPos(wx, y, wz);
                                chunk.setBlockState(out,
                                        Blocks.IRON_BARS.defaultBlockState(), false);
                            }
                        }

                        // перекрытия
                        if (y % 3 == 0) {
                            chunk.setBlockState(new BlockPos(wx, y, wz),
                                    Blocks.SMOOTH_STONE.defaultBlockState(), false);
                        }
                    }

                    // подъезд (лестница)
                    int sx = bx + size / 2;
                    int sz = bz + size / 2;

                    if (wx == sx && wz == sz) {
                        for (int y = 128; y < 128 + height; y++) {
                            chunk.setBlockState(new BlockPos(wx, y, wz),
                                    Blocks.OAK_STAIRS.defaultBlockState(), false);
                        }
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    // =========================
    // ⚙ ОБЯЗАТЕЛЬНЫЕ МЕТОДЫ
    // =========================

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager sm,
                             RandomState random, ChunkAccess chunk) {}

    @Override
    public void applyCarvers(WorldGenRegion level, long seed,
                             RandomState random, BiomeManager bm,
                             StructureManager sm, ChunkAccess chunk,
                             GenerationStep.Carving step) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {}

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type,
                             LevelHeightAccessor level, RandomState random) {
        return 64;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z,
                                     LevelHeightAccessor level, RandomState random) {
        return new NoiseColumn(-64, new BlockState[0]);
    }

    @Override public int getGenDepth() { return 640; }
    @Override public int getSeaLevel() { return 0; }
    @Override public int getMinY() { return -64; }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("GIGA HRUSHCHEVKA STABLE GEN");
    }
}