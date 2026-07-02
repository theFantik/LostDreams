package net.fantik.lostdreams.block.custom;

import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.block.ModWoodTypes;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public class NullWallHangingSignBlock extends WallHangingSignBlock {
    public NullWallHangingSignBlock() {
        super(ModWoodTypes.NULL_HANGING_SIGN_WOOD_TYPE,
                BlockBehaviour.Properties.of().sound(SoundType.HANGING_SIGN).strength(1f).noCollission().ignitedByLava().instrument(NoteBlockInstrument.BASS).forceSolidOn().dropsLike(ModBlocks.NULL_HANGING_SIGN.get()));
    }
}
