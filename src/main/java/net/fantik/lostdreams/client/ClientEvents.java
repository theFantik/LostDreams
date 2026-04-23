package net.fantik.lostdreams.client;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.entity.ModBlockEntities;
import net.fantik.lostdreams.block.entity.ZirconCampfireBlockEntity;
import net.fantik.lostdreams.client.renderer.NullZoneDimensionEffects;
import net.fantik.lostdreams.client.renderer.SurrealAsteroidsDimensionEffects;
import net.fantik.lostdreams.client.renderer.ZirconCampfireRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

@EventBusSubscriber(
        modid = LostDreams.MOD_ID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD
)
public class ClientEvents {

    // Добавь в свой ClientEvents.java (или создай если нет)
// В метод который слушает EntityRenderersEvent.RegisterRenderers:

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.ZIRCON_CAMPFIRE_BE.get(),
                ZirconCampfireRenderer::new
        );
    }

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