package net.fantik.lostdreams.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.List;

public class AsteroidGroupUtil {

    // Добавляем типы формаций для группы
    public enum GroupType {
        SCATTERED,  // Хаотичная (как было)
        RING,       // Кольцо
        TRIANGLE,   // Треугольник (с центрами граней)
        CUBE_FRAME  // Каркас куба (8 вершин)
    }

    public static void generateGroup(ChunkAccess chunk, BlockPos base, int r,
                                     RandomSource random, boolean sameShape, boolean sameColor) {

        // --- 1. ВЫБОР ФОРМЫ ГРУППЫ ---
        GroupType groupType;
        float randChoice = random.nextFloat();

        // Настройка шансов: 55% хаос, 15% кольцо, 15% треугольник, 15% куб
        if (randChoice < 0.55f) {
            groupType = GroupType.SCATTERED;
        } else if (randChoice < 0.70f) {
            groupType = GroupType.RING;
        } else if (randChoice < 0.85f) {
            groupType = GroupType.TRIANGLE;
        } else {
            groupType = GroupType.CUBE_FRAME;
        }

        // Базовые параметры на случай, если sameShape или sameColor = true
        AsteroidUtil.ShapeType baseShape = AsteroidUtil.ShapeType.values()[random.nextInt(AsteroidUtil.ShapeType.values().length)];
        AsteroidUtil.AsteroidType baseType = AsteroidUtil.AsteroidType.values()[random.nextInt(AsteroidUtil.AsteroidType.values().length)];

        List<BlockPos> centers = new ArrayList<>();

        // --- 2. РАСЧЕТ КООРДИНАТ ЦЕНТРОВ ---

        if (groupType == GroupType.SCATTERED) {
            // СТАРАЯ ЛОГИКА: Хаотичное скопление
            int count = 2 + random.nextInt(3);
            centers.add(base);
            for (int i = 0; i < count; i++) {
                BlockPos pos = null;
                for (int attempt = 0; attempt < 10; attempt++) {
                    int dx = random.nextInt(r * 4) - r * 2;
                    int dy = random.nextInt(r) - r / 2;
                    int dz = random.nextInt(r * 4) - r * 2;
                    BlockPos candidate = base.offset(dx, dy, dz);

                    boolean tooClose = false;
                    for (BlockPos c : centers) {
                        if (candidate.distSqr(c) < (r * 2) * (r * 2)) {
                            tooClose = true;
                            break;
                        }
                    }
                    if (!tooClose) {
                        pos = candidate;
                        break;
                    }
                }
                if (pos != null) centers.add(pos);
            }

        } else if (groupType == GroupType.RING) {
            // КОЛЬЦО: 6-8 астероидов, соединенных в хоровод
            int count = 6 + random.nextInt(3);
            // Формула длины окружности, чтобы астероиды радиуса 'r' касались друг друга
            // Дистанция между центрами ~ 1.6 * r (чтобы слегка пересекались)
            double ringRadius = (count * (r * 1.6)) / (2 * Math.PI);

            // Легкий случайный наклон орбиты, чтобы кольца не всегда были строго горизонтальными
            double tiltX = (random.nextFloat() - 0.5) * 0.8;
            double tiltZ = (random.nextFloat() - 0.5) * 0.8;

            for (int i = 0; i < count; i++) {
                double angle = 2 * Math.PI * i / count;
                int dx = (int) (Math.cos(angle) * ringRadius);
                int dz = (int) (Math.sin(angle) * ringRadius);
                int dy = (int) (dx * tiltX + dz * tiltZ); // Применяем наклон по оси Y
                centers.add(base.offset(dx, dy, dz));
            }

        } else if (groupType == GroupType.TRIANGLE) {
            // ТРЕУГОЛЬНИК: 3 вершины + 3 середины ребер = 6 астероидов
            double triRadius = r * 1.85; // Радиус описанной окружности
            List<BlockPos> vertices = new ArrayList<>();

            double tiltX = (random.nextFloat() - 0.5) * 0.8;
            double tiltZ = (random.nextFloat() - 0.5) * 0.8;

            // Вершины
            for (int i = 0; i < 3; i++) {
                double angle = 2 * Math.PI * i / 3;
                int dx = (int) (Math.cos(angle) * triRadius);
                int dz = (int) (Math.sin(angle) * triRadius);
                int dy = (int) (dx * tiltX + dz * tiltZ);
                vertices.add(base.offset(dx, dy, dz));
            }
            centers.addAll(vertices);

            // Середины сторон (чтобы каркас треугольника был сплошным)
            for (int i = 0; i < 3; i++) {
                BlockPos p1 = vertices.get(i);
                BlockPos p2 = vertices.get((i + 1) % 3);
                BlockPos mid = new BlockPos(
                        (p1.getX() + p2.getX()) / 2,
                        (p1.getY() + p2.getY()) / 2,
                        (p1.getZ() + p2.getZ()) / 2
                );
                centers.add(mid);
            }

        } else if (groupType == GroupType.CUBE_FRAME) {
            // КУБ: 8 вершин
            // Смещение от центра до угла. Чтобы 8 шаров слиплись в куб,
            // расстояние между соседними углами должно быть ~ 1.7 * r
            // Значит смещение от центра по каждой оси = 0.85 * r
            int offset = Math.max(2, (int) (r * 0.85));

            int[] signs = {-1, 1};
            for (int x : signs) {
                for (int y : signs) {
                    for (int z : signs) {
                        centers.add(base.offset(x * offset, y * offset, z * offset));
                    }
                }
            }
        }

        // --- 3. ГЕНЕРАЦИЯ АСТЕРОИДОВ ПО ВЫЧИСЛЕННЫМ ЦЕНТРАМ ---

        for (BlockPos pos : centers) {
            AsteroidUtil.ShapeType s = sameShape ? baseShape :
                    AsteroidUtil.ShapeType.values()[random.nextInt(AsteroidUtil.ShapeType.values().length)];
            AsteroidUtil.AsteroidType t = sameColor ? baseType :
                    AsteroidUtil.AsteroidType.values()[random.nextInt(AsteroidUtil.AsteroidType.values().length)];

            // Чуть-чуть «пляшем» радиусом для живописности (кроме разбросанных, там старая логика)
            int individualR = r;
            if (groupType != GroupType.SCATTERED) {
                // В фигурах делаем легкую погрешность размера (+- 15%), чтобы каркас не был скучным и "пластиковым"
                individualR = (int) (r * 0.85f + random.nextInt(Math.max(1, (int)(r * 0.3f))));
            }

            int finalRadius = AsteroidUtil.getRadius(s, individualR);
            AsteroidUtil.generateAsteroidInChunk(chunk, pos, finalRadius, s, t, random);
        }
    }
}