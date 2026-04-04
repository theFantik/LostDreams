package net.fantik.lostdreams.util;

import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class AsteroidUtil {

    public enum AsteroidType { RED, BLUE, YELLOW, PURPLE, LIGHTBLUE, GREEN }

    public enum ShapeType {
        SPHERE, CUBE, PYRAMID, CONE, TORUS, SATURN,
        KETTLEBELL, DUMBBELL, CHAIN, STELLATED
    }

    public static BlockState getRock(AsteroidType type) {
        return switch (type) {
            case RED       -> ModBlocks.SURREAL_RED_ROCK.get().defaultBlockState();
            case GREEN     -> ModBlocks.SURREAL_GREEN_ROCK.get().defaultBlockState();
            case PURPLE    -> ModBlocks.SURREAL_PURPLE_ROCK.get().defaultBlockState();
            case LIGHTBLUE -> ModBlocks.SURREAL_LIGHTBLUE_ROCK.get().defaultBlockState();
            case BLUE      -> ModBlocks.SURREAL_BLUE_ROCK.get().defaultBlockState();
            case YELLOW    -> ModBlocks.SURREAL_YELLOW_ROCK.get().defaultBlockState();
        };
    }

    public static BlockState getOre(AsteroidType type) {
        return switch (type) {
            case RED       -> ModBlocks.SURREAL_RED_RANDOMITE_ORE.get().defaultBlockState();
            case GREEN     -> ModBlocks.SURREAL_GREEN_RANDOMITE_ORE.get().defaultBlockState();
            case PURPLE    -> ModBlocks.SURREAL_PURPLE_RANDOMITE_ORE.get().defaultBlockState();
            case LIGHTBLUE -> ModBlocks.SURREAL_LIGHTBLUE_RANDOMITE_ORE.get().defaultBlockState();
            case BLUE      -> ModBlocks.SURREAL_BLUE_RANDOMITE_ORE.get().defaultBlockState();
            case YELLOW    -> ModBlocks.SURREAL_YELLOW_RANDOMITE_ORE.get().defaultBlockState();
        };
    }

    public static int getRadius(ShapeType shape, int baseR) {
        return switch (shape) {
            case CUBE                        -> Math.max(2, (int)(baseR * 0.65f));
            case TORUS, CONE, PYRAMID        -> (int)(baseR * 1.1f);
            case KETTLEBELL, DUMBBELL, CHAIN, STELLATED -> baseR;
            default                          -> baseR;
        };
    }

    private static float getDeterministicFloat(int x, int y, int z, long baseSeed) {
        long hash = (long) x * 73856093L ^ (long) y * 19128883L ^ (long) z * 82468361L ^ baseSeed;
        hash = (hash ^ (hash >>> 33)) * 0xff51afd7ed558ccdL;
        hash = (hash ^ (hash >>> 33)) * 0xc4ceb9fe1a85ec53L;
        hash = hash ^ (hash >>> 33);
        return (Math.abs(hash) % 1000000) / 1000000.0f;
    }

    public static void generateAsteroidInChunk(ChunkAccess chunk, BlockPos base,
                                               int r, ShapeType shape,
                                               AsteroidType type, RandomSource chunkRandom) {
        BlockState rock = getRock(type);
        BlockState ore = getOre(type);

        int chunkMinX = chunk.getPos().getMinBlockX();
        int chunkMaxX = chunk.getPos().getMaxBlockX();
        int chunkMinZ = chunk.getPos().getMinBlockZ();
        int chunkMaxZ = chunk.getPos().getMaxBlockZ();

        int minY = chunk.getMinBuildHeight();
        int maxY = chunk.getMaxBuildHeight();

        // ИСПРАВЛЕНИЕ: Создаем персональный генератор для этого конкретного астероида!
        // Это навсегда избавит от проблем с "разрезанными" пещерами на границах чанков.
        long asteroidSeed = base.asLong();
        RandomSource astRandom = RandomSource.create(asteroidSeed);

        boolean large = r >= 12;

        // Полые астероиды: радиус не меньше 7, шанс 15%
        boolean hollow = r >= 7 && astRandom.nextFloat() < 0.15f;

        // Создаем шум на основе сида астероида, а не чанка!
        NormalNoise noise = NormalNoise.create(astRandom, 0, 1.0);
        int caveOffsetX = astRandom.nextInt(Math.max(1, r / 2)) - r / 4;
        int caveOffsetY = astRandom.nextInt(Math.max(1, r / 2)) - r / 4;
        int caveOffsetZ = astRandom.nextInt(Math.max(1, r / 2)) - r / 4;

        int bound = r + 1;

        boolean allowOpenCaves = large &&
                (shape == ShapeType.SPHERE || shape == ShapeType.CUBE || shape == ShapeType.SATURN) &&
                astRandom.nextFloat() < 0.15f;

        for (int x = -bound; x <= bound; x++) {
            int wx = base.getX() + x;
            if (wx < chunkMinX || wx > chunkMaxX) continue;

            for (int z = -bound; z <= bound; z++) {
                int wz = base.getZ() + z;
                if (wz < chunkMinZ || wz > chunkMaxZ) continue;

                for (int y = -bound; y <= bound; y++) {
                    int wy = base.getY() + y;
                    if (wy < minY || wy >= maxY) continue;

                    if (!isInsideShape(shape, x, y, z, r)) continue;

                    // ЛОГИКА ПОЛОГО АСТЕРОИДА:
                    // Если он полый, мы вырезаем внутри него ту же самую фигуру,
                    // но радиусом 70% от оригинала (оставляем толщину стенок 30%)
                    if (hollow) {
                        int innerR = (int)(r * 0.70f);
                        if (innerR > 0 && isInsideShape(shape, x, y, z, innerR)) {
                            continue; // Пропускаем центральные блоки, оставляя пустоту
                        }
                    }

                    int distSq = x*x + y*y + z*z;

                    // ПЕЩЕРЫ: Генерируются ТОЛЬКО если астероид НЕ полый
                    if (large && !hollow) {
                        double nx = (x + caveOffsetX) * 0.12;
                        double ny = (y + caveOffsetY) * 0.12;
                        double nz = (z + caveOffsetZ) * 0.12;
                        double noiseVal = noise.getValue(nx, ny, nz);

                        if (noiseVal > 0.45 && distSq < r*r * 0.5) {
                            if (allowOpenCaves) continue;
                            if (distSq < r*r * 0.5) continue;
                        }

                        double tunnel = noise.getValue(nx * 2, ny * 2, nz * 2);
                        if (Math.abs(tunnel) < 0.008 && distSq < r*r * 0.35) {
                            if (allowOpenCaves) continue;
                            if (distSq < r*r * 0.35) continue;
                        }
                    }

                    // Генерация руды (используем детерминированную функцию)
                    float oreChance = getDeterministicFloat(wx, wy, wz, asteroidSeed);
                    BlockState stateToPlace = (oreChance < 0.035f) ? ore : rock;

                    chunk.setBlockState(new BlockPos(wx, wy, wz), stateToPlace, false);
                }
            }
        }

        // Кристаллы генерируются и в пещерах больших астероидов, и внутри полых!
        if (large || hollow) {
            placeCrystalsInChunk(chunk, base, r, bound, shape, chunkRandom,
                    chunkMinX, chunkMaxX, chunkMinZ, chunkMaxZ, minY, maxY, asteroidSeed);
        }
    }

    private static void placeCrystalsInChunk(ChunkAccess chunk, BlockPos base,
                                             int r, int bound, ShapeType shape,
                                             RandomSource random, // Тут можно оставить, так как мы хэшируем координаты
                                             int chunkMinX, int chunkMaxX,
                                             int chunkMinZ, int chunkMaxZ,
                                             int minY, int maxY, long asteroidSeed) {
        BlockState crystal = ModBlocks.SURREAL_GLOWCRYSTAL.get().defaultBlockState();
        net.minecraft.world.level.block.Block crystalBlock = crystal.getBlock();

        final int minDist = 4;
        final int minDistSq = minDist * minDist;

        java.util.function.BiFunction<BlockPos, Integer, Boolean> isNearbyCrystal = (pos, dist) -> {
            int d = dist;
            int dSq = d * d;
            for (int dx = -d; dx <= d; dx++) {
                for (int dy = -d; dy <= d; dy++) {
                    for (int dz = -d; dz <= d; dz++) {
                        int dd = dx*dx + dy*dy + dz*dz;
                        if (dd >= dSq) continue;
                        BlockPos check = pos.offset(dx, dy, dz);

                        if (chunk.getBlockState(check).getBlock() == crystalBlock) {
                            return true;
                        }
                    }
                }
            }
            return false;
        };

        for (int x = -bound; x <= bound; x++) {
            int wx = base.getX() + x;
            if (wx < chunkMinX || wx > chunkMaxX) continue;

            for (int z = -bound; z <= bound; z++) {
                int wz = base.getZ() + z;
                if (wz < chunkMinZ || wz > chunkMaxZ) continue;

                // Потолок
                for (int y = bound; y > -bound; y--) {
                    if (!isInsideShape(shape, x, y, z, r)) continue;
                    if (!isInsideShape(shape, x, y - 1, z, r)) continue;
                    int wy = base.getY() + y;
                    int wyBelow = wy - 1;
                    if (wy < minY || wy >= maxY || wyBelow < minY) continue;

                    BlockState solid = chunk.getBlockState(new BlockPos(wx, wy, wz));
                    BlockState below = chunk.getBlockState(new BlockPos(wx, wyBelow, wz));
                    if (!solid.isSolid()) continue;
                    if (!below.isAir()) continue;

                    float crystalChance = getDeterministicFloat(wx, wyBelow, wz, asteroidSeed + 1);
                    if (crystalChance < 0.03f) {
                        BlockPos placePos = new BlockPos(wx, wyBelow, wz);
                        if (!isNearbyCrystal.apply(placePos, minDist)) {
                            chunk.setBlockState(placePos, crystal, false);
                        }
                    }
                    break;
                }

                // Пол
                for (int y = -bound; y < bound; y++) {
                    if (!isInsideShape(shape, x, y, z, r)) continue;
                    if (!isInsideShape(shape, x, y + 1, z, r)) continue;
                    int wy = base.getY() + y;
                    int wyAbove = wy + 1;
                    if (wy < minY || wyAbove >= maxY) continue;

                    BlockState solid = chunk.getBlockState(new BlockPos(wx, wy, wz));
                    BlockState above = chunk.getBlockState(new BlockPos(wx, wyAbove, wz));
                    if (!solid.isSolid()) continue;
                    if (!above.isAir()) continue;

                    float crystalChance = getDeterministicFloat(wx, wyAbove, wz, asteroidSeed + 2);
                    if (crystalChance < 0.02f) {
                        BlockPos placePos = new BlockPos(wx, wyAbove, wz);
                        if (!isNearbyCrystal.apply(placePos, minDist)) {
                            chunk.setBlockState(placePos, crystal, false);
                        }
                    }
                    break;
                }
            }
        }
    }

    public static boolean isInsideShape(ShapeType shape, int x, int y, int z, int r) {
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
            case STELLATED -> {
                double baseR = r;
                double theta = Math.atan2(z, x);
                double phi   = Math.atan2(Math.sqrt(x*x+z*z), y);

                double spikes = 0.25 * r * Math.sin(5 * theta) * Math.sin(5 * phi);
                double effectiveR = baseR + spikes;

                yield (x*x + y*y + z*z) <= effectiveR * effectiveR;
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