package net.fantik.lostdreams.world.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;


public class SkyBlockDimension {


    public static final ResourceKey<Level> SKYBLOCK_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.parse("lostdreams:skyblock_dim")
    );


    public static boolean isSkyBlock(Level level) {
        return level != null && level.dimension().equals(SKYBLOCK_KEY);
    }

    /**
     * Получает ResourceLocation измерения.
     */
    public static ResourceLocation getDimensionId() {
        return SKYBLOCK_KEY.location();
    }
}