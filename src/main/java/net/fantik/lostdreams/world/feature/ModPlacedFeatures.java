package net.fantik.lostdreams.world.feature;

import net.fantik.lostdreams.LostDreams;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class ModPlacedFeatures {

    public static final ResourceKey<PlacedFeature> NULL_CAVE_SPAWNER_PLACED =
            ResourceKey.create(
                    Registries.PLACED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "null_cave_spawner_placed")
            );

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        Holder<ConfiguredFeature<?, ?>> nullCaveSpawner =
                configuredFeatures.getOrThrow(ModConfiguredFeatures.NULL_CAVE_SPAWNER);

        context.register(NULL_CAVE_SPAWNER_PLACED,
                new PlacedFeature(nullCaveSpawner, List.of(
                        // Количество попыток на чанк
                        CountPlacement.of(1),
                        RarityFilter.onAverageOnceEvery(5),
                        // Случайное смещение
                        InSquarePlacement.spread(),
                        // Высота размещения
                        HeightRangePlacement.uniform(
                                net.minecraft.world.level.levelgen.VerticalAnchor.absolute(-44),
                                net.minecraft.world.level.levelgen.VerticalAnchor.absolute(30)
                        ),
                        // Фильтр по биому
                        BiomeFilter.biome()
                ))
        );
    }
}
