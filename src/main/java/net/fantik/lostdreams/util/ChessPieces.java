package net.fantik.lostdreams.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Процедурное построение шахматных фигур из блоков.
 * Каждая фигура строится как "тело вращения" — на каждом слое высоты задан
 * радиус круга, который заливается блоками. Конь — исключение, он собирается
 * из явно заданных смещений, потому что не симметричен вокруг вертикальной оси.
 *
 * Все build-методы получают ChunkAccess конкретного чанка и сами отбрасывают
 * блоки, которые в этот чанк не попадают (chunk может вызываться несколько раз
 * для одной фигуры, если она пересекает границу чанков).
 */
public final class ChessPieces {

    public static final BlockState WHITE_BLOCK = Blocks.QUARTZ_BLOCK.defaultBlockState();
    public static final BlockState WHITE_TRIM  = Blocks.SMOOTH_QUARTZ.defaultBlockState();
    public static final BlockState BLACK_BLOCK = Blocks.POLISHED_BLACKSTONE.defaultBlockState();
    public static final BlockState BLACK_TRIM  = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();

    private ChessPieces() {}

    public enum PieceType { PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING }

    /**
     * Строит фигуру с центром в (centerX, centerZ), первый слой — на baseY (сразу над доской).
     */
    public static void build(ChunkAccess chunk, ChunkPos chunkPos, int centerX, int centerZ, int baseY,
                             PieceType type, boolean white) {
        BlockState body = white ? WHITE_BLOCK : BLACK_BLOCK;
        BlockState trim = white ? WHITE_TRIM : BLACK_TRIM;

        switch (type) {
            case PAWN   -> buildRadial(chunk, chunkPos, centerX, centerZ, baseY, body, trim, pawnProfile());
            case BISHOP -> buildRadial(chunk, chunkPos, centerX, centerZ, baseY, body, trim, bishopProfile());
            case ROOK   -> buildRook(chunk, chunkPos, centerX, centerZ, baseY, body, trim);
            case QUEEN  -> buildQueen(chunk, chunkPos, centerX, centerZ, baseY, body, trim);
            case KING   -> buildKing(chunk, chunkPos, centerX, centerZ, baseY, body, trim);
            case KNIGHT -> buildKnight(chunk, chunkPos, centerX, centerZ, baseY, body, trim);
        }
    }

    // -----------------------------------------------------------------------
    // Радиальные профили (радиус круга на каждом слое высоты, снизу вверх)
    // -----------------------------------------------------------------------

    private static double[] pawnProfile() {
        return new double[]{2.6, 2.6, 2.4, 2.0, 1.6, 1.3, 1.1, 1.6, 1.9, 1.3};
    }

    private static double[] bishopProfile() {
        return new double[]{3.2, 3.1, 2.9, 2.6, 2.3, 2.0, 1.7, 1.4, 1.2, 1.0, 0.9, 0.8, 1.3, 0.6};
    }

    private static double[] queenBodyProfile() {
        return new double[]{3.6, 3.5, 3.3, 3.0, 2.7, 2.4, 2.1, 1.9, 1.7, 1.6, 1.5, 1.5, 1.6, 1.8, 2.1};
    }

    private static double[] kingBodyProfile() {
        return new double[]{3.8, 3.7, 3.5, 3.2, 2.9, 2.6, 2.3, 2.0, 1.8, 1.7, 1.6, 1.6, 1.7, 1.9, 2.1, 1.6};
    }

    // -----------------------------------------------------------------------
    // Построение по радиальному профилю
    // -----------------------------------------------------------------------

    private static void buildRadial(ChunkAccess chunk, ChunkPos chunkPos, int cx, int cz, int baseY,
                                    BlockState body, BlockState trim, double[] profile) {
        for (int layer = 0; layer < profile.length; layer++) {
            fillCircle(chunk, chunkPos, cx, cz, baseY + layer, profile[layer], body);
        }
        placeIfInside(chunk, chunkPos, cx, baseY + profile.length, cz, trim);
    }

