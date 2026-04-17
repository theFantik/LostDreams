package net.fantik.lostdreams.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fantik.lostdreams.LostDreams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = LostDreams.MOD_ID, value = Dist.CLIENT)
public class NullZoneSkyRenderer {

    private static final ResourceLocation SKY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "textures/environment/null_zone_sky.png");

    private static final ResourceLocation NULL_ZONE_ID =
            ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "null_zone_dim");

    private static VertexBuffer skyBuffer = null;

    private static void buildSky() {
        if (skyBuffer != null) return;

        skyBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float s = 100.0F;
        float t = 64.0F; // Тайлинг (как в Энде)

        // Рисуем куб. UV координаты от 0 до t заставляют текстуру повторяться
        // ВЕРХ
        bufferBuilder.addVertex(-s,  s, -s).setUv(0.0F, 0.0F);
        bufferBuilder.addVertex(-s,  s,  s).setUv(0.0F, t);
        bufferBuilder.addVertex( s,  s,  s).setUv(t, t);
        bufferBuilder.addVertex( s,  s, -s).setUv(t, 0.0F);
        // НИЗ
        bufferBuilder.addVertex(-s, -s,  s).setUv(0.0F, 0.0F);
        bufferBuilder.addVertex(-s, -s, -s).setUv(0.0F, t);
        bufferBuilder.addVertex( s, -s, -s).setUv(t, t);
        bufferBuilder.addVertex( s, -s,  s).setUv(t, 0.0F);
        // СЕВЕР
        bufferBuilder.addVertex(-s,  s, -s).setUv(0.0F, 0.0F);
        bufferBuilder.addVertex( s,  s, -s).setUv(t, 0.0F);
        bufferBuilder.addVertex( s, -s, -s).setUv(t, t);
        bufferBuilder.addVertex(-s, -s, -s).setUv(0.0F, t);
        // ЮГ
        bufferBuilder.addVertex( s,  s,  s).setUv(0.0F, 0.0F);
        bufferBuilder.addVertex(-s,  s,  s).setUv(t, 0.0F);
        bufferBuilder.addVertex(-s, -s,  s).setUv(t, t);
        bufferBuilder.addVertex( s, -s,  s).setUv(0.0F, t);
        // ЗАПАД
        bufferBuilder.addVertex(-s,  s,  s).setUv(0.0F, 0.0F);
        bufferBuilder.addVertex(-s,  s, -s).setUv(t, 0.0F);
        bufferBuilder.addVertex(-s, -s, -s).setUv(t, t);
        bufferBuilder.addVertex(-s, -s,  s).setUv(0.0F, t);
        // ВОСТОК
        bufferBuilder.addVertex( s,  s, -s).setUv(0.0F, 0.0F);
        bufferBuilder.addVertex( s,  s,  s).setUv(t, 0.0F);
        bufferBuilder.addVertex( s, -s,  s).setUv(t, t);
        bufferBuilder.addVertex( s, -s, -s).setUv(0.0F, t);

        skyBuffer.bind();
        skyBuffer.upload(bufferBuilder.buildOrThrow());
        VertexBuffer.unbind();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;

        if (level == null || !level.dimension().location().equals(NULL_ZONE_ID)) return;

        buildSky();

        // 1. Подготовка стейта (как в примере с астероидами)
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        FogRenderer.setupNoFog();

        // Устанавливаем текстурный шейдер
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SKY_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Настройка повторения текстуры
        RenderSystem.texParameter(3553, 10242, 10497); // GL_REPEAT
        RenderSystem.texParameter(3553, 10243, 10497); // GL_REPEAT

        PoseStack ps = event.getPoseStack();
        var cam = mc.gameRenderer.getMainCamera();

        ps.pushPose();

        // 2. МАГИЯ СТАТИЧНОСТИ (Инверсия вращения камеры)
        // Это заставляет куб вращаться вместе с камерой так, что текстура кажется неподвижной в мире
        ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(cam.getXRot()));
        ps.mulPose(com.mojang.math.Axis.YP.rotationDegrees(cam.getYRot() + 180));

        // Если хочешь медленное вращение самого неба (как время суток), добавь это:
        // float skyAngle = level.getTimeOfDay(event.getPartialTick().getGameTimeDeltaPartialTick(false));
        // ps.mulPose(com.mojang.math.Axis.XP.rotationDegrees(skyAngle * 360));

        Matrix4f projection = event.getProjectionMatrix();
        skyBuffer.bind();
        skyBuffer.drawWithShader(ps.last().pose(), projection, RenderSystem.getShader());
        VertexBuffer.unbind();

        ps.popPose();

        // 3. Возвращаем стейты назад
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}