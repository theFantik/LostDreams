package net.fantik.lostdreams.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.world.dimension.SurrealAsteroidsDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;

import java.util.*;

@EventBusSubscriber(modid = LostDreams.MOD_ID, value = Dist.CLIENT)
public class SurrealAsteroidsSkyRenderer {

    // 6 буферов — 3 слоя x 2 полусферы (верхняя + нижняя)
    private static VertexBuffer[] starBuffers = new VertexBuffer[6];
    private static final Random RANDOM = new Random();
    private static final List<ShootingStar> shootingStars = new ArrayList<>();

    private static class ShootingStar {
        Vector3f pos;
        Vector3f dir;
        float life;

        ShootingStar(Vector3f pos, Vector3f dir) {
            this.pos = pos;
            this.dir = dir;
            this.life = 1f;
        }
    }

    private static final float[][] STAR_COLORS = {
            {1f, 1f, 1f},
            {0.8f, 0.9f, 1f},
            {1f, 0.8f, 0.6f},
            {0.6f, 0.8f, 1f},
            {0.9f, 0.5f, 1f},
            {0.3f, 1f, 0.8f},
            {1f, 0.4f, 0.3f},
            {1f, 1f, 0.4f},
    };

    private static void buildLayer(int bufIdx, float dist, int count, long seed) {
        if (starBuffers[bufIdx] != null) return;

        starBuffers[bufIdx] = new VertexBuffer(VertexBuffer.Usage.STATIC);
        Random rand = new Random(seed);

        BufferBuilder buf = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        List<float[]> placed = new ArrayList<>();
        float minAngle = 0.035f;
        int attempts = 0;
        int placed_count = 0;

        while (placed_count < count && attempts < count * 10) {
            attempts++;

            double theta = rand.nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * rand.nextDouble() - 1);

            float nx = (float)(Math.sin(phi) * Math.cos(theta));
            float ny = (float)(Math.cos(phi));
            float nz = (float)(Math.sin(phi) * Math.sin(theta));

            boolean tooClose = false;
            for (float[] p : placed) {
                float dot = nx * p[0] + ny * p[1] + nz * p[2];
                if (dot > (float)Math.cos(minAngle)) {
                    tooClose = true;
                    break;
                }
            }
            if (tooClose) continue;

            placed.add(new float[]{nx, ny, nz});
            placed_count++;

            float px = nx * dist;
            float py = ny * dist;
            float pz = nz * dist;

            float size = 0.10f + rand.nextFloat() * 0.10f;
            boolean rainbow = rand.nextFloat() < 0.01f;
            float[] c = STAR_COLORS[rand.nextInt(STAR_COLORS.length)];

            if (rand.nextFloat() < 0.3f) {
                for (int v = 0; v < 3; v++) {
                    float r = rainbow ? rand.nextFloat() : c[0];
                    float g = rainbow ? rand.nextFloat() : c[1];
                    float b = rainbow ? rand.nextFloat() : c[2];
                    float dx = (v == 0 ? 0 : (v == 1 ? -size : size));
                    float dy = (v == 0 ? size : -size);
                    buf.addVertex(px + dx, py + dy, pz).setColor(r, g, b, 1);
                }
            } else {
                float[][] quad = {
                        {-size, -size}, {size, -size}, {size, size},
                        {-size, -size}, {size, size}, {-size, size}
                };
                for (float[] v : quad) {
                    float r = rainbow ? rand.nextFloat() : c[0];
                    float g = rainbow ? rand.nextFloat() : c[1];
                    float b = rainbow ? rand.nextFloat() : c[2];
                    buf.addVertex(px + v[0], py + v[1], pz).setColor(r, g, b, 1);
                }
            }
        }

