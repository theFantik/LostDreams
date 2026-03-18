package net.fantik.lostdreams.datagen;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.datagen.providers.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Клиентские данные
        ModBlockStateProvider blockStates = new ModBlockStateProvider(output, event.getExistingFileHelper());
        generator.addProvider(event.includeClient(), blockStates);
        generator.addProvider(event.includeClient(),
                new ModItemModelProvider(output, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(),
                new ModLanguageProvider(output));

        // Серверные данные
        generator.addProvider(event.includeServer(),
                new ModRecipeProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(),
                new ModLootTableProvider(output, lookupProvider));

        ModBlockTagProvider blockTags = new ModBlockTagProvider(output, lookupProvider, event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(),
                new ModItemTagProvider(output, lookupProvider, blockTags, event.getExistingFileHelper()));
    }
}