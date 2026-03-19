package net.fantik.lostdreams.client;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.world.dimension.SurrealAsteroidsDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = LostDreams.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class SurrealAsteroidsFogHandler {

    @SubscribeEvent
    public static void onFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!SurrealAsteroidsDimension.isSurrealAsteroids(mc.level)) return;

        event.setRed(0.0f);
        event.setGreen(0.0f);
        event.setBlue(0.0f);
    }

    @SubscribeEvent
    public static void onFogDensity(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        if (!SurrealAsteroidsDimension.isSurrealAsteroids(mc.level)) return;

        event.setNearPlaneDistance(150f);
        event.setFarPlaneDistance(300f);
        event.setCanceled(true);
    }

    // Используем PlayerTickEvent вместо LevelTickEvent — он точно срабатывает на клиенте
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (event.getEntity() != mc.player) return;
        if (!SurrealAsteroidsDimension.isSurrealAsteroids(mc.level)) return;

        // Частицы пыли каждые 4 тика
        if (mc.level.getGameTime() % 4 != 0) return;

        var random = mc.level.getRandom();
        Vec3 playerPos = mc.player.position();

        int count = 3 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            double dx = (random.nextDouble() * 2 - 1) * 20;
            double dy = (random.nextDouble() * 2 - 1) * 15;
            double dz = (random.nextDouble() * 2 - 1) * 20;

            float r = 0.01f + random.nextFloat() * 0.05f;
            float g = 0.01f + random.nextFloat() * 0.05f;
            float b = 0.2f + random.nextFloat() * 0.4f;

            mc.level.addParticle(
                    new DustParticleOptions(new Vector3f(r, g, b), 1.5f),
                    playerPos.x + dx,
                    playerPos.y + dy,
                    playerPos.z + dz,
                    0, 0, 0
            );
        }
    }
}