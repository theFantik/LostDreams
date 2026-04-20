package net.fantik.lostdreams.datagen.providers;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.item.ModItems;
import net.fantik.lostdreams.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // Планки из бревна
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DUSKWILLOW_PLANKS.get(), 4)
                .requires(ModBlocks.DUSKWILLOW_LOG.get())
                .unlockedBy("has_log", has(ModBlocks.DUSKWILLOW_LOG.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DUSKWILLOW_PLANKS.get(), 4)
                .requires(ModBlocks.DUSKWILLOW_WOOD.get())
                .unlockedBy("has_wood", has(ModBlocks.DUSKWILLOW_WOOD.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "duskwillow_planks_from_wood"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DUSKWILLOW_PLANKS.get(), 4)
                .requires(ModBlocks.STRIPPED_DUSKWILLOW_LOG.get())
                .unlockedBy("has_stripped_log", has(ModBlocks.STRIPPED_DUSKWILLOW_LOG.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "duskwillow_planks_from_stripped_log"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DUSKWILLOW_PLANKS.get(), 4)
                .requires(ModBlocks.STRIPPED_DUSKWILLOW_WOOD.get())
                .unlockedBy("has_stripped_wood", has(ModBlocks.STRIPPED_DUSKWILLOW_WOOD.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "duskwillow_planks_from_stripped_wood"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.NULL_PLANKS.get(), 4)
                .requires(ModBlocks.NULL_LOG.get())
                .unlockedBy("has_log", has(ModBlocks.NULL_LOG.get()))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.NULL_PLANKS.get(), 4)
                .requires(ModBlocks.NULL_WOOD.get())
                .unlockedBy("has_wood", has(ModBlocks.NULL_WOOD.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "null_planks_from_wood"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.NULL_PLANKS.get(), 4)
                .requires(ModBlocks.STRIPPED_NULL_LOG.get())
                .unlockedBy("has_stripped_log", has(ModBlocks.STRIPPED_NULL_LOG.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "null_planks_from_stripped_log"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.NULL_PLANKS.get(), 4)
                .requires(ModBlocks.STRIPPED_NULL_WOOD.get())
                .unlockedBy("has_stripped_wood", has(ModBlocks.STRIPPED_NULL_WOOD.get()))
                .save(output, ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "null_planks_from_stripped_wood"));

        // CVETOK

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.ORANGE_DYE.asItem(),1)
                .requires(ModBlocks.NULL_BLOSSOM.get())
                .unlockedBy("has_null_blossom",has(ModBlocks.NULL_BLOSSOM))
                .save(output,ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID,"orange_dye_from_null_blossom"));

        // Древесина
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DUSKWILLOW_WOOD.get(), 3)
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.DUSKWILLOW_LOG.get())
                .unlockedBy("has_log", has(ModBlocks.DUSKWILLOW_LOG.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRIPPED_DUSKWILLOW_WOOD.get(), 3)
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.STRIPPED_DUSKWILLOW_LOG.get())
                .unlockedBy("has_stripped_log", has(ModBlocks.STRIPPED_DUSKWILLOW_LOG.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.NULL_WOOD.get(), 3)
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.NULL_LOG.get())
                .unlockedBy("has_log", has(ModBlocks.NULL_LOG.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRIPPED_NULL_WOOD.get(), 3)
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.STRIPPED_NULL_LOG.get())
                .unlockedBy("has_stripped_log", has(ModBlocks.STRIPPED_NULL_LOG.get()))
                .save(output);

        // Ступеньки
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DUSKWILLOW_STAIRS.get(), 4)
                .pattern("#  ")
                .pattern("## ")
                .pattern("###")
                .define('#', ModBlocks.DUSKWILLOW_PLANKS.get())
                .unlockedBy("has_planks", has(ModBlocks.DUSKWILLOW_PLANKS.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.NULL_STAIRS.get(), 4)
                .pattern("#  ")
                .pattern("## ")
                .pattern("###")
                .define('#', ModBlocks.NULL_PLANKS.get())
                .unlockedBy("has_planks", has(ModBlocks.NULL_PLANKS.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.NULL_STONE_STAIRS.get(), 4)
                .pattern("#  ")
                .pattern("## ")
                .pattern("###")
                .define('#', ModBlocks.NULL_STONE.get())
                .unlockedBy("has_stone", has(ModBlocks.NULL_STONE.get()))
                .save(output);

        // Плита
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DUSKWILLOW_SLAB.get(), 6)
                .pattern("###")
                .define('#', ModBlocks.DUSKWILLOW_PLANKS.get())
                .unlockedBy("has_planks", has(ModBlocks.DUSKWILLOW_PLANKS.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.NULL_SLAB.get(), 6)
                .pattern("###")
                .define('#', ModBlocks.NULL_PLANKS.get())
                .unlockedBy("has_planks", has(ModBlocks.NULL_PLANKS.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.NULL_STONE_SLAB.get(), 6)
                .pattern("###")
                .define('#', ModBlocks.NULL_STONE.get())
                .unlockedBy("has_stone", has(ModBlocks.NULL_STONE.get()))
                .save(output);

        // Забор
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.DUSKWILLOW_FENCE.get(), 3)
                .pattern("#S#")
                .pattern("#S#")
                .define('#', ModBlocks.DUSKWILLOW_PLANKS.get())
                .define('S', net.minecraft.world.item.Items.STICK)
                .unlockedBy("has_planks", has(ModBlocks.DUSKWILLOW_PLANKS.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.NULL_FENCE.get(), 3)
                .pattern("#S#")
                .pattern("#S#")
                .define('#', ModBlocks.NULL_PLANKS.get())
                .define('S', net.minecraft.world.item.Items.STICK)
                .unlockedBy("has_planks", has(ModBlocks.NULL_PLANKS.get()))
                .save(output);

        // Калитка
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.DUSKWILLOW_FENCE_GATE.get(), 1)
                .pattern("S#S")
                .pattern("S#S")
                .define('#', ModBlocks.DUSKWILLOW_PLANKS.get())
                .define('S', net.minecraft.world.item.Items.STICK)
                .unlockedBy("has_planks", has(ModBlocks.DUSKWILLOW_PLANKS.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.NULL_FENCE_GATE.get(), 1)
                .pattern("S#S")
                .pattern("S#S")
                .define('#', ModBlocks.NULL_PLANKS.get())
                .define('S', net.minecraft.world.item.Items.STICK)
                .unlockedBy("has_planks", has(ModBlocks.NULL_PLANKS.get()))
                .save(output);

        // Дверь
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.DUSKWILLOW_DOOR.get(), 3)
                .pattern("##")
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.DUSKWILLOW_PLANKS.get())
                .unlockedBy("has_planks", has(ModBlocks.DUSKWILLOW_PLANKS.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.NULL_DOOR.get(), 3)
                .pattern("##")
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.NULL_PLANKS.get())
                .unlockedBy("has_planks", has(ModBlocks.NULL_PLANKS.get()))
                .save(output);

        // Люк
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.DUSKWILLOW_TRAPDOOR.get(), 2)
                .pattern("###")
                .pattern("###")
                .define('#', ModBlocks.DUSKWILLOW_PLANKS.get())
                .unlockedBy("has_planks", has(ModBlocks.DUSKWILLOW_PLANKS.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.NULL_TRAPDOOR.get(), 2)
                .pattern("###")
                .pattern("###")
                .define('#', ModBlocks.NULL_PLANKS.get())
                .unlockedBy("has_planks", has(ModBlocks.NULL_PLANKS.get()))
                .save(output);

        // Кнопка
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModBlocks.DUSKWILLOW_BUTTON.get(), 1)
                .requires(ModBlocks.DUSKWILLOW_PLANKS.get())
                .unlockedBy("has_planks", has(ModBlocks.DUSKWILLOW_PLANKS.get()))
                .save(output);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, ModBlocks.NULL_BUTTON.get(), 1)
                .requires(ModBlocks.NULL_PLANKS.get())
                .unlockedBy("has_planks", has(ModBlocks.NULL_PLANKS.get()))
                .save(output);

        // Плита давления
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.DUSKWILLOW_PRESSURE_PLATE.get(), 1)
                .pattern("##")
                .define('#', ModBlocks.DUSKWILLOW_PLANKS.get())
                .unlockedBy("has_planks", has(ModBlocks.DUSKWILLOW_PLANKS.get()))
                .save(output);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.NULL_PRESSURE_PLATE.get(), 1)
                .pattern("##")
                .define('#', ModBlocks.NULL_PLANKS.get())
                .unlockedBy("has_planks", has(ModBlocks.NULL_PLANKS.get()))
                .save(output);

        // generators

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DREAM_GENERATOR.get())
                .pattern("ISI")
                .pattern("SBS")
                .pattern("ISI")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.SMOOTH_STONE)
                .define('B', Items.STRING)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DREAM_ADVANCED_GENERATOR.get())
                .pattern("ISI")
                .pattern("SBS")
                .pattern("ISI")
                .define('I', Items.GOLD_INGOT)
                .define('S', ModItems.LUCID_ESSENCE)
                .define('B', ModBlocks.DREAM_GENERATOR)
                .unlockedBy("has_lucid_essence", has(ModItems.LUCID_ESSENCE))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DREAM_ELITE_GENERATOR.get())
                .pattern("ITI")
                .pattern("SBS")
                .pattern("ITI")
                .define('I', Items.DIAMOND)
                .define('T', ModTags.Items.KNOWLEDGE_BLOCKS)
                .define('S', ModBlocks.SURREAL_GLOWCRYSTAL)
                .define('B', ModBlocks.DREAM_ADVANCED_GENERATOR)
                .unlockedBy("has_adv_generator", has(ModBlocks.DREAM_ADVANCED_GENERATOR))
                .save(output);

    }
}