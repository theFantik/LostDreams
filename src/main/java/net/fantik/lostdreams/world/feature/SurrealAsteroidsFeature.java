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
        SPHERE, CUBE, PYRAMID, CONE, TORUS, SATURN,
        KETTLEBELL, DUMBBELL, CHAIN
    }

    // 8 блоков — безопасный радиус который не вызывает ошибок
    private static final int MAX_RADIUS = 8;

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
            case RED       -> ModBlocks.SURREAL_RED_ROCK.get().defaultBlockState();
            case GREEN     -> ModBlocks.SURREAL_GREEN_ROCK.get().defaultBlockState();
            case PURPLE    -> ModBlocks.SURREAL_PURPLE_ROCK.get().defaultBlockState();
            case LIGHTBLUE -> ModBlocks.SURREAL_LIGHTBLUE_ROCK.get().defaultBlockState();
            case BLUE      -> ModBlocks.SURREAL_BLUE_ROCK.get().defaultBlockState();
            case YELLOW    -> ModBlocks.SURREAL_YELLOW_ROCK.get().defaultBlockState();
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
        int offset = 4 + random.nextInt(6);
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
        ShapeType shape = ShapeType.values()[random.nextInt(ShapeType.values().length)];

        float sizeRoll = random.nextFloat();
        int baseR;
        if (sizeRoll < 0.40f) {
            baseR = 2 + random.nextInt(3);        // маленькие (2-4)
        } else if (sizeRoll < 0.75f) {
            baseR = 4 + random.nextInt(3);        // средние (4-6)
        } else {
            baseR = 6 + random.nextInt(3);        // крупные (6-8)
        }

        int r = Math.min(MAX_RADIUS, switch (shape) {
            case CUBE                        -> Math.max(2, (int)(baseR * 0.65f));
            case TORUS, CONE, PYRAMID        -> (int)(baseR * 1.1f);
            case KETTLEBELL, DUMBBELL, CHAIN -> (int)(baseR * 1.0f);
            default                          -> baseR;
        });

        int bound = r;
        boolean hollow = random.nextFloat() < 0.04f && r >= 5;
        boolean large  = r >= 5;

        NormalNoise noise = NormalNoise.create(random, 0, 1.0);
        int caveOffsetX = random.nextInt(Math.max(1, r / 2)) - r / 4;
        int caveOffsetY = random.nextInt(Math.max(1, r / 2)) - r / 4;
        int caveOffsetZ = random.nextInt(Math.max(1, r / 2)) - r / 4;

        for (int x = -bound; x <= bound; x++) {
            for (int y = -bound; y <= bound; y++) {
                for (int z = -bound; z <= bound; z++) {
                    if (!isInsideShape(shape, x, y, z, r)) continue;

                    int distSq = x*x + y*y + z*z;
                    if (hollow && distSq < r*r * 0.6) continue;

                    BlockPos target = base.offset(x, y, z);

                    if (!large) {
                        level.setBlock(target, rock, 2);
                        continue;
                    }

                    double nx = (x + caveOffsetX) * 0.12;
                    double ny = (y + caveOffsetY) * 0.12;
                    double nz = (z + caveOffsetZ) * 0.12;
                    double noiseVal = noise.getValue(nx, ny, nz);
                    if (noiseVal > 0.45 && distSq < r*r * 0.5) continue;

                    double tunnel = noise.getValue(nx * 2, ny * 2, nz * 2);
                    if (Math.abs(tunnel) < 0.008 && distSq < r*r * 0.35) continue;

                    level.setBlock(target, rock, 2);
                }
            }
        }

        if (large) {
            placeCrystals(level, base, r, bound, shape, random);
        }
    }

    private void placeCrystals(WorldGenLevel level, BlockPos base, int r, int bound,
                               ShapeType shape, RandomSource random) {
        BlockState crystal = ModBlocks.SURREAL_GLOWCRYSTAL.get().defaultBlockState();

        for (int x = -bound; x <= bound; x++) {
            for (int z = -bound; z <= bound; z++) {
                // Потолок
                for (int y = bound; y > -bound; y--) {
                    if (!isInsideShape(shape, x, y, z, r)) continue;
                    if (!isInsideShape(shape, x, y - 1, z, r)) continue;
                    BlockPos solid = base.offset(x, y, z);
                    BlockPos air   = base.offset(x, y - 1, z);
                    if (!level.getBlockState(solid).isSolid()) continue;
                    if (!level.getBlockState(air).isAir()) continue;
                    if (random.nextFloat() < 0.08f) level.setBlock(air, crystal, 2);
                    break;
                }

                // Пол
                for (int y = -bound; y < bound; y++) {
                    if (!isInsideShape(shape, x, y, z, r)) continue;
                    if (!isInsideShape(shape, x, y + 1, z, r)) continue;
                    BlockPos solid = base.offset(x, y, z);
                    BlockPos air   = base.offset(x, y + 1, z);
                    if (!level.getBlockState(solid).isSolid()) continue;
                    if (!level.getBlockState(air).isAir()) continue;
                    if (random.nextFloat() < 0.04f) level.setBlock(air, crystal, 2);
                    break;
                }
            }
        }
    }

    private boolean isInsideShape(ShapeType shape, int x, int y, int z, int r) {
        return switch (shape) {
            case SPHERE -> x*x + y*y + z*z <= r*r;

            case CUBE -> Math.abs(x) <= r && Math.abs(y) <= r && Math.abs(z) <= r;

            case PYRAMID -> y >= -r && y <= r && Math.abs(x) + Math.abs(z) <= (r - Math.abs(y));

            case CONE -> {
                if (y < -r || y > r) yield false;
                double cr = (1.0 - (double)(y + r) / (2 * r)) * r;
                yield x*x + z*z <= cr * cr;
            }

            case TORUS -> {
                double q = Math.sqrt(x*x + z*z) - r * 0.6;
                yield q*q + y*y <= (r * 0.35) * (r * 0.35);
            }

            case SATURN -> {
                boolean sphere = x*x + y*y + z*z <= r*r;
                double ring = Math.sqrt(x*x + z*z);
                boolean disk = ring > r * 0.8 && ring < r * 1.2 && Math.abs(y) < r * 0.2;
                yield sphere || disk;
            }

            case KETTLEBELL -> {
                int ballR = (int)(r * 0.75);
                int ballCenterY = (int)(r * 0.2);
                boolean ball = x*x + (y-ballCenterY)*(y-ballCenterY) + z*z <= ballR*ballR;
                int handleY = ballCenterY + ballR;
                double hd = Math.sqrt(x*x + z*z) - r * 0.35;
                boolean handle = hd*hd + (y-handleY-r*0.25)*(y-handleY-r*0.25)
                        <= (r*0.18)*(r*0.18) && y >= handleY;
                yield ball || handle;
            }

            case DUMBBELL -> {
                int ballR = (int)(r * 0.45);
                int co = (int)(r * 0.55);
                boolean b1 = (x-co)*(x-co) + y*y + z*z <= ballR*ballR;
                boolean b2 = (x+co)*(x+co) + y*y + z*z <= ballR*ballR;
                boolean bar = Math.abs(x) <= co && y*y + z*z <= (r*0.15)*(r*0.15);
                yield b1 || b2 || bar;
            }

            case CHAIN -> {
                int ls = (int)(r * 0.5);
                double q1 = Math.sqrt(x*x + z*z) - r * 0.4;
                boolean l1 = q1*q1 + (y+ls)*(y+ls) <= (r*0.2)*(r*0.2);
                double q2 = Math.sqrt(x*x + y*y) - r * 0.4;
                boolean l2 = q2*q2 + z*z <= (r*0.2)*(r*0.2);
                double q3 = Math.sqrt(x*x + z*z) - r * 0.4;
                boolean l3 = q3*q3 + (y-ls)*(y-ls) <= (r*0.2)*(r*0.2);
                yield l1 || l2 || l3;
            }
        };
    }
}