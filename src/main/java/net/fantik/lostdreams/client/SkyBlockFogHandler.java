package net.fantik.lostdreams.client;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.world.dimension.SkyBlockDimension;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class SkyBlockFogHandler {

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!SkyBlockDimension.isSkyBlock(mc.level)) return;

        long time = mc.level.getDayTime() % 24000;

        // 0 = полночь, 6000 = полдень, 12000 = закат, 18000 = восход
        // Нормализуем к 0.0-1.0 где 1.0 = самый светлый (полдень)
        float brightness;
        if (time < 6000) {
            // Полночь -> полдень: темно -> светло
            brightness = 0.3f + (time / 6000f) * 0.65f;
        } else if (time < 12000) {
            // Полдень -> закат: светло -> темно
            brightness = 0.95f - ((time - 6000) / 6000f) * 0.65f;
        } else if (time < 18000) {
            // Закат -> восход: темно
            brightness = 0.3f;
        } else {
            // Восход -> полночь: темно -> светло
            brightness = 0.3f + ((time - 18000) / 6000f) * 0.65f;
        }

        event.setRed(brightness);
        event.setGreen(brightness);
        event.setBlue(brightness * 1.02f); // чуть голубоватый оттенок ночью
    }

    @SubscribeEvent
    public static void onFogDensity(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!SkyBlockDimension.isSkyBlock(mc.level)) return;

        event.setNearPlaneDistance(20f);
        event.setFarPlaneDistance(80f);
        event.setCanceled(true);
    }
}