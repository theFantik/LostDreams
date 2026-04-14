package net.fantik.lostdreams.datagen.providers;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, LostDreams.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {

        add(ModBlocks.DREAM_GENERATOR.get(), "Dream Generator");
        add(ModBlocks.DREAM_ADVANCED_GENERATOR.get(), "Dream Advanced Generator");
        add("container.lostdreams.dream_generator", "Dream Generator");

        // Items
        add(ModItems.PILLOW.get(), "Pillow");
        add(ModItems.NULL_PILLOW.get(), "Null Pillow");
        add(ModItems.BUG_ANTENNA.get(), "Null Bug Antenna");
        add(ModItems.LUCID_ESSENCE.get(), "Lucid Essence");
        add(ModItems.DREAM_CATCHER.get(), "Dream Catcher");
        add(ModItems.NULL_BUG_SPAWN_EGG.get(), "Null Bug Spawn Egg");
        add(ModItems.LUCID_WASTE_SPAWN_EGG.get(), "Lucid Waste Spawn Egg");

        // Null Zone блоки
        add(ModBlocks.FEATHER_BLOCK.get(), "Feather Block");
        add(ModBlocks.NULL_GROUND.get(), "Null Ground");
        add(ModBlocks.NULL_STONE.get(), "Null Stone");
        add(ModBlocks.NULL_BRICKS.get(), "Null Bricks");
        add(ModBlocks.NULL_CRACKED_BRICKS.get(), "Null Cracked Bricks");

        add(ModBlocks.NULL_ZONE_PORTAL.get(), "Null Zone Portal");

        // Knowledge блоки
        add(ModBlocks.PINK_KNOWLEDGE_BLOCK.get(), "Pink Knowledge Block");
        add(ModBlocks.BLUE_KNOWLEDGE_BLOCK.get(), "Blue Knowledge Block");
        add(ModBlocks.GREEN_KNOWLEDGE_BLOCK.get(), "Green Knowledge Block");

        // Duskwillow дерево
        add(ModBlocks.DUSKWILLOW_LOG.get(), "Duskwillow Log");
        add(ModBlocks.DUSKWILLOW_WOOD.get(), "Duskwillow Wood");
        add(ModBlocks.STRIPPED_DUSKWILLOW_LOG.get(), "Stripped Duskwillow Log");
        add(ModBlocks.STRIPPED_DUSKWILLOW_WOOD.get(), "Stripped Duskwillow Wood");
        add(ModBlocks.DUSKWILLOW_LEAVES.get(), "Duskwillow Leaves");
        add(ModBlocks.DUSKWILLOW_SAPLING.get(), "Duskwillow Sapling");
        add(ModBlocks.DUSKWILLOW_PLANKS.get(), "Duskwillow Planks");
        add(ModBlocks.DUSKWILLOW_STAIRS.get(), "Duskwillow Stairs");
        add(ModBlocks.DUSKWILLOW_SLAB.get(), "Duskwillow Slab");
        add(ModBlocks.DUSKWILLOW_FENCE.get(), "Duskwillow Fence");
        add(ModBlocks.DUSKWILLOW_FENCE_GATE.get(), "Duskwillow Fence Gate");
        add(ModBlocks.DUSKWILLOW_DOOR.get(), "Duskwillow Door");
        add(ModBlocks.DUSKWILLOW_TRAPDOOR.get(), "Duskwillow Trapdoor");
        add(ModBlocks.DUSKWILLOW_BUTTON.get(), "Duskwillow Button");
        add(ModBlocks.DUSKWILLOW_PRESSURE_PLATE.get(), "Duskwillow Pressure Plate");


        // Сюр блоки

        add(ModBlocks.SURREAL_BLUE_ROCK.get(), "Surreal Blue Rock");
        add(ModBlocks.SURREAL_RED_ROCK.get(), "Surreal Red Rock");
        add(ModBlocks.SURREAL_YELLOW_ROCK.get(), "Surreal Yellow Rock");
        add(ModBlocks.SURREAL_LIGHTBLUE_ROCK.get(), "Surreal Light Blue Rock");
        add(ModBlocks.SURREAL_GREEN_ROCK.get(), "Surreal Green Rock");
        add(ModBlocks.SURREAL_PURPLE_ROCK.get(), "Surreal Purple Rock");
        add(ModBlocks.SURREAL_GLOWCRYSTAL.get(), "Surreal Glow Crystal");

        add(ModBlocks.SURREAL_BLUE_RANDOMITE_ORE.get(), "Blue Randomite Ore");
        add(ModBlocks.SURREAL_LIGHTBLUE_RANDOMITE_ORE.get(), "Light Blue Randomite Ore");
        add(ModBlocks.SURREAL_GREEN_RANDOMITE_ORE.get(), "Green Randomite Ore");
        add(ModBlocks.SURREAL_PURPLE_RANDOMITE_ORE.get(), "Purple Randomite Ore");
        add(ModBlocks.SURREAL_RED_RANDOMITE_ORE.get(), "Red Randomite Ore");
        add(ModBlocks.SURREAL_YELLOW_RANDOMITE_ORE.get(), "Yellow Randomite Ore");

        // Creative tabs
        add("creativetab.lostdreams.bed_items", "Bed Items");
        add("creativetab.lostdreams.dream_blocks", "Dream Blocks");
        add("creativetab.lostdreams.dream_mobs", "Dream Mobs");
    }
}