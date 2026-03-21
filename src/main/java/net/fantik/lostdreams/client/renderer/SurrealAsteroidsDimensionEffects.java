package net.fantik.lostdreams.client.renderer;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class SurrealAsteroidsDimensionEffects extends DimensionSpecialEffects {

    public SurrealAsteroidsDimensionEffects() {
        super(
                Float.NaN,
                false,
                SkyType.NONE,
                false,
                false
        );
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        return fogColor.multiply(0.0, 0.0, 0.0);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    @Nullable
    public float[] getSunriseColor(float skyAngle, float partialTicks) {
        return null;
    }
}