        starBuffers[bufIdx].bind();
        starBuffers[bufIdx].upload(buf.buildOrThrow());
        VertexBuffer.unbind();
    }

    private static void buildStars() {
        // Слой 0: верхняя полусфера
        buildLayer(0, 80f,  1000, 1000L);
        buildLayer(1, 130f, 800,  2000L);
        buildLayer(2, 200f, 600,  3000L);
        // Слой 1: нижняя полусфера (другой seed — разные звёзды)
        buildLayer(3, 80f,  1000, 4000L);
        buildLayer(4, 130f, 800,  5000L);
        buildLayer(5, 200f, 600,  6000L);
    }

    private static void spawnShootingStar() {
        if (RANDOM.nextFloat() < 0.002f) {
            Vector3f pos = new Vector3f(
                    RANDOM.nextFloat() * 200 - 100,
                    RANDOM.nextFloat() * 100,
                    RANDOM.nextFloat() * 200 - 100
            );
            Vector3f dir = new Vector3f(
                    RANDOM.nextFloat() * 0.5f,
                    -1f,
                    RANDOM.nextFloat() * 0.5f
            ).normalize();
            shootingStars.add(new ShootingStar(pos, dir));
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;
        if (!SurrealAsteroidsDimension.isSurrealAsteroids(level)) return;

        ShaderInstance shader = GameRenderer.getPositionColorShader();
        if (shader == null) return;

        buildStars();
        spawnShootingStar();

        float pt = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float skyAngle = level.getTimeOfDay(pt);

        var cam = mc.gameRenderer.getMainCamera();
        long time = level.getGameTime();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        FogRenderer.setupNoFog();

        // Рендерим верхнюю полусферу с обычным вращением
        PoseStack ps = event.getPoseStack();
        ps.pushPose();
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(cam.getXRot()));
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(cam.getYRot() + 180));
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(skyAngle * 360));

        for (int i = 0; i < 3; i++) {
            float flicker = 0.92f + 0.08f * (float)Math.sin(time * 0.02f + i * 2.1f);
            RenderSystem.setShaderColor(flicker, flicker, flicker, 1);
            starBuffers[i].bind();
            starBuffers[i].drawWithShader(ps.last().pose(), event.getProjectionMatrix(), shader);
            VertexBuffer.unbind();
        }
        ps.popPose();

        // Рендерим нижнюю полусферу со смещением 180 градусов — покрывает противоположную сторону
        ps.pushPose();
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(cam.getXRot()));
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(cam.getYRot() + 180));
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(skyAngle * 360 + 180)); // +180 — другая сторона

        for (int i = 3; i < 6; i++) {
            float flicker = 0.92f + 0.08f * (float)Math.sin(time * 0.02f + i * 2.1f);
            RenderSystem.setShaderColor(flicker, flicker, flicker, 1);
            starBuffers[i].bind();
            starBuffers[i].drawWithShader(ps.last().pose(), event.getProjectionMatrix(), shader);
            VertexBuffer.unbind();
        }
        ps.popPose();

        // Падающие звёзды
        if (!shootingStars.isEmpty()) {
            ps.pushPose();
            ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(cam.getXRot()));
            ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(cam.getYRot() + 180));

            BufferBuilder shootBuf = Tesselator.getInstance().begin(
                    VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);

            Iterator<ShootingStar> it = shootingStars.iterator();
            while (it.hasNext()) {
                ShootingStar sStar = it.next();
                Vector3f start = sStar.pos;
                Vector3f end = new Vector3f(start).add(new Vector3f(sStar.dir).mul(5));
                shootBuf.addVertex(start.x, start.y, start.z).setColor(1, 1, 1, sStar.life);
                shootBuf.addVertex(end.x, end.y, end.z).setColor(1, 1, 1, 0);
                sStar.pos.add(new Vector3f(sStar.dir).mul(2));
                sStar.life -= 0.02f;
                if (sStar.life <= 0) it.remove();
            }
            BufferUploader.drawWithShader(shootBuf.buildOrThrow());
            ps.popPose();
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }
}