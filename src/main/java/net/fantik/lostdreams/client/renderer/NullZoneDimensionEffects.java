package net.fantik.lostdreams.client.renderer;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

/**
 * Эффекты измерения Null Zone.
 *
 * Регистрируется через ClientEvents.java
 *
 * Настройки:
 * - SkyType.NONE: нет солнца и луны (как в Энде)
 * - Тёмный туман
 * - Нет облаков
 */
public class NullZoneDimensionEffects extends DimensionSpecialEffects {

    public NullZoneDimensionEffects() {
        super(
                Float.NaN,      // cloudHeight - нет облаков
                false,          // hasGround
                SkyType.NONE,   // skyType - НЕТ солнца и луны!
                false,          // forceBrightness
                false           // constantAmbientLight
        );
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        // Очень тёмный туман (почти чёрный)
        return fogColor.multiply(0.01, 0.005, 0.02);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        // Всегда туман
        return true;
    }
}