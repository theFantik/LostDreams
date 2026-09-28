package net.fantik.lostdreams.client.renderer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ChessDimensionEffects extends DimensionSpecialEffects {

    public ChessDimensionEffects() {
        super(
                192,
                true,
                SkyType.NORMAL,
                false,
                false

        );
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        // Если хочешь, чтобы туман вообще не окрашивал мир,
        // возвращай fogColor без изменений или слегка приглушённым.
        // Сейчас мы сделаем его нейтральным, чтобы он не "давил" на зрение.
        return new Vec3(
                0.025D * brightness,
                0.012D * brightness,
                0.055D * brightness
        );
    }

    @Override
    public boolean isFoggyAt(int x, int z) {

        return false;
    }


}
