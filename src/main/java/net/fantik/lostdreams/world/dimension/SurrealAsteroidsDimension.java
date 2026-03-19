package net.fantik.lostdreams.world.dimension;

import net.fantik.lostdreams.LostDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class SurrealAsteroidsDimension {

    public static final ResourceKey<Level> SURREAL_ASTEROIDS_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "surreal_asteroids_dim")
    );

    public static boolean isSurrealAsteroids(net.minecraft.world.level.LevelAccessor level) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return serverLevel.dimension().equals(SURREAL_ASTEROIDS_KEY);
        }
        return false;
    }
}