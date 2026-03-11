package net.fantik.lostdreams.world.feature;

import net.fantik.lostdreams.LostDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, LostDreams.MOD_ID);

    public static final DeferredHolder<Feature<?>, VoidHoleFeature> VOID_HOLE =
            FEATURES.register("void_hole", () -> new VoidHoleFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, DreamBuildingFeature> DREAM_BUILDING =
            FEATURES.register("dream_building", () -> new DreamBuildingFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}