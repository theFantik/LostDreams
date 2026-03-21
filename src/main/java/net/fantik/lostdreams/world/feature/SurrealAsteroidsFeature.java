package net.fantik.lostdreams.world.feature;

import com.mojang.serialization.Codec;
import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class SurrealAsteroidsFeature extends Feature<NoneFeatureConfiguration> {

    public SurrealAsteroidsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private enum AsteroidType { RED, BLUE, YELLOW, PURPLE, LIGHTBLUE, GREEN }

    private enum ShapeType {
        SPHERE, CUBE, PYRAMID, CONE, TORUS, SATURN
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int asteroidY = -32 + random.nextInt(224);
        BlockPos base = new BlockPos(origin.getX(), asteroidY, origin.getZ());

        if (!level.getBlockState(base).isAir()) return false;

        AsteroidType type = AsteroidType.values()[random.nextInt(AsteroidType.values().length)];
        BlockState rock = switch (type) {
            case RED -> ModBlocks.SURREAL_RED_ROCK.get().defaultBlockState();
            case GREEN -> ModBlocks.SURREAL_GREEN_ROCK.get().defaultBlockState();
            case PURPLE -> ModBlocks.SURREAL_PURPLE_ROCK.get().defaultBlockState();
            case LIGHTBLUE -> ModBlocks.SURREAL_LIGHTBLUE_ROCK.get().defaultBlockState();
            case BLUE -> ModBlocks.SURREAL_BLUE_ROCK.get().defaultBlockState();
            case YELLOW -> ModBlocks.SURREAL_YELLOW_ROCK.get().defaultBlockState();
        };

        if (random.nextFloat() < 0.25f) {
            generateDoubleAsteroid(level, base, random, rock);
        } else {
            generateAsteroid(level, base, random, rock);
        }

        return true;
    }

    private void generateDoubleAsteroid(WorldGenLevel level, BlockPos base,
                                        RandomSource random, BlockState rock) {

        int offset = 6 + random.nextInt(10);

        BlockPos second = base.offset(
                random.nextInt(offset) - offset / 2,
                random.nextInt(offset) - offset / 2,
                random.nextInt(offset) - offset / 2
        );

        generateAsteroid(level, base, random, rock);
        generateAsteroid(level, second, random, rock);
    }

    private void generateAsteroid(WorldGenLevel level, BlockPos base,
                                  RandomSource random, BlockState rock) {

        int r = 3 + random.nextInt(12);

        ShapeType shape = ShapeType.values()[random.nextInt(ShapeType.values().length)];

        //  шанс полого астероида
        boolean hollow = random.nextFloat() < 0.12f && r >= 6;

        //  система размеров
        boolean small = r < 6;
        boolean medium = r >= 6 && r < 10;
        boolean large = r >= 10;

        NormalNoise noise = NormalNoise.create(random, 0, 1.0);

        int caveOffsetX = random.nextInt(r / 2) - r / 4;
        int caveOffsetY = random.nextInt(r / 2) - r / 4;
        int caveOffsetZ = random.nextInt(r / 2) - r / 4;

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {

                    if (!isInsideShape(shape, x, y, z, r)) continue;

                    int distSq = x*x + y*y + z*z;

                    //  ПОЛЫЙ АСТЕРОИД
                    if (hollow) {
                        if (distSq < r*r * 0.6) continue;
                    }

                    //  маленькие — без пещер
                    if (small) {
                        level.setBlock(base.offset(x, y, z), rock, 2);
                        continue;
                    }

                    double nx = (x + caveOffsetX) * 0.12;
                    double ny = (y + caveOffsetY) * 0.12;
                    double nz = (z + caveOffsetZ) * 0.12;

                    double noiseVal = noise.getValue(nx, ny, nz);

                    //  СРЕДНИЕ — немного пещер
                    if (medium) {
                        if (noiseVal > 0.25 && distSq < r*r * 0.5) continue;
                    }

                    //  БОЛЬШИЕ — полноценные пещеры + туннели
                    if (large) {
                        if (noiseVal > 0.2 && distSq < r*r * 0.65) continue;

                        double tunnel = noise.getValue(nx * 2, ny * 2, nz * 2);
                        if (Math.abs(tunnel) < 0.02 && distSq < r*r * 0.5) continue;
                    }

                    level.setBlock(base.offset(x, y, z), rock, 2);
                }
            }
        }
    }

    private boolean isInsideShape(ShapeType shape, int x, int y, int z, int r) {
        switch (shape) {

            case SPHERE:
                return x*x + y*y + z*z <= r*r;

            case CUBE:
                return Math.abs(x) <= r && Math.abs(y) <= r && Math.abs(z) <= r;

            case PYRAMID:
                return Math.abs(x) + Math.abs(z) <= (r - Math.abs(y));

            case CONE:
                if (y < 0 || y > r) return false;
                double radius = (1.0 - (double)y / r) * r;
                return x*x + z*z <= radius * radius;

            case TORUS:
                double q = Math.sqrt(x*x + z*z) - r * 0.6;
                return q*q + y*y <= (r * 0.3) * (r * 0.3);

            case SATURN:
                boolean sphere = x*x + y*y + z*z <= r*r;
                double ring = Math.sqrt(x*x + z*z);
                boolean disk = ring > r * 0.8 && ring < r * 1.4 && Math.abs(y) < r * 0.2;
                return sphere || disk;
        }

        return false;
    }
}