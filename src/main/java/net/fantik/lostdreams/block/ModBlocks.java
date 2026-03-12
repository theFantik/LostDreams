package net.fantik.lostdreams.block;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.item.ModItems;
import net.fantik.lostdreams.sound.ModSounds;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(LostDreams.MOD_ID);

    public static final DeferredBlock<Block> FEATHER_BLOCK = registerBlock("feather_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4f).sound(SoundType.WOOL).destroyTime(0.5f)
            ));

    public static final DeferredBlock<Block> NULL_GROUND = registerBlock("null_ground",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4f).sound(ModSounds.getNullGroundSoundType()).destroyTime(1f)
            ));

    public static final DeferredBlock<Block> NULL_STONE = registerBlock("null_stone",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));

    public static final DeferredBlock<Block> NULL_BRICKS = registerBlock("null_bricks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3f).sound(SoundType.STONE).destroyTime(1.5f)
            ));

    public static final DeferredBlock<Block> NULL_CRACKED_BRICKS = registerBlock("null_cracked_bricks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(2f).sound(SoundType.STONE).destroyTime(1.5f)
            ));

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name,() -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
