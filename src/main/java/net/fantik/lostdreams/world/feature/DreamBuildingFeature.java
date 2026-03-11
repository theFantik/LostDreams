package net.fantik.lostdreams.world.feature;

import com.mojang.serialization.Codec;
import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.Heightmap;

public class DreamBuildingFeature extends Feature<NoneFeatureConfiguration> {

    private static final int MAX_OFFSET = 12;

    public DreamBuildingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int width  = 4 + random.nextInt(MAX_OFFSET - 3);
        int height = 5 + random.nextInt(18);
        int depth  = 4 + random.nextInt(MAX_OFFSET - 3);

        // Берём реальную поверхность через Heightmap
        int surfaceY = level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                origin.getX(), origin.getZ()
        );

        boolean floating = random.nextBoolean();
        int baseY;
        if (floating) {
            // Поднимаем над реальной поверхностью на 8-24 блока
            baseY = surfaceY + 8 + random.nextInt(16);
        } else {
            baseY = surfaceY;
        }

        BlockPos base = new BlockPos(
                origin.getX() - width / 2,
                baseY,
                origin.getZ() - depth / 2
        );

        buildBox(level, random, base, width, height, depth, origin);

        int annexCount = 1 + random.nextInt(2);
        for (int i = 0; i < annexCount; i++) {
            buildAnnex(level, random, base, width, height, depth, origin);
        }

        fillInterior(level, random, base, width, height, depth, origin);
        crumble(level, random, base, width, height, depth);

        addBlobInclusions(level, random, base, width, height, depth);

        if (floating) {
            buildPillars(level, random, base, width, depth, origin, surfaceY);
        }

        return true;
    }

    private boolean inBounds(BlockPos pos, BlockPos origin) {
        int dx = Math.abs(pos.getX() - origin.getX());
        int dz = Math.abs(pos.getZ() - origin.getZ());
        return dx <= MAX_OFFSET && dz <= MAX_OFFSET;
    }

    /**
     * Столбы теперь принимают surfaceY напрямую — больше не зависят от Heightmap в цикле.
     */
    private void buildPillars(WorldGenLevel level, RandomSource random,
                              BlockPos base, int w, int d, BlockPos origin, int surfaceY) {
        // Фиксированные точки столбов — углы, центры сторон и центр пола
        int[][] points = {
                { base.getX() + 1,     base.getZ() + 1     }, // угол SW
                { base.getX() + w - 2, base.getZ() + 1     }, // угол SE
                { base.getX() + 1,     base.getZ() + d - 2 }, // угол NW
                { base.getX() + w - 2, base.getZ() + d - 2 }, // угол NE
                { base.getX() + w / 2, base.getZ() + d / 2 }, // центр
                { base.getX() + w / 2, base.getZ() + 1     }, // центр юга
                { base.getX() + w / 2, base.getZ() + d - 2 }, // центр севера
                { base.getX() + 1,     base.getZ() + d / 2 }, // центр запада
                { base.getX() + w - 2, base.getZ() + d / 2 }, // центр востока
        };

        // Берём 4-7 случайных точек из списка
        int count = 4 + random.nextInt(4);
        for (int i = 0; i < count && i < points.length; i++) {
            int px = points[i][0];
            int pz = points[i][1];
            int floorY = base.getY(); // Y пола здания

            // Ищем первый твёрдый блок под зданием (воздух и вода — пропускаем)
            int groundY = floorY - 1;
            while (groundY > level.getMinBuildHeight()) {
                BlockPos check = new BlockPos(px, groundY, pz);
                BlockState st = level.getBlockState(check);
                boolean isAir = st.isAir();
                boolean isWater = !st.getFluidState().isEmpty();
                if (!isAir && !isWater) break; // нашли твёрдый блок
                groundY--;
            }

            // Если земля уже вплотную — столб не нужен
            if (groundY >= floorY - 1) continue;

            // Строим столб от земли до пола, заменяя воду тоже
            for (int y = groundY + 1; y < floorY; y++) {
                BlockPos pos = new BlockPos(px, y, pz);
                setBlock(level, pos, pickPillarBlock(random));
            }
        }
    }

    private void buildBox(WorldGenLevel level, RandomSource random,
                          BlockPos base, int w, int h, int d, BlockPos origin) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    boolean isWall = x == 0 || x == w - 1
                            || y == 0 || y == h - 1
                            || z == 0 || z == d - 1;
                    if (isWall) {
                        BlockPos pos = base.offset(x, y, z);
                        if (inBounds(pos, origin)) {
                            setBlock(level, pos, pickWallBlock(random));
                        }
                    }
                }
            }
        }
    }

    private void buildAnnex(WorldGenLevel level, RandomSource random,
                            BlockPos base, int w, int h, int d, BlockPos origin) {
        int aw = 3 + random.nextInt(5);
        int ah = 3 + random.nextInt(h);
        int ad = 3 + random.nextInt(5);

        int side = random.nextInt(4);
        BlockPos annexBase = switch (side) {
            case 0  -> base.offset(-aw, 0, random.nextInt(d));
            case 1  -> base.offset(w,   0, random.nextInt(d));
            case 2  -> base.offset(random.nextInt(w), 0, -ad);
            default -> base.offset(random.nextInt(w), 0, d);
        };

        annexBase = annexBase.offset(0, -random.nextInt(3), 0);
        buildBox(level, random, annexBase, aw, ah, ad, origin);
    }

    private void fillInterior(WorldGenLevel level, RandomSource random,
                              BlockPos base, int w, int h, int d, BlockPos origin) {
        int stairCount = 1 + random.nextInt(4);
        for (int i = 0; i < stairCount; i++) {
            int sx = 1 + random.nextInt(w - 2);
            int sz = 1 + random.nextInt(d - 2);
            int stairHeight = 2 + random.nextInt(h - 2);

            for (int y = 1; y < stairHeight; y++) {
                if (y % 2 == 0) {
                    BlockPos pos = base.offset(sx, y, sz);
                    if (!inBounds(pos, origin)) continue;
                    Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    setBlock(level, pos,
                            Blocks.STONE_BRICK_STAIRS.defaultBlockState()
                                    .setValue(StairBlock.FACING, dir)
                                    .setValue(StairBlock.HALF, random.nextBoolean() ? Half.TOP : Half.BOTTOM)
                                    .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT));
                }
            }
        }

        int pillarCount = random.nextInt(3);
        for (int i = 0; i < pillarCount; i++) {
            int px = 1 + random.nextInt(w - 2);
            int pz = 1 + random.nextInt(d - 2);
            int pillarH = 2 + random.nextInt(h - 2);
            for (int y = 1; y <= pillarH; y++) {
                BlockPos pos = base.offset(px, y, pz);
                if (inBounds(pos, origin)) {
                    setBlock(level, pos, pickWallBlock(random));
                }
            }
        }

        if (random.nextBoolean()) {
            Direction ladderDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int lx = ladderDir == Direction.EAST  ? w - 2 :
                    ladderDir == Direction.WEST  ? 1 :
                            1 + random.nextInt(w - 2);
            int lz = ladderDir == Direction.SOUTH ? d - 2 :
                    ladderDir == Direction.NORTH ? 1 :
                            1 + random.nextInt(d - 2);

            for (int y = 1; y < h - 1; y++) {
                BlockPos lpos = base.offset(lx, y, lz);
                if (inBounds(lpos, origin) && level.getBlockState(lpos).isAir()) {
                    setBlock(level, lpos, Blocks.LADDER.defaultBlockState()
                            .setValue(LadderBlock.FACING, ladderDir));
                }
            }
        }
    }

    private void crumble(WorldGenLevel level, RandomSource random,
                         BlockPos base, int w, int h, int d) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < d; z++) {
                    boolean isWall = x == 0 || x == w - 1
                            || y == 0 || y == h - 1
                            || z == 0 || z == d - 1;
                    if (!isWall) continue;

                    float crumbleChance = 0.1f + (float) y / h * 0.3f;
                    if (random.nextFloat() < crumbleChance) {
                        level.setBlock(base.offset(x, y, z),
                                Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    /**
     * Размещает случайные кучки (blob) из разных материалов внутри и на стенах здания.
     * Каждый blob — шарообразное скопление 4-12 блоков одного материала.
     */
    private void addBlobInclusions(WorldGenLevel level, RandomSource random,
                                   BlockPos base, int w, int h, int d) {
        // Сколько кучек генерируем
        int blobCount = 3 + random.nextInt(6);

        for (int b = 0; b < blobCount; b++) {
            // Центр кучки — случайная точка внутри объёма здания
            int cx = base.getX() + random.nextInt(w);
            int cy = base.getY() + random.nextInt(h);
            int cz = base.getZ() + random.nextInt(d);

            BlockState blobBlock = pickInclusionBlock(random);
            int radius = 1 + random.nextInt(2); // радиус 1-2 блока

            // Заполняем сферу радиуса radius
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        // Проверяем что точка внутри сферы
                        if (dx*dx + dy*dy + dz*dz > radius*radius + 1) continue;

                        BlockPos pos = new BlockPos(cx + dx, cy + dy, cz + dz);

                        // Размещаем только там где уже есть блок (не в воздухе)
                        if (level.getBlockState(pos).isAir()) continue;

                        // Небольшой шанс пропустить блок — делает кучку рваной
                        if (random.nextFloat() < 0.25f) continue;

                        setBlock(level, pos, blobBlock);
                    }
                }
            }
        }
    }

    /**
     * Выбирает материал для кучки-вкрапления.
     */
    private BlockState pickInclusionBlock(RandomSource random) {
        return switch (random.nextInt(10)) {
            case 0     -> Blocks.AMETHYST_BLOCK.defaultBlockState();
            case 1     -> Blocks.GLOWSTONE.defaultBlockState();
            case 2     -> Blocks.MAGMA_BLOCK.defaultBlockState();
            case 3     -> Blocks.PURPUR_BLOCK.defaultBlockState();
            case 4, 5  -> Blocks.STONE.defaultBlockState();
            case 6     -> Blocks.DIRT.defaultBlockState();
            case 7     -> Blocks.NETHERRACK.defaultBlockState();
            default    -> Blocks.END_STONE.defaultBlockState();
        };
    }

    private BlockState pickWallBlock(RandomSource random) {
        return switch (random.nextInt(10)) {
            case 0, 1, 2, 3, 4 -> ModBlocks.NULL_BRICKS.get().defaultBlockState();
            case 5, 6, 7       -> ModBlocks.NULL_CRACKED_BRICKS.get().defaultBlockState();
            case 8             -> Blocks.OBSIDIAN.defaultBlockState();
            default            -> Blocks.DEEPSLATE.defaultBlockState();
        };
    }

    private BlockState pickPillarBlock(RandomSource random) {
        return switch (random.nextInt(6)) {
            case 0, 1, 2 -> ModBlocks.NULL_BRICKS.get().defaultBlockState();
            case 3, 4    -> Blocks.DEEPSLATE.defaultBlockState();
            default      -> Blocks.OBSIDIAN.defaultBlockState();
        };
    }

    private void setBlock(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }
}