package net.fantik.lostdreams.block.custom;

import com.mojang.serialization.MapCodec;
import net.fantik.lostdreams.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NullBerryBushBlock extends BushBlock implements BonemealableBlock {

    public static final MapCodec<NullBerryBushBlock> CODEC = simpleCodec(NullBerryBushBlock::new);

    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    private static final VoxelShape SHAPE_SMALL = Block.box(3, 0, 3, 13, 8, 13);
    private static final VoxelShape SHAPE_BIG   = Block.box(1, 0, 1, 15, 12, 15);

    public NullBerryBushBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level,
                               BlockPos pos, CollisionContext context) {
        return state.getValue(AGE) == 0 ? SHAPE_SMALL : SHAPE_BIG;
    }

    // -----------------------------------------------------------------------
    // Рост
    // -----------------------------------------------------------------------

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < 3;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level,
                           BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < 3 && random.nextInt(5) == 0) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }
    }

    // -----------------------------------------------------------------------
    // Костная мука (BonemealableBlock)
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // Сбор ягод правым кликом (стадии 2 и 3)
    // -----------------------------------------------------------------------

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level,
                                            BlockPos pos, Player player,
                                            BlockHitResult hit) {
        int age = state.getValue(AGE);

        // Собирать можно со стадии 2
        if (age < 2) return InteractionResult.PASS;

        int count = 1 + level.random.nextInt(2);
        popResource(level, pos, new ItemStack(ModItems.NULL_BERRY.get(), count));

        level.playSound(null, pos,
                SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                SoundSource.BLOCKS, 1.0f, 0.8f + level.random.nextFloat() * 0.4f);

        // После сбора возвращаем на стадию 1
        level.setBlock(pos, state.setValue(AGE, 1), 2);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // -----------------------------------------------------------------------
    // Урон при проходе
    // -----------------------------------------------------------------------

    @Override
    public void entityInside(BlockState state, Level level,
                             BlockPos pos, Entity entity) {
        if (entity instanceof Player player) {
            if (!player.isCreative() && !player.isSpectator()
                    && state.getValue(AGE) > 0) {
                entity.makeStuckInBlock(state, new net.minecraft.world.phys.Vec3(0.8, 0.75, 0.8));
            }
        }
    }
}