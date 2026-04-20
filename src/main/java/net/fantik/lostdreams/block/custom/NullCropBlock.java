package net.fantik.lostdreams.block.custom;

import com.mojang.serialization.MapCodec;
import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
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

    // Радиус заражения по стадиям
    private static final int[] SPREAD_RADIUS = {2, 4, 5, 7};

    // Шанс заражения (1 из N) по стадиям
    private static final int[] SPREAD_CHANCE = {15, 9, 6, 2};

    // Шанс спавна моба (1 из N) — только на стадии 3
    private static final int MOB_SPAWN_CHANCE = 15;

    public NullCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    /**
     * @return
     */
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

    // Может расти только на дёрне или null_ground
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(ModBlocks.NULL_GROUND.get());
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level,
                           BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);

        // Рост
        if (age < 3 && random.nextInt(8) == 0) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            age = age + 1;
        }

        // Заражение окружения
        spreadNullInfection(state, level, pos, random, age);

        // Спавн мобов на максимальной стадии
        if (age == 3 && random.nextInt(MOB_SPAWN_CHANCE) == 0) {
            trySpawnMob(level, pos, random);
        }
    }

    private void spreadNullInfection(BlockState state, ServerLevel level,
                                     BlockPos pos, RandomSource random, int age) {
        if (random.nextInt(SPREAD_CHANCE[age]) != 0) return;

        int radius = SPREAD_RADIUS[age];
        int infectCount = 1 + random.nextInt(age + 1);

        // Собираем все подходящие позиции вокруг
        List<BlockPos> candidates = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos candidate = pos.offset(dx, 0, dz);
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos check = candidate.offset(0, dy, 0);
                    if (isInfectable(level.getBlockState(check))) {
                        candidates.add(check);
                    }
                }
            }
        }

        if (candidates.isEmpty()) return;

        for (int i = 0; i < infectCount && !candidates.isEmpty(); i++) {
            int idx = random.nextInt(candidates.size());
            infectBlock(level, candidates.get(idx), random, age);
            candidates.remove(idx);
        }
    }

    private boolean isInfectable(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL);
    }

    private void infectBlock(ServerLevel level, BlockPos pos,
                             RandomSource random, int age) {
        // Заменяем блок на null_ground
        level.setBlock(pos, ModBlocks.NULL_GROUND.get().defaultBlockState(), 2);

        BlockPos above = pos.above();
        // Если над блоком пусто — ставим декорацию
        if (!level.isEmptyBlock(above)) return;

        int roll = random.nextInt(100);

        if (age == 0) {
            // Стадия 0: только трава
            if (roll < 60) {
                level.setBlock(above,
                        ModBlocks.NULL_GRASS.get().defaultBlockState(), 2);
            }

        } else if (age == 1) {
            // Стадия 1: трава или цветок
            if (roll < 50) {
                level.setBlock(above,
                        ModBlocks.NULL_GRASS.get().defaultBlockState(), 2);
            } else if (roll < 70) {
                level.setBlock(above,
                        ModBlocks.NULL_BLOSSOM.get().defaultBlockState(), 2);
            }

        } else if (age == 2) {
            // Стадия 2: трава, цветок или дерево
            if (roll < 40) {
                level.setBlock(above,
                        ModBlocks.NULL_GRASS.get().defaultBlockState(), 2);
            } else if (roll < 60) {
                level.setBlock(above,
                        ModBlocks.NULL_BLOSSOM.get().defaultBlockState(), 2);
            } else if (roll < 70) {
                tryPlaceDeadTree(level, above, random);
            }

        } else {
            // Стадия 3: всё + куст + дерево чаще
            if (roll < 30) {
                level.setBlock(above,
                        ModBlocks.NULL_GRASS.get().defaultBlockState(), 2);
            } else if (roll < 50) {
                level.setBlock(above,
                        ModBlocks.NULL_BLOSSOM.get().defaultBlockState(), 2);
            } else if (roll < 65) {
                tryPlaceDeadTree(level, above, random);
            } else if (roll < 80) {
                level.setBlock(above,
                        ModBlocks.NULL_BERRY_BUSH.get().defaultBlockState(), 2);
            }
        }
    }

    private void tryPlaceDeadTree(ServerLevel level, BlockPos pos, RandomSource random) {
        // Простое мёртвое дерево прямо здесь
        int trunkH = 3 + random.nextInt(3);
        for (int i = 0; i < trunkH; i++) {
            BlockPos tp = pos.above(i);
            if (level.isEmptyBlock(tp)) {
                level.setBlock(tp, ModBlocks.NULL_LOG.get().defaultBlockState()
                        .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                                net.minecraft.core.Direction.Axis.Y), 2);
            }
        }
        // 1-2 ветки
        int branches = 1 + random.nextInt(2);
        for (int b = 0; b < branches; b++) {
            int branchY = trunkH / 2 + random.nextInt(trunkH / 2 + 1);
            BlockPos branchBase = pos.above(branchY);
            int dx = (random.nextBoolean() ? 1 : -1);
            int dz = (random.nextBoolean() ? 1 : -1);
            net.minecraft.core.Direction.Axis axis =
                    random.nextBoolean()
                            ? net.minecraft.core.Direction.Axis.X
                            : net.minecraft.core.Direction.Axis.Z;
            BlockPos bp = branchBase.offset(
                    axis == net.minecraft.core.Direction.Axis.X ? dx : 0,
                    0,
                    axis == net.minecraft.core.Direction.Axis.Z ? dz : 0);
            if (level.isEmptyBlock(bp)) {
                level.setBlock(bp, ModBlocks.NULL_LOG.get().defaultBlockState()
                        .setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, axis), 2);
            }
        }
    }

    private void trySpawnMob(ServerLevel level, BlockPos pos, RandomSource random) {
        // Позиция для спавна — рядом с кропом
        BlockPos spawnPos = pos.offset(
                random.nextInt(5) - 2,
                1,
                random.nextInt(5) - 2);

        if (!level.isEmptyBlock(spawnPos)) return;

        // 50/50 между null_bug и lucid_waste
        net.minecraft.world.entity.Entity mob;
        if (random.nextBoolean()) {
            mob = ModEntities.NULL_BUG.get().create(level);
        } else {
            mob = ModEntities.LUCID_WASTE.get().create(level);
        }

        if (mob == null) return;

        mob.moveTo(spawnPos.getX() + 0.5,
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
