package net.fantik.lostdreams.block;

import net.fantik.lostdreams.block.entity.TestPortalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

public class TestPortalBlock extends BaseEntityBlock {

    public static final MapCodec<TestPortalBlock> CODEC =
            simpleCodec(TestPortalBlock::new);

    public TestPortalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Сам блок не рисуем.
        // Его поверхность будет рисовать BlockEntityRenderer.
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new TestPortalBlockEntity(pos, state);
    }
}