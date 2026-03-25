package net.fantik.lostdreams.world;

import com.mojang.serialization.MapCodec;
import net.fantik.lostdreams.LostDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBiomeSources {
    public static final DeferredRegister<MapCodec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, LostDreams.MOD_ID);



    public static void register(IEventBus bus) {
        BIOME_SOURCES.register(bus);
    }
}
