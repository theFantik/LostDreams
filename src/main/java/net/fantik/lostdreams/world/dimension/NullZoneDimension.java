package net.fantik.lostdreams.world.dimension;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Класс для хранения ResourceKey измерения Null Zone.
 *
 * Поскольку измерение создано через JSON (data-driven подход),
 * нам нужен только ResourceKey для привязки Skybox и других клиентских эффектов.
 *
 * ResourceKey должен совпадать с ID в JSON файле измерения:
 * data/lostdreams/dimension/null_zone.json
 */
public class NullZoneDimension {

    /**
     * ResourceKey для измерения Null Zone.
     * Используется для:
     * - Привязки Skybox (RegisterDimensionSpecialEffectsEvent)
     * - Проверки текущего измерения игрока
     * - Телепортации в измерение
     *
     * ВАЖНО: Должен совпадать с ID в JSON файле!
     * Если ваш JSON файл: data/lostdreams/dimension/null_zone.json
     * То ResourceKey: "lostdreams:null_zone"
     */
    public static final ResourceKey<Level> NULL_ZONE_KEY = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.parse("lostdreams:null_zone_dim")
    );

    /**
     * Проверяет, находится ли игрок в измерении Null Zone.
     * Полезно для клиентских эффектов.
     *
     * Пример использования:
     * if (NullZoneDimension.isInNullZone(player)) {
     *     // Специальные эффекты
     * }
     */
    public static boolean isNullZone(Level level) {
        return level != null && level.dimension().equals(NULL_ZONE_KEY);
    }

    /**
     * Получает ResourceLocation измерения.
     */
    public static ResourceLocation getDimensionId() {
        return NULL_ZONE_KEY.location();
    }
}