    private static void fillCircle(ChunkAccess chunk, ChunkPos chunkPos, int cx, int cz, int y,
                                   double radius, BlockState state) {
        int r = (int) Math.ceil(radius);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (Math.sqrt(dx * dx + dz * dz) <= radius) {
                    placeIfInside(chunk, chunkPos, cx + dx, y, cz + dz, state);
                }
            }
        }
    }

    private static void placeIfInside(ChunkAccess chunk, ChunkPos chunkPos, int worldX, int y, int worldZ,
                                      BlockState state) {
        if (worldX < chunkPos.getMinBlockX() || worldX > chunkPos.getMaxBlockX()) return;
        if (worldZ < chunkPos.getMinBlockZ() || worldZ > chunkPos.getMaxBlockZ()) return;
        chunk.setBlockState(new BlockPos(worldX, y, worldZ), state, false);
    }

    // -----------------------------------------------------------------------
    // Ладья: цилиндр с зубцами наверху (как крепостная башня)
    // -----------------------------------------------------------------------

    private static void buildRook(ChunkAccess chunk, ChunkPos chunkPos, int cx, int cz, int baseY,
                                  BlockState body, BlockState trim) {
        int height = 11;
        double radius = 3.4;
        for (int layer = 0; layer < height; layer++) {
            fillCircle(chunk, chunkPos, cx, cz, baseY + layer, radius, body);
        }
        int topY = baseY + height;
        for (int i = 0; i < 8; i++) {
            double angle = i * (Math.PI / 4.0);
            int dx = (int) Math.round(Math.cos(angle) * radius);
            int dz = (int) Math.round(Math.sin(angle) * radius);
            if (i % 2 == 0) {
                placeIfInside(chunk, chunkPos, cx + dx, topY, cz + dz, trim);
                placeIfInside(chunk, chunkPos, cx + dx, topY + 1, cz + dz, trim);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Ферзь: тело + корона из зубцов
    // -----------------------------------------------------------------------

    private static void buildQueen(ChunkAccess chunk, ChunkPos chunkPos, int cx, int cz, int baseY,
                                   BlockState body, BlockState trim) {
        double[] profile = queenBodyProfile();
        buildRadial(chunk, chunkPos, cx, cz, baseY, body, trim, profile);
        int crownY = baseY + profile.length;
        for (int i = 0; i < 8; i++) {
            double angle = i * (Math.PI / 4.0);
            int dx = (int) Math.round(Math.cos(angle) * 1.6);
            int dz = (int) Math.round(Math.sin(angle) * 1.6);
            placeIfInside(chunk, chunkPos, cx + dx, crownY, cz + dz, trim);
        }
        placeIfInside(chunk, chunkPos, cx, crownY + 1, cz, trim);
    }

    // -----------------------------------------------------------------------
    // Король: тело + крест наверху (самая высокая фигура)
    // -----------------------------------------------------------------------

    private static void buildKing(ChunkAccess chunk, ChunkPos chunkPos, int cx, int cz, int baseY,
                                  BlockState body, BlockState trim) {
        double[] profile = kingBodyProfile();
        buildRadial(chunk, chunkPos, cx, cz, baseY, body, trim, profile);
        int crossY = baseY + profile.length + 1;
        placeIfInside(chunk, chunkPos, cx, crossY, cz, trim);
        placeIfInside(chunk, chunkPos, cx, crossY + 1, cz, trim);
        placeIfInside(chunk, chunkPos, cx, crossY + 2, cz, trim);
        placeIfInside(chunk, chunkPos, cx - 1, crossY + 1, cz, trim);
        placeIfInside(chunk, chunkPos, cx + 1, crossY + 1, cz, trim);
        placeIfInside(chunk, chunkPos, cx, crossY + 1, cz - 1, trim);
        placeIfInside(chunk, chunkPos, cx, crossY + 1, cz + 1, trim);
    }

    // -----------------------------------------------------------------------
    // Конь: асимметричная "голова лошади", собрана вручную из смещений
    // -----------------------------------------------------------------------

    private static void buildKnight(ChunkAccess chunk, ChunkPos chunkPos, int cx, int cz, int baseY,
                                    BlockState body, BlockState trim) {
        // Округлая база
        for (int layer = 0; layer < 4; layer++) {
            fillCircle(chunk, chunkPos, cx, cz, baseY + layer, 2.6 - layer * 0.15, body);
        }
        // Изогнутая "шея", наклонённая в сторону +X
        int[][] neck = {
                {0, 4, 0}, {0, 5, 0}, {1, 6, 0}, {1, 7, 0}, {2, 8, 0}, {2, 9, 0}
        };
        for (int[] p : neck) {
            fillCircle(chunk, chunkPos, cx + p[0], cz + p[2], baseY + p[1], 1.4, body);
        }
        // "Голова" и "уши" на конце шеи
        int headX = cx + 3;
        int headY = baseY + 10;
        int headZ = cz;
        fillCircle(chunk, chunkPos, headX, headZ, headY, 1.6, body);
        fillCircle(chunk, chunkPos, headX + 1, headZ, headY, 1.2, body);
        placeIfInside(chunk, chunkPos, headX, headY + 1, headZ - 1, trim);
        placeIfInside(chunk, chunkPos, headX, headY + 1, headZ + 1, trim);
        placeIfInside(chunk, chunkPos, headX + 2, headY, headZ, trim);
    }
}