package net.fantik.lostdreams.block.custom;

import com.mojang.serialization.MapCodec;

import net.fantik.lostdreams.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;



public class DreamGeneratorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HAS_RESOURCE = BooleanProperty.create("has_resource");

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);

    public DreamGeneratorBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HAS_RESOURCE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(DreamGeneratorBlock::new);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HAS_RESOURCE);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // BlockEntity методы
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DreamGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.DREAM_GENERATOR.get(),
                (lvl, pos, blockState, be) -> DreamGeneratorBlockEntity.tick(lvl, pos, blockState, be));
    }

    // Открытие GUI
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DreamGeneratorBlockEntity generator) {
                player.openMenu(new SimpleMenuProvider(
                        (containerId, inventory, p) -> new DreamGeneratorMenu(containerId, inventory, generator),
                        generator.getDisplayName()
                ), pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    // Дроп инвентаря при разрушении
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof DreamGeneratorBlockEntity generator) {
                if (level instanceof ServerLevel) {
                    Containers.dropContents(level, pos, generator);
                }
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }


    // Совместимость с воронкой
    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DreamGeneratorBlockEntity generator) {
            // Вычисляем сигнал вручную
            int totalItems = 0;
            int maxItems = 0;
            for (int i = 0; i < generator.getContainerSize(); i++) {
                ItemStack stack = generator.getItem(i);
                if (!stack.isEmpty()) {
                    totalItems += stack.getCount();
                    maxItems += stack.getMaxStackSize();
                } else {
                    maxItems += 64;
                }
            }
            if (totalItems == 0) return 0;
            return 1 + (totalItems * 14 / maxItems); // 1-15 уровень сигнала
        }
        return 0;
    }

    // Метод для генерации ресурсов (вызывается из события)
    public static void generateResources(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DreamGeneratorBlockEntity generator) {
            generator.generateRandomResource();
        }
    }

    // Проверка наличия кровати над блоком
    public static boolean hasBedAbove(Level level, BlockPos pos) {
        BlockState aboveState = level.getBlockState(pos.above());
        return aboveState.getBlock() instanceof BedBlock;
    }
}