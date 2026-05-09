package net.fantik.lostdreams.world.feature;

import net.fantik.lostdreams.LostDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> NULL_CAVE_SPAWNER =
            ResourceKey.create(
                    Registries.CONFIGURED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "null_cave_spawner")
            );

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(NULL_CAVE_SPAWNER,
                new ConfiguredFeature<>(
                        ModFeatures.NULL_CAVE_SPAWNER.get(),
                        NoneFeatureConfiguration.INSTANCE
                )
        );
    }
}
