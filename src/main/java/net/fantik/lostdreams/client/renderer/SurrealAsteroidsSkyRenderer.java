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

    // Объединяем в 2 буфера вместо 6 — меньше draw calls
    private static VertexBuffer starBufferTop = null;
    private static VertexBuffer starBufferBottom = null;
    private static boolean starsBuilt = false;

    private static final Random RANDOM = new Random();
    private static final List<ShootingStar> shootingStars = new ArrayList<>();
    private static final int MAX_SHOOTING_STARS = 5;

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

    private static void addVertex(BufferBuilder buf, Vector3f v, float r, float g, float b) {
        buf.addVertex(v.x, v.y, v.z).setColor(r, g, b, 1f);
    }

    private static void buildHalfSphere(BufferBuilder buf, int count, long seed, float dist) {
        Random rand = new Random(seed);
        for (int i = 0; i < count; i++) {
            double theta = rand.nextDouble() * Math.PI * 2;
            double phi   = Math.acos(2 * rand.nextDouble() - 1);

            float nx = (float)(Math.sin(phi) * Math.cos(theta));
            float ny = (float)(Math.cos(phi));
            float nz = (float)(Math.sin(phi) * Math.sin(theta));

            Vector3f center = new Vector3f(nx, ny, nz).mul(dist);
            float size = 0.06f + rand.nextFloat() * 0.1f;
            int shapeType = rand.nextInt(3);

            float[] c = STAR_COLORS[rand.nextInt(STAR_COLORS.length)];
            float fr = c[0], fg = c[1], fb = c[2];

            Vector3f forward = new Vector3f(nx, ny, nz);
            Vector3f helper = Math.abs(ny) < 0.99f ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);
            Vector3f right = new Vector3f(forward).cross(helper).normalize().mul(size);
            Vector3f up    = new Vector3f(right).cross(forward).normalize().mul(size);

            if (shapeType == 0) {
                // Квадрат
                Vector3f v0 = new Vector3f(center).sub(right).sub(up);
                Vector3f v1 = new Vector3f(center).add(right).sub(up);
                Vector3f v2 = new Vector3f(center).add(right).add(up);
                Vector3f v3 = new Vector3f(center).sub(right).add(up);
                addVertex(buf, v0, fr, fg, fb); addVertex(buf, v1, fr, fg, fb); addVertex(buf, v2, fr, fg, fb);
                addVertex(buf, v0, fr, fg, fb); addVertex(buf, v2, fr, fg, fb); addVertex(buf, v3, fr, fg, fb);
            } else if (shapeType == 1) {
                // Треугольник
                Vector3f v0 = new Vector3f(center).sub(new Vector3f(up).mul(1.6f));
                Vector3f v1 = new Vector3f(center).sub(new Vector3f(right).mul(1.3f)).add(up);
                Vector3f v2 = new Vector3f(center).add(new Vector3f(right).mul(1.3f)).add(up);
                addVertex(buf, v0, fr, fg, fb);
                addVertex(buf, v1, fr, fg, fb);
                addVertex(buf, v2, fr, fg, fb);
            } else {
                // Ромб
                Vector3f v0 = new Vector3f(center).sub(new Vector3f(up).mul(1.7f));
                Vector3f v1 = new Vector3f(center).sub(new Vector3f(right).mul(1.2f));
                Vector3f v2 = new Vector3f(center).add(new Vector3f(up).mul(1.7f));
                Vector3f v3 = new Vector3f(center).add(new Vector3f(right).mul(1.2f));
                addVertex(buf, v0, fr, fg, fb); addVertex(buf, v1, fr, fg, fb); addVertex(buf, v2, fr, fg, fb);
                addVertex(buf, v0, fr, fg, fb); addVertex(buf, v2, fr, fg, fb); addVertex(buf, v3, fr, fg, fb);
            }
        }
    }

    private static void buildStars() {
        if (starsBuilt) return;
        starsBuilt = true;

        // Верхняя полусфера — все слои в одном буфере
        starBufferTop = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder bufTop = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        buildHalfSphere(bufTop, 1000, 1000L, 80f);
        buildHalfSphere(bufTop, 800,  2000L, 130f);
        buildHalfSphere(bufTop, 600,  3000L, 200f);
        starBufferTop.bind();
        starBufferTop.upload(bufTop.buildOrThrow());
        VertexBuffer.unbind();

        // Нижняя полусфера
        starBufferBottom = new VertexBuffer(VertexBuffer.Usage.STATIC);
        BufferBuilder bufBottom = Tesselator.getInstance().begin(
                VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        buildHalfSphere(bufBottom, 1000, 4000L, 80f);
        buildHalfSphere(bufBottom, 800,  5000L, 130f);
        buildHalfSphere(bufBottom, 600,  6000L, 200f);
        starBufferBottom.bind();
        starBufferBottom.upload(bufBottom.buildOrThrow());
        VertexBuffer.unbind();
    }

    private static void spawnShootingStar() {
        if (shootingStars.size() >= MAX_SHOOTING_STARS) return;
        if (RANDOM.nextFloat() < 0.002f) {
            shootingStars.add(new ShootingStar(
                    new Vector3f(RANDOM.nextFloat() * 200 - 100,
                            RANDOM.nextFloat() * 100,
                            RANDOM.nextFloat() * 200 - 100),
                    new Vector3f(0.2f, -1f, 0.2f).normalize()
            ));
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
        long time = level.getGameTime();
        var cam = mc.gameRenderer.getMainCamera();

        // Мерцание — один расчёт на кадр
        float flicker = 0.92f + 0.08f * (float)Math.sin(time * 0.02f);

        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        FogRenderer.setupNoFog();
        RenderSystem.setShaderColor(flicker, flicker, flicker, 1);

        PoseStack ps = event.getPoseStack();

        // Верхняя полусфера — 1 draw call
        ps.pushPose();
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(cam.getXRot()));
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(cam.getYRot() + 180));
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(skyAngle * 360));
        starBufferTop.bind();
        starBufferTop.drawWithShader(ps.last().pose(), event.getProjectionMatrix(), shader);
        VertexBuffer.unbind();
        ps.popPose();

        // Нижняя полусфера — 1 draw call
        ps.pushPose();
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(cam.getXRot()));
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(cam.getYRot() + 180));
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(skyAngle * 360 + 180));
        starBufferBottom.bind();
        starBufferBottom.drawWithShader(ps.last().pose(), event.getProjectionMatrix(), shader);
        VertexBuffer.unbind();
        ps.popPose();

        // Падающие звёзды — динамический буфер только если есть звёзды
        if (!shootingStars.isEmpty()) {
            ps.pushPose();
            ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(cam.getXRot()));
            ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(cam.getYRot() + 180));

            BufferBuilder shootBuf = Tesselator.getInstance().begin(
                    VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);

            Iterator<ShootingStar> it = shootingStars.iterator();
            while (it.hasNext()) {
                ShootingStar s = it.next();
                Vector3f end = new Vector3f(s.pos).add(new Vector3f(s.dir).mul(8));
                shootBuf.addVertex(s.pos.x, s.pos.y, s.pos.z).setColor(1f, 1f, 1f, s.life);
                shootBuf.addVertex(end.x, end.y, end.z).setColor(1f, 1f, 1f, 0f);
                s.pos.add(new Vector3f(s.dir).mul(2));
                s.life -= 0.025f;
                if (s.life <= 0) it.remove();
            }

            BufferUploader.drawWithShader(shootBuf.buildOrThrow());
            ps.popPose();
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }
}