package net.fantik.lostdreams.client;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.client.renderer.NullZoneDimensionEffects;
import net.fantik.lostdreams.client.renderer.SurrealAsteroidsDimensionEffects;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

@EventBusSubscriber(
        modid = LostDreams.MOD_ID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD
)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(
                ResourceLocation.parse("lostdreams:null_zone_dim"),
                new NullZoneDimensionEffects()
        );
        LostDreams.LOGGER.info("Registered Null Zone dimension effects: lostdreams:null_zone_dim");

        event.register(
                ResourceLocation.parse("lostdreams:surreal_asteroids"),
                new SurrealAsteroidsDimensionEffects()
        );
        LostDreams.LOGGER.info("Registered Surreal Asteroids dimension effects: lostdreams:surreal_asteroids");
    }
}