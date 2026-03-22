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
            case RED      -> ModBlocks.SURREAL_RED_ROCK.get().defaultBlockState();
            case GREEN    -> ModBlocks.SURREAL_GREEN_ROCK.get().defaultBlockState();
            case PURPLE   -> ModBlocks.SURREAL_PURPLE_ROCK.get().defaultBlockState();
            case LIGHTBLUE -> ModBlocks.SURREAL_LIGHTBLUE_ROCK.get().defaultBlockState();
            case BLUE     -> ModBlocks.SURREAL_BLUE_ROCK.get().defaultBlockState();
            case YELLOW   -> ModBlocks.SURREAL_YELLOW_ROCK.get().defaultBlockState();
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

        ShapeType shape = ShapeType.values()[random.nextInt(ShapeType.values().length)];

        // Базовый радиус
        int baseR = 3 + random.nextInt(12);

        // Корректируем размер по форме
        int r = switch (shape) {
            case CUBE                  -> Math.max(3, (int)(baseR * 0.65f)); // меньше
            case TORUS, CONE, PYRAMID  -> (int)(baseR * 1.35f);              // больше
            case KETTLEBELL, DUMBBELL, CHAIN -> (int)(baseR * 1.2f);         // чуть больше — сложные формы
            default                    -> baseR;
        };

        boolean hollow = random.nextFloat() < 0.04f && r >= 10;
        boolean large  = r >= 10;

        NormalNoise noise = NormalNoise.create(random, 0, 1.0);

        int caveOffsetX = random.nextInt(Math.max(1, r / 2)) - r / 4;
        int caveOffsetY = random.nextInt(Math.max(1, r / 2)) - r / 4;
        int caveOffsetZ = random.nextInt(Math.max(1, r / 2)) - r / 4;

        for (int x = -r * 2; x <= r * 2; x++) {
            for (int y = -r * 2; y <= r * 2; y++) {
                for (int z = -r * 2; z <= r * 2; z++) {

                    if (!isInsideShape(shape, x, y, z, r)) continue;

                    int distSq = x*x + y*y + z*z;

                    if (hollow && distSq < r*r * 0.6) continue;

                    if (!large) {
                        level.setBlock(base.offset(x, y, z), rock, 2);
                        continue;
                    }

                    double nx = (x + caveOffsetX) * 0.12;
                    double ny = (y + caveOffsetY) * 0.12;
                    double nz = (z + caveOffsetZ) * 0.12;
                    double noiseVal = noise.getValue(nx, ny, nz);

                    if (noiseVal > 0.45 && distSq < r*r * 0.5) continue;

                    double tunnel = noise.getValue(nx * 2, ny * 2, nz * 2);
                    if (Math.abs(tunnel) < 0.008 && distSq < r*r * 0.35) continue;

                    level.setBlock(base.offset(x, y, z), rock, 2);
                }
            }
        }

        if (large) {
            placeCrystals(level, base, r, shape, random);
        }
    }

    private void placeCrystals(WorldGenLevel level, BlockPos base, int r,
                               ShapeType shape, RandomSource random) {

        BlockState crystal = ModBlocks.SURREAL_GLOWCRYSTAL.get().defaultBlockState();

        for (int x = -r * 2; x <= r * 2; x++) {
            for (int z = -r * 2; z <= r * 2; z++) {

                // --- ПОТОЛОК: сверху вниз ---
                for (int y = r * 2; y > -r * 2; y--) {
                    BlockPos solid = base.offset(x, y, z);
                    BlockPos air   = base.offset(x, y - 1, z);

                    if (!isInsideShape(shape, x, y, z, r)) continue;
                    if (!isInsideShape(shape, x, y - 1, z, r)) continue;

                    if (!level.getBlockState(solid).isSolid()) continue;
                    if (!level.getBlockState(air).isAir()) continue;

                    if (random.nextFloat() < 0.08f) {
                        level.setBlock(air, crystal, 2);
                    }
                    break;
                }

                // --- ПОЛ: снизу вверх ---
                for (int y = -r * 2; y < r * 2; y++) {
                    BlockPos solid = base.offset(x, y, z);
                    BlockPos air   = base.offset(x, y + 1, z);

                    if (!isInsideShape(shape, x, y, z, r)) continue;
                    if (!isInsideShape(shape, x, y + 1, z, r)) continue;

                    if (!level.getBlockState(solid).isSolid()) continue;
                    if (!level.getBlockState(air).isAir()) continue;

                    if (random.nextFloat() < 0.04f) {
                        level.setBlock(air, crystal, 2);
                    }
                    break;
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
                return y >= -r && y <= r && Math.abs(x) + Math.abs(z) <= (r - Math.abs(y));

            case CONE:
                if (y < -r || y > r) return false;
                double coneRadius = (1.0 - (double)(y + r) / (2 * r)) * r;
                return x*x + z*z <= coneRadius * coneRadius;

            case TORUS: {
                double q = Math.sqrt(x*x + z*z) - r * 0.6;
                return q*q + y*y <= (r * 0.35) * (r * 0.35);
            }

            case SATURN: {
                boolean sphere = x*x + y*y + z*z <= r*r;
                double ring = Math.sqrt(x*x + z*z);
                boolean disk = ring > r * 0.8 && ring < r * 1.4 && Math.abs(y) < r * 0.2;
                return sphere || disk;
            }

            case KETTLEBELL: {
                // Шар сверху + ручка снизу (прямоугольник)
                int ballR = (int)(r * 0.75);
                int ballCenterY = (int)(r * 0.2);
                boolean ball = (x*x + (y - ballCenterY)*(y - ballCenterY) + z*z) <= ballR*ballR;

                // Ручка — дуга над шаром
                int handleY = ballCenterY + ballR;
                double handleDist = Math.sqrt(x*x + z*z) - r * 0.35;
                boolean handle = handleDist*handleDist + (y - handleY - r*0.25)*(y - handleY - r*0.25)
                        <= (r * 0.18) * (r * 0.18)
                        && y >= handleY;

                return ball || handle;
            }

            case DUMBBELL: {
                // Два шара по бокам по оси X + тонкая перекладина между ними
                int ballR = (int)(r * 0.55);
                int centerOffset = (int)(r * 0.75);

                boolean ball1 = (x - centerOffset)*(x - centerOffset) + y*y + z*z <= ballR*ballR;
                boolean ball2 = (x + centerOffset)*(x + centerOffset) + y*y + z*z <= ballR*ballR;

                // Перекладина между шарами
                boolean bar = Math.abs(x) <= centerOffset
                        && y*y + z*z <= (r * 0.15) * (r * 0.15);

                return ball1 || ball2 || bar;
            }

            case CHAIN: {
                // Три звена цепи — чередующиеся торусы по осям XZ и XY
                int linkSpacing = (int)(r * 0.7);

                // Звено 1 — торус в плоскости XZ (центр по Y = -linkSpacing)
                double q1 = Math.sqrt(x*x + z*z) - r * 0.4;
                boolean link1 = q1*q1 + (y + linkSpacing)*(y + linkSpacing)
                        <= (r * 0.2) * (r * 0.2);

                // Звено 2 — торус в плоскости XY (центр по Y = 0), повёрнутый на 90°
                double q2 = Math.sqrt(x*x + y*y) - r * 0.4;
                boolean link2 = q2*q2 + z*z <= (r * 0.2) * (r * 0.2);

                // Звено 3 — торус в плоскости XZ (центр по Y = +linkSpacing)
                double q3 = Math.sqrt(x*x + z*z) - r * 0.4;
                boolean link3 = q3*q3 + (y - linkSpacing)*(y - linkSpacing)
                        <= (r * 0.2) * (r * 0.2);

                return link1 || link2 || link3;
            }
        }

        return false;
    }
}