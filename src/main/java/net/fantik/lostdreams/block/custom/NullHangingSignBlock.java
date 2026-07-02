package net.fantik.lostdreams.block.custom;

import net.fantik.lostdreams.block.ModWoodTypes;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public class NullHangingSignBlock extends CeilingHangingSignBlock {
    public NullHangingSignBlock() {
        super(ModWoodTypes.NULL_HANGING_SIGN_WOOD_TYPE, BlockBehaviour.Properties.of().sound(SoundType.HANGING_SIGN).strength(1f).noCollission().ignitedByLava().instrument(NoteBlockInstrument.BASS).forceSolidOn());
    }
}
