package net.fantik.lostdreams.block.custom;

import com.mojang.serialization.MapCodec;
import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NullCropBlock extends BushBlock implements BonemealableBlock {

    public static final MapCodec<NullCropBlock> CODEC = simpleCodec(NullCropBlock::new);

    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            Block.box(0, 0, 0, 16, 4,  16),
            Block.box(0, 0, 0, 16, 8,  16),
            Block.box(0, 0, 0, 16, 12, 16),
            Block.box(0, 0, 0, 16, 16, 16)
    };

    // Радиус заражения по стадиям (мох-подобный: растёт от краёв)
    private static final int[] SPREAD_RADIUS = {2, 3, 5, 7};

    // Сколько блоков заражается за 1 тик
    private static final int[] INFECT_PER_TICK = {1, 1, 2, 3};

    // Шанс заражения (1 из N) — чем ниже, тем чаще
    private static final int[] SPREAD_CHANCE = {12, 8, 5, 2};

    // Шанс спавна моба (1 из N) — только на стадии 3
    private static final int MOB_SPAWN_CHANCE = 15;

    public NullCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level,
                               BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(ModBlocks.NULL_GROUND.get());
    }

    // ВАЖНО: всегда тикает — заражение продолжается даже на стадии 3
    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level,
                           BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);

        // Рост только до стадии 3
        if (age < 3 && random.nextInt(8) == 0) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            age = age + 1;
        }

        // Заражение всегда — даже на стадии 3
        spreadNullInfection(level, pos, random, age);

        // Спавн мобов только на стадии 3
        if (age == 3 && random.nextInt(MOB_SPAWN_CHANCE) == 0) {
            trySpawnMob(level, pos, random);
        }
    }

    // -----------------------------------------------------------------------
    // Равномерное заражение — как мох (волновое распространение)
    // -----------------------------------------------------------------------
    private void spreadNullInfection(ServerLevel level, BlockPos cropPos,
                                     RandomSource random, int age) {
        if (random.nextInt(SPREAD_CHANCE[age]) != 0) return;

        int radius = SPREAD_RADIUS[age];
        int infectCount = INFECT_PER_TICK[age];

        /*
         * Мох-алгоритм:
         * 1. Ищем уже заражённые null_ground блоки в радиусе
         * 2. От каждого заражённого ищем соседей которые можно заразить
         * 3. Заражаем соседей — так зона растёт равномерно от краёв
         */
        List<BlockPos> infectedBlocks = new ArrayList<>();
        List<BlockPos> frontier = new ArrayList<>(); // граница заражения

        // Сам crop тоже считается источником
        infectedBlocks.add(cropPos);

        // Собираем все уже заражённые блоки в радиусе
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos check = cropPos.offset(dx, dy, dz);
                    if (level.getBlockState(check).is(ModBlocks.NULL_GROUND.get())) {
                        infectedBlocks.add(check);
                    }
                }
            }
        }

        // Ищем границу — незаражённые блоки рядом с заражёнными
        for (BlockPos infected : infectedBlocks) {
            // Проверяем 4 стороны (горизонтально)
            for (Direction dir : new Direction[]{
                    Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                BlockPos neighbor = infected.relative(dir);

                // Проверяем на уровне, выше и ниже
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos candidate = neighbor.offset(0, dy, 0);

                    // Должен быть в радиусе от crop
                    if (!isInRadius(cropPos, candidate, radius)) continue;

                    if (isInfectable(level.getBlockState(candidate))
                            && !frontier.contains(candidate)) {
                        frontier.add(candidate);
                    }
                }
            }
        }

        if (frontier.isEmpty()) return;

        // Перемешиваем для случайности
        Collections.shuffle(frontier, new java.util.Random(random.nextLong()));

        // Заражаем infectCount блоков из границы
        int infected = 0;
        for (BlockPos target : frontier) {
            if (infected >= infectCount) break;
            infectBlock(level, target, random, age);
            infected++;
        }
    }

    private boolean isInRadius(BlockPos center, BlockPos target, int radius) {
        int dx = Math.abs(center.getX() - target.getX());
        int dz = Math.abs(center.getZ() - target.getZ());
        return dx <= radius && dz <= radius;
    }

    private boolean isInfectable(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL);
    }

    private void infectBlock(ServerLevel level, BlockPos pos,
                             RandomSource random, int age) {
        level.setBlock(pos, ModBlocks.NULL_GROUND.get().defaultBlockState(), 2);

        BlockPos above = pos.above();
        if (!level.isEmptyBlock(above)) return;

        int roll = random.nextInt(100);

        switch (age) {
            case 0 -> {
                // Только трава
                if (roll < 60) setNullGrass(level, above);
            }
            case 1 -> {
                // Трава или цветок
                if      (roll < 50) setNullGrass(level, above);
                else if (roll < 70) setNullBlossom(level, above);
            }
            case 2 -> {
                // Трава, цветок или дерево
                if      (roll < 40) setNullGrass(level, above);
                else if (roll < 60) setNullBlossom(level, above);
                else if (roll < 70) tryPlaceDeadTree(level, above, random);
            }
            case 3 -> {
                // Всё включая куст
                if      (roll < 30) setNullGrass(level, above);
                else if (roll < 50) setNullBlossom(level, above);
                else if (roll < 65) tryPlaceDeadTree(level, above, random);
                else if (roll < 80) setNullBerryBush(level, above);
            }
        }
    }

    private void setNullGrass(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, ModBlocks.NULL_GRASS.get().defaultBlockState(), 2);
    }

    private void setNullBlossom(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, ModBlocks.NULL_BLOSSOM.get().defaultBlockState(), 2);
    }

    private void setNullBerryBush(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, ModBlocks.NULL_BERRY_BUSH.get().defaultBlockState(), 2);
    }

    private void tryPlaceDeadTree(ServerLevel level, BlockPos pos, RandomSource random) {
        int trunkH = 3 + random.nextInt(3);
        for (int i = 0; i < trunkH; i++) {
            BlockPos tp = pos.above(i);
            if (level.isEmptyBlock(tp)) {
                level.setBlock(tp, ModBlocks.NULL_LOG.get().defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2);
            }
        }

        int branches = 1 + random.nextInt(2);
        for (int b = 0; b < branches; b++) {
            int branchY = trunkH / 2 + random.nextInt(trunkH / 2 + 1);
            BlockPos branchBase = pos.above(branchY);

            Direction.Axis axis = random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z;
            int offset = random.nextBoolean() ? 1 : -1;

            BlockPos bp = branchBase.offset(
                    axis == Direction.Axis.X ? offset : 0,
                    0,
                    axis == Direction.Axis.Z ? offset : 0);

            if (level.isEmptyBlock(bp)) {
                level.setBlock(bp, ModBlocks.NULL_LOG.get().defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, axis), 2);
            }
        }
    }

    private void trySpawnMob(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos spawnPos = pos.offset(
                random.nextInt(5) - 2,
                1,
                random.nextInt(5) - 2);

        if (!level.isEmptyBlock(spawnPos)) return;

        net.minecraft.world.entity.Entity mob;
        if (random.nextBoolean()) {
            mob = ModEntities.NULL_BUG.get().create(level);
        } else {
            mob = ModEntities.LUCID_WASTE.get().create(level);
        }

        if (mob == null) return;

        mob.moveTo(
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                random.nextFloat() * 360f, 0f);
        level.addFreshEntity(mob);
    }

    // BonemealableBlock
    @Override
    public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader level,
                                         BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random,
                                     BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random,
                                BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < 3) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }
    }
}
