package net.fantik.lostdreams.client.renderer;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

public class NullZoneDimensionEffects extends DimensionSpecialEffects {

    public NullZoneDimensionEffects() {
        super(
                Float.NaN,      // cloudHeight - нет облаков
                false,          // hasGround
                SkyType.NONE,   // skyType - небо рисуем сами
                false,          // forceBrightness
                false           // constantAmbientLight
        );
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        // Если хочешь, чтобы туман вообще не окрашивал мир,
        // возвращай fogColor без изменений или слегка приглушённым.
        // Сейчас мы сделаем его нейтральным, чтобы он не "давил" на зрение.
        return fogColor.multiply(1.0, 1.0, 1.0);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        // ГЛАВНОЕ ИЗМЕНЕНИЕ:
        // Ставим false. Если здесь true, Minecraft включает "густой" режим тумана
        // (как в Незере или во время ливня), который обрезает видимость.
        return false;
    }
}