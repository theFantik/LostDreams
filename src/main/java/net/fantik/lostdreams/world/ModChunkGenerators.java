package net.fantik.lostdreams.world;

// В вашем главном классе или DimensionRegistration.java

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import com.mojang.serialization.MapCodec;

public class ModChunkGenerators {

    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(BuiltInRegistries.CHUNK_GENERATOR, "lostdreams");

    public static final DeferredHolder<MapCodec<? extends ChunkGenerator>,
            MapCodec<ChessDimensionChunkGenerator>> CHESS_GENERATOR =
            CHUNK_GENERATORS.register("chess_generator",
                    () -> ChessDimensionChunkGenerator.CODEC);

    public static void register(net.neoforged.bus.api.IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);
    }
}

