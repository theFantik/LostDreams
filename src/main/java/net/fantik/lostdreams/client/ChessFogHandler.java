package net.fantik.lostdreams.client;

import com.mojang.blaze3d.shaders.FogShape;
import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.world.dimension.ChessDimension;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(
        modid = LostDreams.MOD_ID,
        bus = EventBusSubscriber.Bus.GAME,
        value = Dist.CLIENT
)
public class ChessFogHandler {

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) return;
        if (!ChessDimension.isChessDim(mc.level)) return;


        event.setRed(0.045f);
        event.setGreen(0.020f);
        event.setBlue(0.120f);

    }

    @SubscribeEvent
    public static void onFogDensity(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null) return;
        if (!ChessDimension.isChessDim(mc.level)) return;

        event.setNearPlaneDistance(35f);
        event.setFarPlaneDistance(380f);
        event.setFogShape(FogShape.CYLINDER);
        event.setCanceled(true);
    }
}