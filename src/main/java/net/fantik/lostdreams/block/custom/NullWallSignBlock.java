package net.fantik.lostdreams.block.custom;

import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.block.ModWoodTypes;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;

public class NullWallSignBlock extends WallSignBlock {

    public NullWallSignBlock() {
        super(ModWoodTypes.NULL_SIGN_WOOD_TYPE,
                BlockBehaviour.Properties.of().sound(SoundType.WOOD).strength(1f).noCollission().ignitedByLava().instrument(NoteBlockInstrument.BASS).forceSolidOn().dropsLike(ModBlocks.NULL_SIGN.get()));

    }


}
