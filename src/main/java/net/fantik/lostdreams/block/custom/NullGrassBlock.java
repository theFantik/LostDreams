package net.fantik.lostdreams.block.custom;

import com.mojang.serialization.MapCodec;
import net.fantik.lostdreams.block.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NullGrassBlock extends BushBlock {

    // ОБЯЗАТЕЛЬНО в 1.21+
    public static final MapCodec<NullGrassBlock> CODEC = simpleCodec(NullGrassBlock::new);

    // хитбокс как у низкой травы
    protected static final VoxelShape SHAPE =
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    @Override
    public MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    public NullGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // хитбокс
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level,
                                  BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // НА ЧЁМ МОЖНО РАСТИ
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.NULL_GROUND.get())

                || state.is(Blocks.END_STONE)
                || state.is(Blocks.NETHERRACK);
    }

    // если блок снизу исчез — ломаемся
    @Override
    protected BlockState updateShape(BlockState state,
                                     Direction direction,
                                     BlockState neighborState,
                                     LevelAccessor level,
                                     BlockPos pos,
                                     BlockPos neighborPos) {

        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}