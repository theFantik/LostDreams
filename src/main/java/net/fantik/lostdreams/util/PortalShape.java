package net.fantik.lostdreams.util;

import net.fantik.lostdreams.block.PortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;

public class PortalShape {
    private final LevelAccessor level;
    private final PortalDefinition def;
    private final Direction.Axis axis;
    private final Direction rightDir;

    private int numPortalBlocks;
    @Nullable
    private BlockPos bottomLeft;
    private int height;
    private final int width;

    // Ищем портал, проверяя обе оси (X и Z)
    public static Optional<PortalShape> find(LevelAccessor level, BlockPos pos, PortalDefinition def) {
        Optional<PortalShape> shapeX = Optional.of(new PortalShape(level, pos, Direction.Axis.X, def))
                .filter(PortalShape::isValid);

        if (shapeX.isPresent()) return shapeX;

        return Optional.of(new PortalShape(level, pos, Direction.Axis.Z, def))
                .filter(PortalShape::isValid);
    }

    private PortalShape(LevelAccessor level, BlockPos bottomLeft, Direction.Axis axis, PortalDefinition def) {
        this.level = level;
        this.axis = axis;
        this.def = def;
        this.rightDir = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;

        this.bottomLeft = this.calculateBottomLeft(bottomLeft);
        if (this.bottomLeft == null) {
            this.bottomLeft = bottomLeft;
            this.width = 1;
            this.height = 1;
        } else {
            this.width = this.calculateWidth();
            if (this.width > 0) {
                this.height = this.calculateHeight();
            }
        }
    }

    @Nullable
    private BlockPos calculateBottomLeft(BlockPos pos) {
        // Ограничиваем спуск вниз, чтобы не улететь в бездну
        int i = Math.max(this.level.getMinBuildHeight(), pos.getY() - def.getMaxHeight());

        while (pos.getY() > i && isInside(this.level.getBlockState(pos.below()))) {
            pos = pos.below();
        }

        Direction direction = this.rightDir.getOpposite(); // Идем "влево"
        int j = this.getDistanceUntilEdgeAboveFrame(pos, direction) - 1;
        return j < 0 ? null : pos.relative(direction, j);
    }

    private int calculateWidth() {
        int i = this.getDistanceUntilEdgeAboveFrame(this.bottomLeft, this.rightDir);
        return i >= def.getMinWidth() && i <= def.getMaxWidth() ? i : 0;
    }

    private int getDistanceUntilEdgeAboveFrame(BlockPos pos, Direction direction) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        // ВАЖНО: Жесткий лимит цикла, чтобы игра больше не висла!
        for (int i = 0; i <= def.getMaxWidth(); i++) {
            mut.set(pos).move(direction, i);
            BlockState state = this.level.getBlockState(mut);

            if (!isInside(state)) {
                if (state.is(def.getFrameBlock())) {
                    return i;
                }
                break;
            }

            BlockState stateBelow = this.level.getBlockState(mut.move(Direction.DOWN));
            if (!stateBelow.is(def.getFrameBlock())) {
                break;
            }
        }
        return 0;
    }

    private int calculateHeight() {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        int i = this.getDistanceUntilTop(mut);
        return i >= def.getMinHeight() && i <= def.getMaxHeight() && this.hasTopFrame(mut, i) ? i : 0;
    }

    private boolean hasTopFrame(BlockPos.MutableBlockPos pos, int distanceToTop) {
        for (int i = 0; i < this.width; i++) {
            pos.set(this.bottomLeft).move(Direction.UP, distanceToTop).move(this.rightDir, i);
            if (!this.level.getBlockState(pos).is(def.getFrameBlock())) {
                return false;
            }
        }
        return true;
    }

    private int getDistanceUntilTop(BlockPos.MutableBlockPos pos) {
        for (int i = 0; i <= def.getMaxHeight(); i++) {
            // Левая стенка
            pos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, -1);
            if (!this.level.getBlockState(pos).is(def.getFrameBlock())) return i;

            // Правая стенка
            pos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, this.width);
            if (!this.level.getBlockState(pos).is(def.getFrameBlock())) return i;

            // Внутренности
            for (int j = 0; j < this.width; j++) {
                pos.set(this.bottomLeft).move(Direction.UP, i).move(this.rightDir, j);
                BlockState state = this.level.getBlockState(pos);
                if (!isInside(state)) {
                    return i;
                }
                if (state.is(def.getPortalBlock())) {
                    this.numPortalBlocks++;
                }
            }
        }
        return def.getMaxHeight();
    }

    private boolean isInside(BlockState state) {
        // Огонь тоже считается пустотой, так как мы им кликаем
        return state.isAir() || state.canBeReplaced() || state.is(def.getPortalBlock());
    }

    public boolean isValid() {
        return this.bottomLeft != null
                && this.width >= def.getMinWidth() && this.width <= def.getMaxWidth()
                && this.height >= def.getMinHeight() && this.height <= def.getMaxHeight();
    }

    public void fill() {
        BlockState portalState = def.getPortalBlock().defaultBlockState()
                .setValue(PortalBlock.AXIS, this.axis);

        BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1))
                .forEach(pos -> this.level.setBlock(pos, portalState, 18));
    }
}