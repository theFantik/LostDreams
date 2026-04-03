package net.fantik.lostdreams.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkAccess;
// ИМПОРТ BlockState БОЛЬШЕ НЕ НУЖЕН, НО МОЖНО ОСТАВИТЬ, ЧТОБЫ НЕ МЕШАЛ

import java.util.ArrayList;
import java.util.List;

public class AsteroidGroupUtil {

    public static void generateGroup(ChunkAccess chunk, BlockPos base, int r,
                                     RandomSource random, boolean sameShape, boolean sameColor) {
        int count = 2 + random.nextInt(3); // 2–4 астероида в группе

        AsteroidUtil.ShapeType shape = AsteroidUtil.ShapeType.values()[random.nextInt(AsteroidUtil.ShapeType.values().length)];
        AsteroidUtil.AsteroidType type = AsteroidUtil.AsteroidType.values()[random.nextInt(AsteroidUtil.AsteroidType.values().length)];

        List<BlockPos> centers = new ArrayList<>();
        centers.add(base);

        for (int i = 0; i < count; i++) {
            BlockPos pos = null;

            // несколько попыток найти подходящий оффсет
            for (int attempt = 0; attempt < 10; attempt++) {
                int dx = random.nextInt(r * 4) - r * 2;
                int dy = random.nextInt(r) - r / 2;
                int dz = random.nextInt(r * 4) - r * 2;

                BlockPos candidate = base.offset(dx, dy, dz);

                boolean tooClose = false;
                for (BlockPos c : centers) {
                    double distSq = candidate.distSqr(c);
                    if (distSq < (r * 2) * (r * 2)) { // минимум два радиуса
                        tooClose = true;
                        break;
                    }
                }

                if (!tooClose) {
                    pos = candidate;
                    break;
                }
            }

            if (pos == null) continue; // не нашли подходящее место

            centers.add(pos);

            AsteroidUtil.ShapeType s = sameShape ? shape :
                    AsteroidUtil.ShapeType.values()[random.nextInt(AsteroidUtil.ShapeType.values().length)];
            AsteroidUtil.AsteroidType t = sameColor ? type :
                    AsteroidUtil.AsteroidType.values()[random.nextInt(AsteroidUtil.AsteroidType.values().length)];

            int rr = AsteroidUtil.getRadius(s, r);

            // ИЗМЕНЕНИЕ: Передаем тип 't' вместо 'rock'
            AsteroidUtil.generateAsteroidInChunk(chunk, pos, rr, s, t, random);
        }
    }
}