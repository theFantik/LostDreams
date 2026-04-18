package net.fantik.lostdreams.datagen.providers;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.ExistingFileHelper;


import java.util.concurrent.CompletableFuture;

import static net.minecraft.tags.TagEntry.tag;

public class ModItemTagProvider extends ItemTagsProvider {

    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                              ModBlockTagProvider blockTagProvider, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagProvider.contentsGetter(), LostDreams.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ItemTags.LOGS).add(
                ModBlocks.DUSKWILLOW_LOG.get().asItem(),
                ModBlocks.DUSKWILLOW_WOOD.get().asItem(),
                ModBlocks.STRIPPED_DUSKWILLOW_LOG.get().asItem(),
                ModBlocks.STRIPPED_DUSKWILLOW_WOOD.get().asItem());

        tag(ItemTags.LOGS_THAT_BURN).add(
                ModBlocks.DUSKWILLOW_LOG.get().asItem(),
                ModBlocks.DUSKWILLOW_WOOD.get().asItem(),
                ModBlocks.STRIPPED_DUSKWILLOW_LOG.get().asItem(),
                ModBlocks.STRIPPED_DUSKWILLOW_WOOD.get().asItem());



        tag(ItemTags.PLANKS).add(ModBlocks.DUSKWILLOW_PLANKS.get().asItem());
        tag(ItemTags.WOODEN_FENCES).add(ModBlocks.DUSKWILLOW_FENCE.get().asItem());
        tag(ItemTags.WOODEN_DOORS).add(ModBlocks.DUSKWILLOW_DOOR.get().asItem());
        tag(ItemTags.WOODEN_TRAPDOORS).add(ModBlocks.DUSKWILLOW_TRAPDOOR.get().asItem());
        tag(ItemTags.WOODEN_BUTTONS).add(ModBlocks.DUSKWILLOW_BUTTON.get().asItem());
        tag(ItemTags.WOODEN_SLABS).add(ModBlocks.DUSKWILLOW_SLAB.get().asItem());
        tag(ItemTags.WOODEN_STAIRS).add(ModBlocks.DUSKWILLOW_STAIRS.get().asItem());
        tag(ItemTags.LEAVES).add(ModBlocks.DUSKWILLOW_LEAVES.get().asItem());
        tag(ItemTags.SAPLINGS).add(ModBlocks.DUSKWILLOW_SAPLING.get().asItem());
        tag(ItemTags.FLOWERS).add(ModBlocks.NULL_BLOSSOM.get().asItem());

        tag(ItemTags.STONE_CRAFTING_MATERIALS).add(ModBlocks.SURREAL_BLUE_ROCK.get().asItem(),
                ModBlocks.SURREAL_RED_ROCK.get().asItem(),
                ModBlocks.SURREAL_PURPLE_ROCK.get().asItem(),
                ModBlocks.SURREAL_LIGHTBLUE_ROCK.get().asItem(),
                ModBlocks.SURREAL_GREEN_ROCK.get().asItem(),
                ModBlocks.SURREAL_YELLOW_ROCK.get().asItem(),
                ModBlocks.NULL_BRICKS.get().asItem(),
                ModBlocks.NULL_STONE.get().asItem(),
                ModBlocks.NULL_CRACKED_BRICKS.get().asItem());
        tag(ItemTags.STONE_TOOL_MATERIALS).add(ModBlocks.SURREAL_BLUE_ROCK.get().asItem(),
                ModBlocks.SURREAL_RED_ROCK.get().asItem(),
                ModBlocks.SURREAL_PURPLE_ROCK.get().asItem(),
                ModBlocks.SURREAL_LIGHTBLUE_ROCK.get().asItem(),
                ModBlocks.SURREAL_GREEN_ROCK.get().asItem(),
                ModBlocks.SURREAL_YELLOW_ROCK.get().asItem(),
                ModBlocks.NULL_BRICKS.get().asItem(),
                ModBlocks.NULL_STONE.get().asItem(),
                ModBlocks.NULL_CRACKED_BRICKS.get().asItem());
        ;
    }
}