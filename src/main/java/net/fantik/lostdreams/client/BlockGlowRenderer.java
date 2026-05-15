package net.fantik.lostdreams.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fantik.lostdreams.LostDreams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Collections;
import java.util.List;

@EventBusSubscriber(
        modid = LostDreams.MOD_ID,
        bus = EventBusSubscriber.Bus.GAME,
        value = Dist.CLIENT)
public class BlockGlowRenderer {

    // Список позиций блоков для подсветки
    private static volatile List<BlockPos> glowingBlocks =
            Collections.emptyList();

    // Время последнего обновления (для fade-out)
    private static long lastUpdateTime = 0;

    // Сколько тиков держим подсветку
    private static final int GLOW_DURATION_TICKS = 30;

    public static void updateGlowingBlocks(List<BlockPos> positions) {
        glowingBlocks = List.copyOf(positions);
        lastUpdateTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES)
            return;

        if (glowingBlocks.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        var bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(XRayRenderType.XRAY_LINES);

        var camPos = mc.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        // СТАБИЛЬНЫЙ цвет XRAY
        float r = 0.2f;
        float g = 0.8f;
        float b = 1.0f;
        float a = 1.0f;

        for (BlockPos pos : glowingBlocks) {
            // 🔥 анти-мерцание — увеличиваем коробку на 0.01
            AABB box = new AABB(pos).inflate(0.01);

            LevelRenderer.renderLineBox(
                    poseStack, consumer,
                    box.minX, box.minY, box.minZ,
                    box.maxX, box.maxY, box.maxZ,
                    r, g, b, a
            );
        }

        poseStack.popPose();
        bufferSource.endBatch(XRayRenderType.XRAY_LINES);
    }
}
