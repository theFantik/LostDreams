package net.fantik.lostdreams.world.dimension;

import net.fantik.lostdreams.LostDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class ApartmentsDimension {

    public static final ResourceKey<Level> APARTMENTS_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "gigachrushchevka")
    );

    public static boolean isApartments(LevelAccessor level) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            return serverLevel.dimension().equals(APARTMENTS_KEY);
        }
        if (level instanceof net.minecraft.client.multiplayer.ClientLevel clientLevel) {
            return clientLevel.dimension().equals(APARTMENTS_KEY);
        }
        if (level instanceof Level l) {
            return l.dimension().equals(APARTMENTS_KEY);
        }
        return false;
    }
}