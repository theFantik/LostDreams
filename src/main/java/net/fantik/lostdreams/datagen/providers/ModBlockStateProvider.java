package net.fantik.lostdreams.datagen.providers;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, LostDreams.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Простые блоки
        simpleBlock(ModBlocks.NULL_STONE.get());
        simpleBlock(ModBlocks.SURREAL_BLUE_ROCK.get());
        simpleBlock(ModBlocks.SURREAL_RED_ROCK.get());
        simpleBlock(ModBlocks.SURREAL_PURPLE_ROCK.get());
        simpleBlock(ModBlocks.SURREAL_GREEN_ROCK.get());
        simpleBlock(ModBlocks.SURREAL_LIGHTBLUE_ROCK.get());
        simpleBlock(ModBlocks.SURREAL_YELLOW_ROCK.get());
        simpleBlock(ModBlocks.SURREAL_GLOWCRYSTAL.get());
        simpleBlock(ModBlocks.NULL_BRICKS.get());
        simpleBlock(ModBlocks.NULL_CRACKED_BRICKS.get());
        simpleBlock(ModBlocks.NULL_GROUND.get());
        simpleBlock(ModBlocks.FEATHER_BLOCK.get());
        simpleBlock(ModBlocks.PINK_KNOWLEDGE_BLOCK.get());
        simpleBlock(ModBlocks.BLUE_KNOWLEDGE_BLOCK.get());
        simpleBlock(ModBlocks.GREEN_KNOWLEDGE_BLOCK.get());

        // Duskwillow дерево
        logBlock(ModBlocks.DUSKWILLOW_LOG.get());
        logBlock(ModBlocks.STRIPPED_DUSKWILLOW_LOG.get());
        axisBlock(ModBlocks.DUSKWILLOW_WOOD.get(),
                modLoc("block/duskwillow_log"),
                modLoc("block/duskwillow_log"));
        axisBlock(ModBlocks.STRIPPED_DUSKWILLOW_WOOD.get(),
                modLoc("block/stripped_duskwillow_log"),
                modLoc("block/stripped_duskwillow_log"));


        simpleBlock(ModBlocks.DUSKWILLOW_PLANKS.get());
        simpleBlock(ModBlocks.DUSKWILLOW_LEAVES.get());

        // Саженец
        simpleBlock(ModBlocks.DUSKWILLOW_SAPLING.get(),
                models().cross("duskwillow_sapling",
                        blockTexture(ModBlocks.DUSKWILLOW_SAPLING.get())).renderType("cutout"));

        // Ступеньки
        stairsBlock(ModBlocks.DUSKWILLOW_STAIRS.get(),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()));

        // Плита
        slabBlock(ModBlocks.DUSKWILLOW_SLAB.get(),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()));

        // Забор
        fenceBlock(ModBlocks.DUSKWILLOW_FENCE.get(),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()));

        // Калитка
        fenceGateBlock(ModBlocks.DUSKWILLOW_FENCE_GATE.get(),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()));

        // Дверь
        doorBlockWithRenderType(ModBlocks.DUSKWILLOW_DOOR.get(),
                modLoc("block/duskwillow_door_bottom"),
                modLoc("block/duskwillow_door_top"), "cutout");

        // Люк
        trapdoorBlockWithRenderType(ModBlocks.DUSKWILLOW_TRAPDOOR.get(),
                modLoc("block/duskwillow_trapdoor"), true, "cutout");

        // Кнопка
        buttonBlock(ModBlocks.DUSKWILLOW_BUTTON.get(),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()));

        // Плита давления
        pressurePlateBlock(ModBlocks.DUSKWILLOW_PRESSURE_PLATE.get(),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()));
    }
}