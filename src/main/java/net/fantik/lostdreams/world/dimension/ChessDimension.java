package net.fantik.lostdreams.world.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ChessDimension {
    public static final ResourceKey<Level> CHESS_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.parse("lostdreams:chess_dimension")
    );


    public static boolean isChessDim(Level level) {
        return level != null && level.dimension().equals(CHESS_KEY);
    }

    /**
     * Получает ResourceLocation измерения.
     */
    public static ResourceLocation getDimensionId() {
        return CHESS_KEY.location();
    }
}
