package net.fantik.lostdreams.world.tree;

import net.fantik.lostdreams.LostDreams;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.Optional;

public class DuskwillowTreeGrower {
    public static final TreeGrower INSTANCE = new TreeGrower(
            "duskwillow",
            Optional.empty(),
            Optional.of(ResourceKey.create(
                    Registries.CONFIGURED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "duskwillow_tree")
            )),
            Optional.empty()
    );
}