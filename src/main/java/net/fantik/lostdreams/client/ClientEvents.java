package net.fantik.lostdreams.client;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.client.renderer.NullZoneDimensionEffects;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

/**
 * Клиентские события.
 *
 * Регистрирует:
 * - Эффекты измерения Null Zone
 */
@EventBusSubscriber(
        modid = LostDreams.MOD_ID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD
)
public class ClientEvents {



    /**
     * Регистрация эффектов измерения.
     *
     * ============================================
     * ВАЖНО: ID должен совпадать с JSON!
     * ============================================
     * В dimension_type JSON:
     *   "effects": "lostdreams:null_zone"
     *
     * В коде:
     *   ResourceLocation.parse("lostdreams:null_zone")
     */
    @SubscribeEvent
    public static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        // ID эффектов (как в JSON "effects": "lostdreams:null_zone")
        ResourceLocation effectsId = ResourceLocation.parse("lostdreams:null_zone_dim");

        event.register(effectsId, new NullZoneDimensionEffects());

        LostDreams.LOGGER.info("Registered Null Zone dimension effects: {}", effectsId);
    }
}