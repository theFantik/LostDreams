package net.fantik.lostdreams.block;

import net.fantik.lostdreams.particle.ModParticles;
import net.fantik.lostdreams.util.PortalDefinition;
import net.fantik.lostdreams.util.PortalRegistry;
import net.fantik.lostdreams.util.PortalTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class PortalBlock extends Block {

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;

    private static final VoxelShape SHAPE_X = Block.box(0, 0, 6, 16, 16, 10);
    private static final VoxelShape SHAPE_Z = Block.box(6, 0, 0, 10, 16, 16);

    public PortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                               BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? SHAPE_X : SHAPE_Z;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                                        BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(entity instanceof ServerPlayer player)) return;

        PortalDefinition def = PortalRegistry.findByPortalBlock(this);
        if (def == null) return;

        PortalTeleporter.teleport(player, serverLevel, def);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Амбиент звук портала — редко как у незерского
        if (random.nextInt(100) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT,
                    SoundSource.BLOCKS,
                    0.5f,
                    random.nextFloat() * 0.1f + 0.1f,
                    false
            );
        }

        // Частицы
        Direction.Axis axis = state.getValue(AXIS);
        for (int i = 0; i < 4; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + random.nextDouble();

            // Смещаем частицы в плоскость портала
            if (axis == Direction.Axis.X) {
                z = pos.getZ() + 0.5;
            } else {
                x = pos.getX() + 0.5;
            }

            level.addParticle(
                    ModParticles.NULL_PARTICLE.get(),
                    x, y, z,
                    (random.nextDouble() - 0.5) * 0.5,
                    (random.nextDouble() - 0.5) * 0.5,
                    (random.nextDouble() - 0.5) * 0.5
            );
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
                                  LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        Direction.Axis axis = state.getValue(AXIS);

        if (facing.getAxis() != axis && facing.getAxis().isHorizontal()) {
            return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
        } else {
            PortalDefinition def = PortalRegistry.findByPortalBlock(this);
            if (def != null) {
                boolean isValidNeighbor = facingState.is(this) || facingState.is(def.getFrameBlock());
                if (!isValidNeighbor) {
                    return Blocks.AIR.defaultBlockState();
                }
            }
            return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state,
                                    net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        return List.of();
    }

    @Override
    public boolean canBeReplaced(BlockState state,
                                 net.minecraft.world.item.context.BlockPlaceContext context) {
        return false;
    }
}