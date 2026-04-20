package net.fantik.lostdreams.datagen.providers;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.block.PortalBlock;
import net.fantik.lostdreams.block.custom.NullBerryBushBlock;
import net.fantik.lostdreams.block.custom.NullCropBlock;
import net.minecraft.core.Direction;
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
        simpleBlock(ModBlocks.SURREAL_BLUE_RANDOMITE_ORE.get());
        simpleBlock(ModBlocks.SURREAL_LIGHTBLUE_RANDOMITE_ORE.get());
        simpleBlock(ModBlocks.SURREAL_GREEN_RANDOMITE_ORE.get());
        simpleBlock(ModBlocks.SURREAL_PURPLE_RANDOMITE_ORE.get());
        simpleBlock(ModBlocks.SURREAL_RED_RANDOMITE_ORE.get());
        simpleBlock(ModBlocks.SURREAL_YELLOW_RANDOMITE_ORE.get());

        horizontalBlock(ModBlocks.DREAM_GENERATOR.get(),
                models().orientableWithBottom(
                        "dream_generator",
                        modLoc("block/dream_generator_side"),  // сторона
                        modLoc("block/dream_generator_front"), // перед (без ресурсов)
                        modLoc("block/dream_generator_bottom"), // низ
                        modLoc("block/dream_generator_top")    // верх
                ).texture("particle", modLoc("block/dream_generator_side"))
        );
        horizontalBlock(ModBlocks.DREAM_ADVANCED_GENERATOR.get(),
                models().orientableWithBottom(
                        "dream_advanced_generator",
                        modLoc("block/dream_advanced_generator_side"),  // сторона
                        modLoc("block/dream_advanced_generator_front"), // перед (без ресурсов)
                        modLoc("block/dream_advanced_generator_bottom"), // низ
                        modLoc("block/dream_advanced_generator_top")    // верх
                ).texture("particle", modLoc("block/dream_advanced_generator_side"))
        );
        horizontalBlock(ModBlocks.DREAM_ELITE_GENERATOR.get(),
                models().orientableWithBottom(
                        "dream_elite_generator",
                        modLoc("block/dream_elite_generator_side"),  // сторона
                        modLoc("block/dream_elite_generator_front"), // перед (без ресурсов)
                        modLoc("block/dream_elite_generator_bottom"), // низ
                        modLoc("block/dream_elite_generator_top")    // верх
                ).texture("particle", modLoc("block/dream_elite_generator_side"))
        );



        //  дерево
        logBlock(ModBlocks.DUSKWILLOW_LOG.get());
        logBlock(ModBlocks.NULL_LOG.get());
        logBlock(ModBlocks.STRIPPED_NULL_LOG.get());
        logBlock(ModBlocks.STRIPPED_DUSKWILLOW_LOG.get());
        axisBlock(ModBlocks.DUSKWILLOW_WOOD.get(),
                modLoc("block/duskwillow_log"),
                modLoc("block/duskwillow_log"));
        axisBlock(ModBlocks.STRIPPED_DUSKWILLOW_WOOD.get(),
                modLoc("block/stripped_duskwillow_log"),
                modLoc("block/stripped_duskwillow_log"));
        axisBlock(ModBlocks.NULL_WOOD.get(),
                modLoc("block/null_log"),
                modLoc("block/null_log"));
        axisBlock(ModBlocks.STRIPPED_NULL_WOOD.get(),
                modLoc("block/stripped_null_log"),
                modLoc("block/stripped_null_log"));


        simpleBlock(ModBlocks.DUSKWILLOW_PLANKS.get());
        simpleBlock(ModBlocks.NULL_PLANKS.get());
        simpleBlock(ModBlocks.DUSKWILLOW_LEAVES.get());

        simpleBlock(ModBlocks.NULL_BLOSSOM.get(),
                models().cross("null_blossom",
                        blockTexture(ModBlocks.NULL_BLOSSOM.get())).renderType("cutout"));

        simpleBlock(ModBlocks.NULL_GRASS.get(),
                models().cross("null_grass",
                        blockTexture(ModBlocks.NULL_GRASS.get())).renderType("cutout"));



        // Саженец
        simpleBlock(ModBlocks.DUSKWILLOW_SAPLING.get(),
                models().cross("duskwillow_sapling",
                        blockTexture(ModBlocks.DUSKWILLOW_SAPLING.get())).renderType("cutout"));

        // Ступеньки
        stairsBlock(ModBlocks.DUSKWILLOW_STAIRS.get(),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()));
        stairsBlock(ModBlocks.NULL_STAIRS.get(),
                blockTexture(ModBlocks.NULL_PLANKS.get()));
        stairsBlock(ModBlocks.NULL_STONE_STAIRS.get(),
                blockTexture(ModBlocks.NULL_STONE.get()));

        // Плита
        slabBlock(ModBlocks.DUSKWILLOW_SLAB.get(),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()));
        slabBlock(ModBlocks.NULL_SLAB.get(),
                blockTexture(ModBlocks.NULL_PLANKS.get()),
                blockTexture(ModBlocks.NULL_PLANKS.get()));
        slabBlock(ModBlocks.NULL_STONE_SLAB.get(),
                blockTexture(ModBlocks.NULL_STONE.get()),
                blockTexture(ModBlocks.NULL_STONE.get()));


        // Забор
        fenceBlock(ModBlocks.DUSKWILLOW_FENCE.get(),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()));
        fenceBlock(ModBlocks.NULL_FENCE.get(),
                blockTexture(ModBlocks.NULL_PLANKS.get()));

        // Калитка
        fenceGateBlock(ModBlocks.DUSKWILLOW_FENCE_GATE.get(),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()));
        fenceGateBlock(ModBlocks.NULL_FENCE_GATE.get(),
                blockTexture(ModBlocks.NULL_PLANKS.get()));

        // Дверь
        doorBlockWithRenderType(ModBlocks.DUSKWILLOW_DOOR.get(),
                modLoc("block/duskwillow_door_bottom"),
                modLoc("block/duskwillow_door_top"), "cutout");
        doorBlockWithRenderType(ModBlocks.NULL_DOOR.get(),
                modLoc("block/null_door_bottom"),
                modLoc("block/null_door_top"), "cutout");

        // Люк
        trapdoorBlockWithRenderType(ModBlocks.DUSKWILLOW_TRAPDOOR.get(),
                modLoc("block/duskwillow_trapdoor"), true, "cutout");
        trapdoorBlockWithRenderType(ModBlocks.NULL_TRAPDOOR.get(),
                modLoc("block/null_trapdoor"), true, "cutout");

        // Кнопка
        buttonBlock(ModBlocks.DUSKWILLOW_BUTTON.get(),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()));
        buttonBlock(ModBlocks.NULL_BUTTON.get(),
                blockTexture(ModBlocks.NULL_PLANKS.get()));

        // Плита давления
        pressurePlateBlock(ModBlocks.DUSKWILLOW_PRESSURE_PLATE.get(),
                blockTexture(ModBlocks.DUSKWILLOW_PLANKS.get()));
        pressurePlateBlock(ModBlocks.NULL_PRESSURE_PLATE.get(),
                blockTexture(ModBlocks.NULL_PLANKS.get()));

        getVariantBuilder(ModBlocks.NULL_BERRY_BUSH.get())
                .partialState().with(NullBerryBushBlock.AGE, 0)
                .modelForState().modelFile(
                        models().cross("null_berry_bush_stage0",
                                        modLoc("block/null_berry_bush_stage0"))
                                .renderType("cutout")).addModel()
                .partialState().with(NullBerryBushBlock.AGE, 1)
                .modelForState().modelFile(
                        models().cross("null_berry_bush_stage1",
                                        modLoc("block/null_berry_bush_stage1"))
                                .renderType("cutout")).addModel()
                .partialState().with(NullBerryBushBlock.AGE, 2)
                .modelForState().modelFile(
                        models().cross("null_berry_bush_stage2",
                                        modLoc("block/null_berry_bush_stage2"))
                                .renderType("cutout")).addModel()
                .partialState().with(NullBerryBushBlock.AGE, 3)
                .modelForState().modelFile(
                        models().cross("null_berry_bush_stage3",
                                        modLoc("block/null_berry_bush_stage3"))
                                .renderType("cutout")).addModel();

        getVariantBuilder(ModBlocks.NULL_CROP.get())
                .partialState().with(NullCropBlock.AGE, 0)
                .modelForState().modelFile(
                        models().cross("null_crop_stage0",
                                        modLoc("block/null_crop_stage0"))
                                .renderType("cutout")).addModel()
                .partialState().with(NullCropBlock.AGE, 1)
                .modelForState().modelFile(
                        models().cross("null_crop_stage1",
                                        modLoc("block/null_crop_stage1"))
                                .renderType("cutout")).addModel()
                .partialState().with(NullCropBlock.AGE, 2)
                .modelForState().modelFile(
                        models().cross("null_crop_stage2",
                                        modLoc("block/null_crop_stage2"))
                                .renderType("cutout")).addModel()
                .partialState().with(NullCropBlock.AGE, 3)
                .modelForState().modelFile(
                        models().cross("null_crop_stage3",
                                        modLoc("block/null_crop_stage3"))
                                .renderType("cutout")).addModel();

        // Для каждого портал-блока
        getVariantBuilder(ModBlocks.NULL_ZONE_PORTAL.get())
                .partialState().with(PortalBlock.AXIS, Direction.Axis.X)
                .modelForState().modelFile(models().getExistingFile(
                        modLoc("block/null_zone_portal_x"))).addModel()
                .partialState().with(PortalBlock.AXIS, Direction.Axis.Z)
                .modelForState().modelFile(models().getExistingFile(
                        modLoc("block/null_zone_portal_z"))).addModel();


    }
}