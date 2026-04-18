package net.fantik.lostdreams.datagen.providers;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {

    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                               ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, LostDreams.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Брёвна
        tag(BlockTags.LOGS)
                .add(ModBlocks.DUSKWILLOW_LOG.get())
                .add(ModBlocks.DUSKWILLOW_WOOD.get())
                .add(ModBlocks.STRIPPED_DUSKWILLOW_LOG.get())
                .add(ModBlocks.STRIPPED_DUSKWILLOW_WOOD.get());

        tag(BlockTags.LOGS_THAT_BURN)
                .add(ModBlocks.DUSKWILLOW_LOG.get())
                .add(ModBlocks.DUSKWILLOW_WOOD.get())
                .add(ModBlocks.STRIPPED_DUSKWILLOW_LOG.get())
                .add(ModBlocks.STRIPPED_DUSKWILLOW_WOOD.get());

        // Планки
        tag(BlockTags.PLANKS).add(ModBlocks.DUSKWILLOW_PLANKS.get());

        // Листья
        tag(BlockTags.LEAVES).add(ModBlocks.DUSKWILLOW_LEAVES.get());

        // Заборы
        tag(BlockTags.FENCES).add(ModBlocks.DUSKWILLOW_FENCE.get());
        tag(BlockTags.WOODEN_FENCES).add(ModBlocks.DUSKWILLOW_FENCE.get());

        // Двери и люки
        tag(BlockTags.WOODEN_DOORS).add(ModBlocks.DUSKWILLOW_DOOR.get());
        tag(BlockTags.WOODEN_TRAPDOORS).add(ModBlocks.DUSKWILLOW_TRAPDOOR.get());
        tag(BlockTags.WOODEN_BUTTONS).add(ModBlocks.DUSKWILLOW_BUTTON.get());
        tag(BlockTags.WOODEN_PRESSURE_PLATES).add(ModBlocks.DUSKWILLOW_PRESSURE_PLATE.get());
        tag(BlockTags.WOODEN_SLABS).add(ModBlocks.DUSKWILLOW_SLAB.get());
        tag(BlockTags.WOODEN_STAIRS).add(ModBlocks.DUSKWILLOW_STAIRS.get());

        // Mineable
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.DUSKWILLOW_LOG.get())
                .add(ModBlocks.DUSKWILLOW_WOOD.get())
                .add(ModBlocks.STRIPPED_DUSKWILLOW_LOG.get())
                .add(ModBlocks.STRIPPED_DUSKWILLOW_WOOD.get())
                .add(ModBlocks.DUSKWILLOW_PLANKS.get())
                .add(ModBlocks.DUSKWILLOW_STAIRS.get())
                .add(ModBlocks.DUSKWILLOW_SLAB.get())
                .add(ModBlocks.DUSKWILLOW_FENCE.get())
                .add(ModBlocks.DUSKWILLOW_FENCE_GATE.get())
                .add(ModBlocks.DUSKWILLOW_DOOR.get())
                .add(ModBlocks.DUSKWILLOW_TRAPDOOR.get())
                .add(ModBlocks.DUSKWILLOW_BUTTON.get())
                .add(ModBlocks.DUSKWILLOW_PRESSURE_PLATE.get());

        tag(BlockTags.MUSHROOM_GROW_BLOCK)
                .add(ModBlocks.NULL_GROUND.get());

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.NULL_STONE.get())
                .add(ModBlocks.NULL_BRICKS.get())
                .add(ModBlocks.NULL_CRACKED_BRICKS.get())
                .add(ModBlocks.SURREAL_RED_ROCK.get())
                .add(ModBlocks.SURREAL_LIGHTBLUE_ROCK.get())
                .add(ModBlocks.SURREAL_GREEN_ROCK.get())
                .add(ModBlocks.SURREAL_PURPLE_ROCK.get())
                .add(ModBlocks.SURREAL_BLUE_ROCK.get())
                .add(ModBlocks.SURREAL_YELLOW_ROCK.get())
                .add(ModBlocks.SURREAL_GLOWCRYSTAL.get())
                .add(ModBlocks.SURREAL_BLUE_RANDOMITE_ORE.get())
                .add(ModBlocks.SURREAL_LIGHTBLUE_RANDOMITE_ORE.get())
                .add(ModBlocks.SURREAL_GREEN_RANDOMITE_ORE.get())
                .add(ModBlocks.SURREAL_PURPLE_RANDOMITE_ORE.get())
                .add(ModBlocks.SURREAL_RED_RANDOMITE_ORE.get())
                .add(ModBlocks.SURREAL_YELLOW_RANDOMITE_ORE.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.SURREAL_BLUE_RANDOMITE_ORE.get())
                .add(ModBlocks.SURREAL_LIGHTBLUE_RANDOMITE_ORE.get())
                .add(ModBlocks.SURREAL_GREEN_RANDOMITE_ORE.get())
                .add(ModBlocks.SURREAL_PURPLE_RANDOMITE_ORE.get())
                .add(ModBlocks.SURREAL_RED_RANDOMITE_ORE.get())
                .add(ModBlocks.SURREAL_YELLOW_RANDOMITE_ORE.get());

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.NULL_GROUND.get())
                .add(ModBlocks.PINK_KNOWLEDGE_BLOCK.get())
                .add(ModBlocks.GREEN_KNOWLEDGE_BLOCK.get())
                .add(ModBlocks.BLUE_KNOWLEDGE_BLOCK.get());

        // Dirt для саженцев
        tag(BlockTags.DIRT)
                .add(ModBlocks.PINK_KNOWLEDGE_BLOCK.get())
                .add(ModBlocks.BLUE_KNOWLEDGE_BLOCK.get())
                .add(ModBlocks.GREEN_KNOWLEDGE_BLOCK.get())
                .add(ModBlocks.NULL_GROUND.get());
    }
}