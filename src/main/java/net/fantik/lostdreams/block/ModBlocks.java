package net.fantik.lostdreams.block;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.custom.*;
import net.fantik.lostdreams.item.ModItems;
import net.fantik.lostdreams.sound.ModSounds;
import net.fantik.lostdreams.world.tree.DuskwillowTreeGrower;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(LostDreams.MOD_ID);

    // порталы

    public static final DeferredBlock<PortalBlock> NULL_ZONE_PORTAL = registerBlock("null_zone_portal",
            () -> new PortalBlock(BlockBehaviour.Properties.of()
                    .noCollission()
                    .lightLevel(s -> 11)
                    .strength(-1f, 3600000f)
                    .noLootTable()));


    // функциональные блоки

    public static final DeferredBlock<DreamGeneratorBlock> DREAM_GENERATOR = registerBlock("dream_generator",
            () -> new DreamGeneratorBlock(BlockBehaviour.Properties.of()
                    .strength(3f)
                    .sound(SoundType.METAL)
                    .noOcclusion(), GeneratorTier.BASIC));

    public static final DeferredBlock<DreamGeneratorBlock> DREAM_ADVANCED_GENERATOR = registerBlock("dream_advanced_generator",
            () -> new DreamGeneratorBlock(BlockBehaviour.Properties.of()
                    .strength(3f)
                    .sound(SoundType.METAL)
                    .noOcclusion(), GeneratorTier.ADVANCED));
    public static final DeferredBlock<DreamGeneratorBlock> DREAM_ELITE_GENERATOR = registerBlock("dream_elite_generator",
            () -> new DreamGeneratorBlock(BlockBehaviour.Properties.of()
                    .strength(3f)
                    .sound(SoundType.METAL)
                    .noOcclusion(), GeneratorTier.ELITE));

    // -----------------------------------------------------------------------
    // Null Zone блоки
    // -----------------------------------------------------------------------
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

    public static final DeferredBlock<SlabBlock> NULL_STONE_SLAB = registerBlock("null_stone_slab", ()-> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_SLAB)));
    public static final DeferredBlock<StairBlock> NULL_STONE_STAIRS = registerBlock("null_stone_stairs",
            () -> new StairBlock(NULL_STONE.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_STAIRS)));

    public static final DeferredBlock<Block> NULL_BRICKS = registerBlock("null_bricks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3f).sound(SoundType.STONE).destroyTime(1.5f)
            ));

    public static final DeferredBlock<Block> NULL_CRACKED_BRICKS = registerBlock("null_cracked_bricks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(2f).sound(SoundType.STONE).destroyTime(1.5f)
            ));

    public static final DeferredBlock<Block> NULL_BLOSSOM = registerBlock("null_blossom",() -> new FlowerBlock(MobEffects.BLINDNESS, 5,BlockBehaviour.Properties.ofFullCopy(Blocks.RED_TULIP)));
    public static final DeferredBlock<Block> NULL_GRASS = registerBlock("null_grass",()-> new NullGrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS)));

    public static final DeferredBlock<NullBerryBushBlock> NULL_BERRY_BUSH =
            registerBlock("null_berry_bush",
                    () -> new NullBerryBushBlock(BlockBehaviour.Properties
                            .ofFullCopy(Blocks.SWEET_BERRY_BUSH)
                            .sound(SoundType.GRASS)
                            .noCollission()));

    // Замени registerBlock на прямую регистрацию
    public static final DeferredBlock<NullCropBlock> NULL_CROP =
            BLOCKS.register("null_crop",
                    () -> new NullCropBlock(BlockBehaviour.Properties.of()
                            .noCollission()
                            .randomTicks()
                            .instabreak()
                            .sound(SoundType.GRASS)
                            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)));


    public static final DeferredBlock<RotatedPillarBlock> NULL_LOG = registerBlock("null_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)) {
                @Override
                public net.minecraft.world.level.block.state.BlockState getToolModifiedState(
                        net.minecraft.world.level.block.state.BlockState state,
                        net.minecraft.world.item.context.UseOnContext context,
                        net.neoforged.neoforge.common.ItemAbility itemAbility,
                        boolean simulate) {
                    if (itemAbility == net.neoforged.neoforge.common.ItemAbilities.AXE_STRIP) {
                        return STRIPPED_NULL_LOG.get().defaultBlockState()
                                .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
                    }
                    return super.getToolModifiedState(state, context, itemAbility, simulate);
                }
            });

    public static final DeferredBlock<RotatedPillarBlock> NULL_WOOD = registerBlock("null_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)) {
                @Override
                public net.minecraft.world.level.block.state.BlockState getToolModifiedState(
                        net.minecraft.world.level.block.state.BlockState state,
                        net.minecraft.world.item.context.UseOnContext context,
                        net.neoforged.neoforge.common.ItemAbility itemAbility,
                        boolean simulate) {
                    if (itemAbility == net.neoforged.neoforge.common.ItemAbilities.AXE_STRIP) {
                        return STRIPPED_NULL_WOOD.get().defaultBlockState()
                                .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
                    }
                    return super.getToolModifiedState(state, context, itemAbility, simulate);
                }
            });

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_NULL_LOG = registerBlock("stripped_null_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_NULL_WOOD = registerBlock("stripped_null_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD)));





    public static final DeferredBlock<Block> NULL_PLANKS = registerBlock("null_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));

    public static final DeferredBlock<StairBlock> NULL_STAIRS = registerBlock("null_stairs",
            () -> new StairBlock(NULL_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)));

    public static final DeferredBlock<SlabBlock> NULL_SLAB = registerBlock("null_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)));

    public static final DeferredBlock<FenceBlock> NULL_FENCE = registerBlock("null_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)));

    public static final DeferredBlock<FenceGateBlock> NULL_FENCE_GATE = registerBlock("null_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)));

    public static final DeferredBlock<DoorBlock> NULL_DOOR = registerBlock("null_door",
            () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)));

    public static final DeferredBlock<TrapDoorBlock> NULL_TRAPDOOR = registerBlock("null_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)));

    public static final DeferredBlock<ButtonBlock> NULL_BUTTON = registerBlock("null_button",
            () -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON)));

    public static final DeferredBlock<PressurePlateBlock> NULL_PRESSURE_PLATE = registerBlock("null_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE)));



    // -----------------------------------------------------------------------
    // Knowledge блоки
    // -----------------------------------------------------------------------
    public static final DeferredBlock<Block> PINK_KNOWLEDGE_BLOCK = registerBlock("pink_knowledge_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)));
    public static final DeferredBlock<Block> BLUE_KNOWLEDGE_BLOCK = registerBlock("blue_knowledge_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)));
    public static final DeferredBlock<Block> GREEN_KNOWLEDGE_BLOCK = registerBlock("green_knowledge_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT)));

    // -----------------------------------------------------------------------
    // Duskwillow дерево
    // -----------------------------------------------------------------------
    public static final DeferredBlock<RotatedPillarBlock> DUSKWILLOW_LOG = registerBlock("duskwillow_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)) {
                @Override
                public net.minecraft.world.level.block.state.BlockState getToolModifiedState(
                        net.minecraft.world.level.block.state.BlockState state,
                        net.minecraft.world.item.context.UseOnContext context,
                        net.neoforged.neoforge.common.ItemAbility itemAbility,
                        boolean simulate) {
                    if (itemAbility == net.neoforged.neoforge.common.ItemAbilities.AXE_STRIP) {
                        return STRIPPED_DUSKWILLOW_LOG.get().defaultBlockState()
                                .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
                    }
                    return super.getToolModifiedState(state, context, itemAbility, simulate);
                }
            });

    public static final DeferredBlock<RotatedPillarBlock> DUSKWILLOW_WOOD = registerBlock("duskwillow_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)) {
                @Override
                public net.minecraft.world.level.block.state.BlockState getToolModifiedState(
                        net.minecraft.world.level.block.state.BlockState state,
                        net.minecraft.world.item.context.UseOnContext context,
                        net.neoforged.neoforge.common.ItemAbility itemAbility,
                        boolean simulate) {
                    if (itemAbility == net.neoforged.neoforge.common.ItemAbilities.AXE_STRIP) {
                        return STRIPPED_DUSKWILLOW_WOOD.get().defaultBlockState()
                                .setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
                    }
                    return super.getToolModifiedState(state, context, itemAbility, simulate);
                }
            });

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_DUSKWILLOW_LOG = registerBlock("stripped_duskwillow_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)));

    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_DUSKWILLOW_WOOD = registerBlock("stripped_duskwillow_wood",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD)));

    public static final DeferredBlock<LeavesBlock> DUSKWILLOW_LEAVES = registerBlock("duskwillow_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)));

    public static final DeferredBlock<SaplingBlock> DUSKWILLOW_SAPLING = registerBlock("duskwillow_sapling",
            () -> new SaplingBlock(DuskwillowTreeGrower.INSTANCE,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));

    public static final DeferredBlock<Block> DUSKWILLOW_PLANKS = registerBlock("duskwillow_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));

    public static final DeferredBlock<StairBlock> DUSKWILLOW_STAIRS = registerBlock("duskwillow_stairs",
            () -> new StairBlock(DUSKWILLOW_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)));

    public static final DeferredBlock<SlabBlock> DUSKWILLOW_SLAB = registerBlock("duskwillow_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)));

    public static final DeferredBlock<FenceBlock> DUSKWILLOW_FENCE = registerBlock("duskwillow_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)));

    public static final DeferredBlock<FenceGateBlock> DUSKWILLOW_FENCE_GATE = registerBlock("duskwillow_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)));

    public static final DeferredBlock<DoorBlock> DUSKWILLOW_DOOR = registerBlock("duskwillow_door",
            () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)));

    public static final DeferredBlock<TrapDoorBlock> DUSKWILLOW_TRAPDOOR = registerBlock("duskwillow_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)));

    public static final DeferredBlock<ButtonBlock> DUSKWILLOW_BUTTON = registerBlock("duskwillow_button",
            () -> new ButtonBlock(BlockSetType.OAK, 30, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON)));

    public static final DeferredBlock<PressurePlateBlock> DUSKWILLOW_PRESSURE_PLATE = registerBlock("duskwillow_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE)));



    // Сюр блоки

    public static final DeferredBlock<Block> SURREAL_BLUE_ROCK = registerBlock("surreal_blue_rock",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));

    public static final DeferredBlock<Block> SURREAL_RED_ROCK = registerBlock("surreal_red_rock",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));

    public static final DeferredBlock<Block> SURREAL_YELLOW_ROCK = registerBlock("surreal_yellow_rock",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));

    public static final DeferredBlock<Block> SURREAL_PURPLE_ROCK = registerBlock("surreal_purple_rock",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));

    public static final DeferredBlock<Block> SURREAL_GREEN_ROCK = registerBlock("surreal_green_rock",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));

    public static final DeferredBlock<Block> SURREAL_LIGHTBLUE_ROCK = registerBlock("surreal_lightblue_rock",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));

    public static final DeferredBlock<Block> SURREAL_GLOWCRYSTAL = registerBlock("surreal_glowcrystal",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(0.5f)
                    .lightLevel(state -> 15)
                    .sound(ModSounds.getglowcSoundType())
                    .noOcclusion()));

    // Randomite руды
    public static final DeferredBlock<Block> SURREAL_BLUE_RANDOMITE_ORE = registerBlock("surreal_blue_randomite_ore",
            () -> new XpDropsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<Block> SURREAL_LIGHTBLUE_RANDOMITE_ORE = registerBlock("surreal_lightblue_randomite_ore",
            () -> new XpDropsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<Block> SURREAL_GREEN_RANDOMITE_ORE = registerBlock("surreal_green_randomite_ore",
            () -> new XpDropsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<Block> SURREAL_PURPLE_RANDOMITE_ORE = registerBlock("surreal_purple_randomite_ore",
            () -> new XpDropsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<Block> SURREAL_RED_RANDOMITE_ORE = registerBlock("surreal_red_randomite_ore",
            () -> new XpDropsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredBlock<Block> SURREAL_YELLOW_RANDOMITE_ORE = registerBlock("surreal_yellow_randomite_ore",
            () -> new XpDropsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));

    // -----------------------------------------------------------------------
    // Регистрация
    // -----------------------------------------------------------------------
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }



    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}