package net.fantik.lostdreams.block.entity;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.block.custom.DreamGeneratorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LostDreams.MOD_ID);

    public static final Supplier<BlockEntityType<DreamGeneratorBlockEntity>> DREAM_GENERATOR =
            BLOCK_ENTITIES.register("dream_generator",
                    () -> BlockEntityType.Builder.of(DreamGeneratorBlockEntity::new,
                            ModBlocks.DREAM_GENERATOR.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}