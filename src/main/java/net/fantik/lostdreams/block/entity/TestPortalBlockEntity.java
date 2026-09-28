package net.fantik.lostdreams.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TestPortalBlockEntity extends BlockEntity {

    public TestPortalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TEST_PORTAL_BE.get(), pos, state);
    }
}