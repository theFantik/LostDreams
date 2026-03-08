package net.fantik.lostdreams.entity;

import net.fantik.lostdreams.LostDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, LostDreams.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<NullBugEntity>> NULL_BUG =
            ENTITY_TYPES.register("null_bug", () ->
                    EntityType.Builder.<NullBugEntity>of(NullBugEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 0.5F)   // ширина, высота хитбокса
                            .build(ResourceLocation.parse("lostdreams:null_bug").toString())
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}