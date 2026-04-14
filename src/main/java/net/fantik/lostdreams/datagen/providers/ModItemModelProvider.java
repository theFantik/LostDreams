package net.fantik.lostdreams.datagen.providers;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, LostDreams.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // Простые предметы (item/generated)
        basicItem(ModItems.PILLOW.get());
        basicItem(ModItems.NULL_PILLOW.get());
        basicItem(ModItems.BUG_ANTENNA.get());
        basicItem(ModItems.LUCID_ESSENCE.get());
        basicItem(ModItems.DREAM_CATCHER.get());

        // Spawn eggs — используют builtin/template_spawn_egg
        withExistingParent(ModItems.NULL_BUG_SPAWN_EGG.get().toString(),
                mcLoc("item/template_spawn_egg"));
        withExistingParent(ModItems.LUCID_WASTE_SPAWN_EGG.get().toString(),
                mcLoc("item/template_spawn_egg"));

        // Блоки — наследуют от блочной модели
        blockItem(ModBlocks.DUSKWILLOW_LOG);
        blockItem(ModBlocks.DUSKWILLOW_WOOD);
        blockItem(ModBlocks.STRIPPED_DUSKWILLOW_LOG);
        blockItem(ModBlocks.STRIPPED_DUSKWILLOW_WOOD);
        blockItem(ModBlocks.DUSKWILLOW_PLANKS);
        blockItem(ModBlocks.DUSKWILLOW_LEAVES);
        blockItem(ModBlocks.DUSKWILLOW_STAIRS);
        blockItem(ModBlocks.DUSKWILLOW_FENCE_GATE);
        withExistingParent("duskwillow_trapdoor", modLoc("block/duskwillow_trapdoor_bottom"));
        blockItem(ModBlocks.DUSKWILLOW_PRESSURE_PLATE);


        // Саженец — item/generated
        withExistingParent("duskwillow_sapling", mcLoc("item/generated"))
                .texture("layer0", modLoc("block/duskwillow_sapling"));

        // Дверь — item/generated с нижней текстурой
        withExistingParent("duskwillow_door", mcLoc("item/generated"))
                .texture("layer0", modLoc("block/duskwillow_door_bottom"));

        // Забор — inventory модель
        withExistingParent("duskwillow_fence", mcLoc("block/fence_inventory"))
                .texture("texture", modLoc("block/duskwillow_planks"));

        // Кнопка — inventory модель
        withExistingParent("duskwillow_button", mcLoc("block/button_inventory"))
                .texture("texture", modLoc("block/duskwillow_planks"));

        // Плита
        withExistingParent("duskwillow_slab",
                modLoc("block/duskwillow_slab"));



        // Простые блоки
        blockItem(ModBlocks.NULL_STONE);
        blockItem(ModBlocks.SURREAL_BLUE_ROCK);
        blockItem(ModBlocks.SURREAL_RED_ROCK);
        blockItem(ModBlocks.SURREAL_GREEN_ROCK);
        blockItem(ModBlocks.SURREAL_LIGHTBLUE_ROCK);
        blockItem(ModBlocks.SURREAL_PURPLE_ROCK);
        blockItem(ModBlocks.SURREAL_YELLOW_ROCK);
        blockItem(ModBlocks.SURREAL_GLOWCRYSTAL);
        blockItem(ModBlocks.SURREAL_BLUE_RANDOMITE_ORE);
        blockItem(ModBlocks.SURREAL_LIGHTBLUE_RANDOMITE_ORE);
        blockItem(ModBlocks.SURREAL_GREEN_RANDOMITE_ORE);
        blockItem(ModBlocks.SURREAL_PURPLE_RANDOMITE_ORE);
        blockItem(ModBlocks.SURREAL_RED_RANDOMITE_ORE);
        blockItem(ModBlocks.SURREAL_YELLOW_RANDOMITE_ORE);
        blockItem(ModBlocks.NULL_BRICKS);
        blockItem(ModBlocks.NULL_CRACKED_BRICKS);
        blockItem(ModBlocks.NULL_GROUND);
        blockItem(ModBlocks.FEATHER_BLOCK);
        blockItem(ModBlocks.PINK_KNOWLEDGE_BLOCK);
        blockItem(ModBlocks.BLUE_KNOWLEDGE_BLOCK);
        blockItem(ModBlocks.GREEN_KNOWLEDGE_BLOCK);
        blockItem(ModBlocks.DREAM_GENERATOR);
        blockItem(ModBlocks.DREAM_ADVANCED_GENERATOR);
    }

    private <T extends Block> void blockItem(net.neoforged.neoforge.registries.DeferredBlock<T> block) {
        withExistingParent(block.getId().getPath(), modLoc("block/" + block.getId().getPath()));
    }
}