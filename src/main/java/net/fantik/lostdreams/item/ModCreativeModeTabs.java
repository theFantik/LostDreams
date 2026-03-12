package net.fantik.lostdreams.item;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LostDreams.MOD_ID);

    public static final Supplier<CreativeModeTab> BED_ITEMS_TAB = CREATIVE_MODE_TAB.register("bed_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.PILLOW.get()))
                    .title(Component.translatable("creativetab.lostdreams.bed_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.PILLOW);
                        output.accept(ModItems.DREAM_CATCHER);
                    }).build());

    public static final Supplier<CreativeModeTab> DREAM_BLOCK_TAB = CREATIVE_MODE_TAB.register("dream_blocks_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.FEATHER_BLOCK))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "bed_items_tab"))
                    .title(Component.translatable("creativetab.lostdreams.dream_blocks"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.FEATHER_BLOCK);
                        output.accept(ModBlocks.NULL_GROUND);
                        output.accept(ModBlocks.NULL_BRICKS);
                        output.accept(ModBlocks.NULL_CRACKED_BRICKS);
                        output.accept(ModBlocks.NULL_STONE);
                    }).build());

    public static final Supplier<CreativeModeTab> DREAM_MOBS_TAB = CREATIVE_MODE_TAB.register("dream_mobs_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.BUG_ANTENNA.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "dream_blocks_tab"))
                    .title(Component.translatable("creativetab.lostdreams.dream_mobs"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.BUG_ANTENNA);
                        output.accept(ModItems.NULL_BUG_SPAWN_EGG);
                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}