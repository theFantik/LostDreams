package net.fantik.lostdreams.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class ClusterGenerator {

    public enum ClusterShape {
        SPHERE, DIAMOND, TORUS, FLOW
    }

    public enum ClusterType {
        NORMAL,
        RED_ONLY,
        BLUE_ONLY,
        CHAINS_ONLY,
        SPHERES_ONLY,
        ANOMALY
    }

    private static long hash(long seed, int x, int z) {
        long h = seed;
        h ^= x * 341873128712L;
        h ^= z * 132897987541L;
        return h;
    }

    public static Result getCluster(long seed, int x, int z) {

        // ⭐ FIX отрицательных координат
        int cellX = Mth.floor((double)x / 400.0);
        int cellZ = Mth.floor((double)z / 400.0);

        RandomSource rand = RandomSource.create(hash(seed, cellX, cellZ));

        ClusterShape shape = ClusterShape.values()[rand.nextInt(ClusterShape.values().length)];
        ClusterType type = ClusterType.values()[rand.nextInt(ClusterType.values().length)];

        double size = switch (rand.nextInt(3)) {
            case 0 -> 120;
            case 1 -> 180;
            default -> 260;
        };

        // центр кластера
        double cx = cellX * 400 + 200;
        double cz = cellZ * 400 + 200;

        double dx = x - cx;
        double dz = z - cz;

        // ⭐ сид-зависимый поворот
        double angle = rand.nextDouble() * Math.PI * 2;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double rx = dx * cos - dz * sin;
        double rz = dx * sin + dz * cos;

        double density = switch (shape) {

            case SPHERE -> {
                double dist = Math.sqrt(rx * rx + rz * rz);
                yield 1.0 - dist / size;
            }

            case DIAMOND -> {
                double dist = Math.abs(rx) + Math.abs(rz);
                yield 1.0 - dist / size;
            }

            case TORUS -> {
                double dist = Math.sqrt(rx * rx + rz * rz);
                double ring = Math.abs(dist - size * 0.65);
                yield 1.0 - ring / (size * 0.35);
            }

            case FLOW -> {
                double line = Math.abs(rz);
                yield 1.0 - line / (size * 0.35);
            }
        };

        // ⭐ плавный falloff
        density = Mth.clamp(density, -1.0, 1.0);

        // 💀 редкие огромные аномалии
        if (rand.nextFloat() < 0.03f) {
            type = ClusterType.ANOMALY;
            density += 2.0;
        }

        return new Result(density, type, size);
    }

    public record Result(double density, ClusterType type, double size) {}
}