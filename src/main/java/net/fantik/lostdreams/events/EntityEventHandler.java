package net.fantik.lostdreams.events;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.client.model.LucidWispModel;
import net.fantik.lostdreams.client.model.MeteorModel;
import net.fantik.lostdreams.client.model.MeteorWatcherModel;
import net.fantik.lostdreams.client.model.NullBugModel;
import net.fantik.lostdreams.client.renderer.*;
import net.fantik.lostdreams.entity.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class EntityEventHandler {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.NULL_BUG.get(), NullBugEntity.createAttributes());
        event.put(ModEntities.LUCID_WASTE.get(), LucidWasteEntity.createAttributes().build());
        event.put(ModEntities.METEOR.get(), MeteorEntity.createAttributes().build());
        event.put(ModEntities.METEOR_WATCHER.get(), MeteorWatcherEntity.createAttributes().build());
    }

    @EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {

        @SubscribeEvent
        public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(NullBugModel.LAYER_LOCATION, NullBugModel::createBodyLayer);
            event.registerLayerDefinition(LucidWispModel.LAYER_LOCATION, LucidWispModel::createBodyLayer);
            event.registerLayerDefinition(MeteorModel.LAYER_LOCATION, MeteorModel::createBodyLayer);
            event.registerLayerDefinition(MeteorWatcherModel.LAYER_LOCATION, MeteorWatcherModel::createBodyLayer);
        }



        @SubscribeEvent
        public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.NULL_BUG.get(), NullBugRenderer::new);
            event.registerEntityRenderer(ModEntities.LUCID_WASTE.get(), LucidWispRenderer::new);
            event.registerEntityRenderer(ModEntities.METEOR.get(), MeteorRenderer::new);
            event.registerEntityRenderer(ModEntities.METEOR_WATCHER.get(), MeteorWatcherRenderer::new);
            event.registerEntityRenderer(ModEntities.METEOR_MOUNT.get(), MeteorMountRenderer::new);
        }
    }
}