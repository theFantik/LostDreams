package net.fantik.lostdreams.block;



import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.util.PortalDefinition;
import net.fantik.lostdreams.util.PortalRegistry;
import net.fantik.lostdreams.world.dimension.NullZoneDimension;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.fantik.lostdreams.LostDreams;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModPortals {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Null Zone портал
            PortalRegistry.register(PortalDefinition.builder()
                    .frame(ModBlocks.NULL_STONE.get())
                    .portalBlock(ModBlocks.NULL_ZONE_PORTAL.get())
                    .destination(NullZoneDimension.NULL_ZONE_KEY)
                    .minWidth(2).minHeight(3)
                    .build());

            // Surreal Asteroids портал — добавишь когда будет готов блок
            // PortalRegistry.register(PortalDefinition.builder()
            //         .frame(ModBlocks.SURREAL_BLUE_ROCK.get())
            //         .portalBlock(ModBlocks.SURREAL_ASTEROIDS_PORTAL.get())
            //         .destination(SurrealAsteroidsDimension.SURREAL_ASTEROIDS_KEY)
            //         .minWidth(2).minHeight(3)
            //         .build());
        });
    }
}