package net.fantik.lostdreams.datagen.providers;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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

            // Duskwillow дерево
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



            // Плита — особый случай
            add(ModBlocks.DUSKWILLOW_SLAB.get(), createSlabItemTable(ModBlocks.DUSKWILLOW_SLAB.get()));

            // Дверь — дропает только нижняя часть
            add(ModBlocks.DUSKWILLOW_DOOR.get(), createDoorTable(ModBlocks.DUSKWILLOW_DOOR.get()));

            // Листья — саженец, палки, яблоки
            add(ModBlocks.DUSKWILLOW_LEAVES.get(), createLeavesDrops(
                    ModBlocks.DUSKWILLOW_LEAVES.get(),
                    ModBlocks.DUSKWILLOW_SAPLING.get(),
                    NORMAL_LEAVES_SAPLING_CHANCES));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ModBlocks.BLOCKS.getEntries().stream()
                    .map(e -> (Block) e.get())
                    ::iterator;
        }
    }
}