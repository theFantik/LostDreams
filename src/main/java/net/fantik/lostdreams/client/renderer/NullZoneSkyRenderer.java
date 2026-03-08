package net.fantik.lostdreams.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fantik.lostdreams.LostDreams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * Рендерит небо как в Энде — текстура натянута на большой куб (skybox).
 *
 * ============================================
 * НАСТРОЙКА ТЕКСТУРЫ
 * ============================================
 * Файл: assets/lostdreams/textures/environment/null_zone_sky.png
 * Размер: 128x128 или больше (степень двойки)
 * Текстура будет повторяться (тайл) на каждой грани куба — как в Энде.
 */
@EventBusSubscriber(modid = LostDreams.MOD_ID, value = Dist.CLIENT)
public class NullZoneSkyRenderer {

    private static final ResourceLocation SKY_TEXTURE =
            ResourceLocation.parse("lostdreams:textures/environment/null_zone_sky.png");

    private static final ResourceLocation NULL_ZONE_ID =
            ResourceLocation.parse("lostdreams:null_zone_dim");

    // Размер куба (как в Энде)
    private static final float SIZE = 100.0f;

    // Сколько раз текстура тайлится на каждой грани (как в Энде — 16 раз)
    private static final float TILE = 32.0f;

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;

        if (level == null || !level.dimension().location().equals(NULL_ZONE_ID)) {
            return;
        }

        renderEndStyleSky(event.getPoseStack());
    }

    private static void renderEndStyleSky(PoseStack poseStack) {
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SKY_TEXTURE);
        RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1.0f);

        // GL_REPEAT для тайлинга текстуры (как в Энде)
        RenderSystem.texParameter(3553, 10242, 10497); // GL_TEXTURE_WRAP_S = GL_REPEAT
        RenderSystem.texParameter(3553, 10243, 10497); // GL_TEXTURE_WRAP_T = GL_REPEAT

        Tesselator tesselator = Tesselator.getInstance();
        Matrix4f matrix = poseStack.last().pose();
        float s = SIZE;
        float t = TILE;

        // ===== ВЕРХНЯЯ ГРАНЬ =====
        drawQuad(tesselator, matrix,
                -s,  s, -s,  0,  0,
                s,  s, -s,  t,  0,
                s,  s,  s,  t,  t,
                -s,  s,  s,  0,  t);

        // ===== НИЖНЯЯ ГРАНЬ =====
        drawQuad(tesselator, matrix,
                -s, -s,  s,  0,  0,
                s, -s,  s,  t,  0,
                s, -s, -s,  t,  t,
                -s, -s, -s,  0,  t);

        // ===== СЕВЕРНАЯ ГРАНЬ (Z-) =====
        drawQuad(tesselator, matrix,
                -s,  s, -s,  0,  0,
                -s, -s, -s,  0,  t,
                s, -s, -s,  t,  t,
                s,  s, -s,  t,  0);

        // ===== ЮЖНАЯ ГРАНЬ (Z+) =====
        drawQuad(tesselator, matrix,
                s,  s,  s,  0,  0,
                s, -s,  s,  0,  t,
                -s, -s,  s,  t,  t,
                -s,  s,  s,  t,  0);

        // ===== ЗАПАДНАЯ ГРАНЬ (X-) =====
        drawQuad(tesselator, matrix,
                -s,  s,  s,  0,  0,
                -s, -s,  s,  0,  t,
                -s, -s, -s,  t,  t,
                -s,  s, -s,  t,  0);

        // ===== ВОСТОЧНАЯ ГРАНЬ (X+) =====
        drawQuad(tesselator, matrix,
                s,  s, -s,  0,  0,
                s, -s, -s,  0,  t,
                s, -s,  s,  t,  t,
                s,  s,  s,  t,  0);

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    /**
     * Рисует один квад (грань куба) с UV-координатами.
     */
    private static void drawQuad(
            Tesselator tesselator, Matrix4f matrix,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3,
            float x4, float y4, float z4, float u4, float v4
    ) {
        BufferBuilder buffer = tesselator.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX
        );
        buffer.addVertex(matrix, x1, y1, z1).setUv(u1, v1);
        buffer.addVertex(matrix, x2, y2, z2).setUv(u2, v2);
        buffer.addVertex(matrix, x3, y3, z3).setUv(u3, v3);
        buffer.addVertex(matrix, x4, y4, z4).setUv(u4, v4);
        BufferUploader.drawWithShader(buffer.build());
    }
}