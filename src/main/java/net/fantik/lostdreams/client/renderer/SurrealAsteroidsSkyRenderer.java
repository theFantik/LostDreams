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

        for (int i = 0; i < count; i++) {
            double theta = rand.nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * rand.nextDouble() - 1);

            float nx = (float)(Math.sin(phi) * Math.cos(theta));
            float ny = (float)(Math.cos(phi));
            float nz = (float)(Math.sin(phi) * Math.sin(theta));

            float px = nx * dist;
            float py = ny * dist;
            float pz = nz * dist;

            float size = 0.08f + rand.nextFloat() * 0.08f;
            boolean rainbow = rand.nextFloat() < 0.01f;
            float[] c = STAR_COLORS[rand.nextInt(STAR_COLORS.length)];

            // Billboard: right и up перпендикулярны радиус-вектору звезды
            Vector3f forward = new Vector3f(nx, ny, nz); // уже нормализован (единичная сфера)

            Vector3f helper = (Math.abs(ny) < 0.99f)
                    ? new Vector3f(0, 1, 0)
                    : new Vector3f(1, 0, 0);

            // right = forward × helper, затем нормализуем
            Vector3f right = new Vector3f(forward).cross(helper).normalize().mul(size);
            // up = right × forward (порядок важен!)
            Vector3f up = new Vector3f(right).cross(forward).normalize().mul(size);

            // 4 угла
            float[] v0 = {px - right.x - up.x, py - right.y - up.y, pz - right.z - up.z};
            float[] v1 = {px + right.x - up.x, py + right.y - up.y, pz + right.z - up.z};
            float[] v2 = {px + right.x + up.x, py + right.y + up.y, pz + right.z + up.z};
            float[] v3 = {px - right.x + up.x, py - right.y + up.y, pz - right.z + up.z};

            float[][] tris = {v0, v1, v2, v0, v2, v3};

            // Фиксируем цвет ДО цикла по вершинам
            float fr = rainbow ? rand.nextFloat() : c[0];
            float fg = rainbow ? rand.nextFloat() : c[1];
            float fb = rainbow ? rand.nextFloat() : c[2];

            for (float[] v : tris) {
                buf.addVertex(v[0], v[1], v[2]).setColor(fr, fg, fb, 1);
            }
        }

        starBuffers[bufIdx].bind();
        starBuffers[bufIdx].upload(buf.buildOrThrow());
        VertexBuffer.unbind();
    }

    private static void buildStars() {
        buildLayer(0, 80f,  1000, 1000L);
        buildLayer(1, 130f, 800,  2000L);
        buildLayer(2, 200f, 600,  3000L);
        buildLayer(3, 80f,  1000, 4000L);
        buildLayer(4, 130f, 800,  5000L);
        buildLayer(5, 200f, 600,  6000L);
    }

    private static void spawnShootingStar() {
        if (RANDOM.nextFloat() < 0.002f) {
            shootingStars.add(new ShootingStar(
                    new Vector3f(RANDOM.nextFloat()*200-100, RANDOM.nextFloat()*100, RANDOM.nextFloat()*200-100),
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

        var cam = mc.gameRenderer.getMainCamera();
        long time = level.getGameTime();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        FogRenderer.setupNoFog();

        PoseStack ps = event.getPoseStack();

        // === ПАРАЛЛАКС ФАКТОРЫ (ОЧЕНЬ МАЛЕНЬКИЕ!) ===
        float[] parallax = {0.002f, 0.001f, 0.0005f};

        // -------- ВЕРХ --------
        ps.pushPose();
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(cam.getXRot()));
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(cam.getYRot() + 180));
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(skyAngle * 360));

        for (int i = 0; i < 3; i++) {
            ps.pushPose();

            // 🔥 ВОТ ОН — ПРАВИЛЬНЫЙ ПАРАЛЛАКС
            ps.translate(
                    -cam.getPosition().x * parallax[i],
                    -cam.getPosition().y * parallax[i],
                    -cam.getPosition().z * parallax[i]
            );

            float flicker = 0.92f + 0.08f * (float)Math.sin(time * 0.02f + i * 2.1f);
            RenderSystem.setShaderColor(flicker, flicker, flicker, 1);

            starBuffers[i].bind();
            starBuffers[i].drawWithShader(ps.last().pose(), event.getProjectionMatrix(), shader);
            VertexBuffer.unbind();

            ps.popPose();
        }
        ps.popPose();

        // -------- НИЗ --------
        ps.pushPose();
        ps.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(0));
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(cam.getXRot()));
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(cam.getYRot() + 180));
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90));
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(skyAngle * 360 + 180));

        for (int i = 3; i < 6; i++) {
            ps.pushPose();

            float factor = parallax[i - 3];

            ps.translate(
                    -cam.getPosition().x * factor,
                    -cam.getPosition().y * factor,
                    -cam.getPosition().z * factor
            );

            float flicker = 0.92f + 0.08f * (float)Math.sin(time * 0.02f + i * 2.1f);
            RenderSystem.setShaderColor(flicker, flicker, flicker, 1);

            starBuffers[i].bind();
            starBuffers[i].drawWithShader(ps.last().pose(), event.getProjectionMatrix(), shader);
            VertexBuffer.unbind();

            ps.popPose();
        }
        ps.popPose();

        RenderSystem.setShaderColor(1,1,1,1);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }
}