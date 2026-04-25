package net.fantik.lostdreams;

import net.fantik.lostdreams.block.ModBlocks;
import net.fantik.lostdreams.block.entity.ModBlockEntities;
import net.fantik.lostdreams.datagen.ModDataGenerator;
import net.fantik.lostdreams.effect.ModEffects;
import net.fantik.lostdreams.entity.ModEntities;
import net.fantik.lostdreams.events.ModFuelEvents;
import net.fantik.lostdreams.item.ModCreativeModeTabs;
import net.fantik.lostdreams.item.ModItems;
import net.fantik.lostdreams.particle.ModParticles;
import net.fantik.lostdreams.particle.NullParticle;
import net.fantik.lostdreams.particle.ZirconFlame;
import net.fantik.lostdreams.particle.ZirconParticles;
import net.fantik.lostdreams.screen.DreamGeneratorScreen;
import net.fantik.lostdreams.screen.ModMenuTypes;
import net.fantik.lostdreams.sound.ModSounds;
import net.fantik.lostdreams.world.*;
import net.fantik.lostdreams.world.feature.ModFeatures;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(LostDreams.MOD_ID)
public class LostDreams {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "lostdreams";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();


    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public LostDreams(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        ModParticles.register(modEventBus);
        ModEffects.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        ModFeatures.register(modEventBus);
        ModEntities.register(modEventBus);
        ModSounds.register(modEventBus);
        ModBiomeSources.register(modEventBus);

        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);



        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(SkyBlockChunkGenerator::register);
        modEventBus.addListener(SurrealAsteroidsChunkGenerator::register);
        modEventBus.addListener(GigachrushchevkaChunkGenerator::register);




        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }


    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.DREAM_GENERATOR_MENU.get(), DreamGeneratorScreen::new);
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class ClientModEvents {
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {

        }



        @SubscribeEvent
        public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ModParticles.NULL_PARTICLE.get(), NullParticle.Provider::new);
            event.registerSpriteSet(ModParticles.ZIRCON_FLAME.get(), ZirconFlame.Provider::new);
            event.registerSpriteSet(ModParticles.ZIRCON_PARTICLES.get(), ZirconParticles.Provider::new);
        }
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.DREAM_GENERATOR_MENU.get(), DreamGeneratorScreen::new);
        }
    }
}
