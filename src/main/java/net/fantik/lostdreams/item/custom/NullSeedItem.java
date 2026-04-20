package net.fantik.lostdreams.item.custom;

import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class NullSeedItem extends Item {

    public NullSeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        // Только по дёрну или земле
        boolean validSoil = state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(ModBlocks.NULL_GROUND.get());

        if (!validSoil) return InteractionResult.PASS;

        BlockPos above = pos.above();
        if (!level.isEmptyBlock(above)) return InteractionResult.PASS;

        if (!level.isClientSide) {
            // Ставим crop
            level.setBlock(above,
                    ModBlocks.NULL_CROP.get().defaultBlockState(), 2);

            level.playSound(null, above,
                    SoundEvents.CROP_PLANTED,
                    SoundSource.BLOCKS, 1f, 1f);

            // Убираем семя из руки
            if (!context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
