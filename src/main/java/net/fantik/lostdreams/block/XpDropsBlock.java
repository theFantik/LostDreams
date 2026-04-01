package net.fantik.lostdreams.block;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class XpDropsBlock extends DropExperienceBlock {
    public XpDropsBlock(BlockBehaviour.Properties props)
    {
        // ⭐ опыт как у обычных руд
        super(UniformInt.of(3, 7), props);
    }
}
