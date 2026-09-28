package net.fantik.lostdreams.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.block.ModWoodTypes;
import net.fantik.lostdreams.block.entity.ModBlockEntities;
import net.fantik.lostdreams.block.entity.ZirconCampfireBlockEntity;
import net.fantik.lostdreams.client.renderer.*;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

@EventBusSubscriber(
        modid = LostDreams.MOD_ID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD
)
public class ClientEvents {

    // Добавь в свой ClientEvents.java (или создай если нет)
// В метод который слушает EntityRenderersEvent.RegisterRenderers:

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Sheets.addWoodType(ModWoodTypes.NULL_SIGN_WOOD_TYPE);
            Sheets.addWoodType(ModWoodTypes.NULL_HANGING_SIGN_WOOD_TYPE);
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.ZIRCON_CAMPFIRE_BE.get(),
                ZirconCampfireRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.TEST_PORTAL_BE.get(),
                TestPortalRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) {
        try {
            ShaderInstance shader = new ShaderInstance(
                    event.getResourceProvider(),
                    ResourceLocation.fromNamespaceAndPath(
                            LostDreams.MOD_ID,
                            "test_portal"
                    ),
                    DefaultVertexFormat.POSITION_TEX
            );

            TestPortalShader.SHADER = shader;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load TEST_PORTAL shader",
                    e
            );
        }
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

        event.register(
                ResourceLocation.parse("lostdreams:chess_dimension"),
                new ChessDimensionEffects()
        );
        LostDreams.LOGGER.info("Registered Chess dimension effects: lostdreams:chess_dimension");
    }
}