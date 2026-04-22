package net.fantik.lostdreams.datagen.providers;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.item.ModItems;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.ibm.icu.impl.CurrencyData.provider;

public class ModLootTableProvider extends LootTableProvider {

    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK)
        ), lookupProvider);
    }

    public static class ModBlockLoot extends BlockLootSubProvider {

        protected ModBlockLoot(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
        }

        @Override
        protected void generate() {
            // Простые блоки — дропают сами себя
            dropSelf(ModBlocks.NULL_STONE.get());
            dropSelf(ModBlocks.NULL_BLOSSOM.get());
            dropSelf(ModBlocks.NULL_BRICKS.get());
            dropSelf(ModBlocks.NULL_CRACKED_BRICKS.get());
            dropSelf(ModBlocks.NULL_GROUND.get());
            dropSelf(ModBlocks.FEATHER_BLOCK.get());
            dropSelf(ModBlocks.PINK_KNOWLEDGE_BLOCK.get());
            dropSelf(ModBlocks.BLUE_KNOWLEDGE_BLOCK.get());
            dropSelf(ModBlocks.GREEN_KNOWLEDGE_BLOCK.get());
            dropSelf(ModBlocks.SURREAL_BLUE_ROCK.get());
            dropSelf(ModBlocks.SURREAL_RED_ROCK.get());
            dropSelf(ModBlocks.SURREAL_PURPLE_ROCK.get());
            dropSelf(ModBlocks.SURREAL_LIGHTBLUE_ROCK.get());
            dropSelf(ModBlocks.SURREAL_GREEN_ROCK.get());
            dropSelf(ModBlocks.SURREAL_YELLOW_ROCK.get());
            dropSelf(ModBlocks.SURREAL_GLOWCRYSTAL.get());
            dropSelf(ModBlocks.DREAM_GENERATOR.get());
            dropSelf(ModBlocks.DREAM_ADVANCED_GENERATOR.get());
            dropSelf(ModBlocks.DREAM_ELITE_GENERATOR.get());

            //  дерево
            dropSelf(ModBlocks.DUSKWILLOW_LOG.get());
            dropSelf(ModBlocks.DUSKWILLOW_WOOD.get());
            dropSelf(ModBlocks.STRIPPED_DUSKWILLOW_LOG.get());
            dropSelf(ModBlocks.STRIPPED_DUSKWILLOW_WOOD.get());
            dropSelf(ModBlocks.DUSKWILLOW_PLANKS.get());
            dropSelf(ModBlocks.DUSKWILLOW_STAIRS.get());
            dropSelf(ModBlocks.DUSKWILLOW_FENCE.get());
            dropSelf(ModBlocks.DUSKWILLOW_FENCE_GATE.get());
            dropSelf(ModBlocks.DUSKWILLOW_BUTTON.get());
            dropSelf(ModBlocks.DUSKWILLOW_PRESSURE_PLATE.get());
            dropSelf(ModBlocks.DUSKWILLOW_TRAPDOOR.get());
            dropSelf(ModBlocks.DUSKWILLOW_SAPLING.get());
            dropSelf(ModBlocks.NULL_LOG.get());
            dropSelf(ModBlocks.NULL_WOOD.get());
            dropSelf(ModBlocks.STRIPPED_NULL_LOG.get());
            dropSelf(ModBlocks.STRIPPED_NULL_WOOD.get());
            dropSelf(ModBlocks.NULL_PLANKS.get());
            dropSelf(ModBlocks.NULL_STAIRS.get());
            dropSelf(ModBlocks.NULL_STONE_STAIRS.get());
            dropSelf(ModBlocks.NULL_FENCE.get());
            dropSelf(ModBlocks.NULL_FENCE_GATE.get());
            dropSelf(ModBlocks.NULL_BUTTON.get());
            dropSelf(ModBlocks.NULL_PRESSURE_PLATE.get());
            dropSelf(ModBlocks.NULL_TRAPDOOR.get());



            // Randomite руды — случайные дропы
            add(ModBlocks.SURREAL_BLUE_RANDOMITE_ORE.get(), createRandomiteDrops(ModBlocks.SURREAL_BLUE_RANDOMITE_ORE.get()));
            add(ModBlocks.SURREAL_LIGHTBLUE_RANDOMITE_ORE.get(), createRandomiteDrops(ModBlocks.SURREAL_LIGHTBLUE_RANDOMITE_ORE.get()));
            add(ModBlocks.SURREAL_GREEN_RANDOMITE_ORE.get(), createRandomiteDrops(ModBlocks.SURREAL_GREEN_RANDOMITE_ORE.get()));
            add(ModBlocks.SURREAL_PURPLE_RANDOMITE_ORE.get(), createRandomiteDrops(ModBlocks.SURREAL_PURPLE_RANDOMITE_ORE.get()));
            add(ModBlocks.SURREAL_RED_RANDOMITE_ORE.get(), createRandomiteDrops(ModBlocks.SURREAL_RED_RANDOMITE_ORE.get()));
            add(ModBlocks.SURREAL_YELLOW_RANDOMITE_ORE.get(), createRandomiteDrops(ModBlocks.SURREAL_YELLOW_RANDOMITE_ORE.get()));

            // Плита — особый случай
            add(ModBlocks.DUSKWILLOW_SLAB.get(), createSlabItemTable(ModBlocks.DUSKWILLOW_SLAB.get()));
            add(ModBlocks.NULL_SLAB.get(), createSlabItemTable(ModBlocks.NULL_SLAB.get()));
            add(ModBlocks.NULL_STONE_SLAB.get(), createSlabItemTable(ModBlocks.NULL_STONE_SLAB.get()));

            // Дверь — дропает только нижняя часть
            add(ModBlocks.DUSKWILLOW_DOOR.get(), createDoorTable(ModBlocks.DUSKWILLOW_DOOR.get()));
            add(ModBlocks.NULL_DOOR.get(), createDoorTable(ModBlocks.NULL_DOOR.get()));

            // Листья — саженец, палки, яблоки
            add(ModBlocks.DUSKWILLOW_LEAVES.get(), createLeavesDrops(
                    ModBlocks.DUSKWILLOW_LEAVES.get(),
                    ModBlocks.DUSKWILLOW_SAPLING.get(),
                    NORMAL_LEAVES_SAPLING_CHANCES));

            add(ModBlocks.NULL_GRASS.get(), noDrop());
            add(ModBlocks.NULL_BERRY_BUSH.get(), noDrop());
            add(ModBlocks.NULL_CROP.get(), noDrop());
            add(ModBlocks.ZIRCON_TORCH.get(), noDrop());
            add(ModBlocks.ZIRCON_WALL_TORCH.get(), noDrop());

            add(ModBlocks.ZIRCON_ORE.get(), block ->
                    createOreDrop(block, ModItems.ZIRCON.get()));

        }

        protected LootTable.Builder createRandomiteDrops(Block block)
        {
            // Fortune как Holder (1.21 система)
            Holder<Enchantment> fortune = this.registries
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.FORTUNE);

            // 🎲 Случайные предметы (ТОЛЬКО если нет Silk Touch)
            LootPool.Builder randomPool = LootPool.lootPool()
                    .when(this.doesNotHaveSilkTouch())
                    .setRolls(ConstantValue.exactly(1))

                    .add(LootItem.lootTableItem(Items.ANCIENT_DEBRIS).setWeight(1))
                    .add(LootItem.lootTableItem(Items.RAW_COPPER).setWeight(12))
                    .add(LootItem.lootTableItem(Items.RAW_IRON).setWeight(12))
                    .add(LootItem.lootTableItem(Items.RAW_GOLD).setWeight(10))
                    .add(LootItem.lootTableItem(Items.REDSTONE).setWeight(10))
                    .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(6))
                    .add(LootItem.lootTableItem(Items.LAPIS_LAZULI).setWeight(8))
                    .add(LootItem.lootTableItem(Items.EMERALD).setWeight(5))
                    .add(LootItem.lootTableItem(Items.AMETHYST_SHARD).setWeight(8))

                    // базовое количество
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))

                    // Fortune увеличивает количество
                    .apply(ApplyBonusCount.addOreBonusCount(fortune));

            // 🟣 Silk Touch таблица:
            // silk → дроп блока
            // no silk → работает randomPool
            return createSilkTouchDispatchTable(block,
                    LootItem.lootTableItem(Items.AIR)
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(0)))
            ).withPool(randomPool);
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream()
                    .map(e -> (Block) e.get())
                    ::iterator;
        }
    }
}