package net.fantik.lostdreams.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fantik.lostdreams.block.entity.TestPortalBlockEntity;
import net.fantik.lostdreams.client.renderer.TestPortalShader;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.joml.Matrix4f;

public class TestPortalRenderer
        implements BlockEntityRenderer<TestPortalBlockEntity> {

    public TestPortalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            TestPortalBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {

        VertexConsumer buffer =
                bufferSource.getBuffer(TestPortalShader.PORTAL);

        Matrix4f pose = poseStack.last().pose();

        /*
         * Вертикальная плоскость.
         *
         * Она находится внутри блока:
         *
         *       Y
         *       ↑
         *       │
         *       ┌──────┐
         *       │      │
         *       │PORTAL│
         *       │      │
         *       └──────┘
         *       └────────→ X
         *
         * Пока делаем портал на передней стороне блока.
         */

        float z = 0.501f;

        buffer.addVertex(pose, 0.0f, 0.0f, z)
                .setUv(0.0f, 0.0f);

        buffer.addVertex(pose, 1.0f, 0.0f, z)
                .setUv(1.0f, 0.0f);

        buffer.addVertex(pose, 1.0f, 1.0f, z)
                .setUv(1.0f, 1.0f);

        buffer.addVertex(pose, 0.0f, 1.0f, z)
                .setUv(0.0f, 1.0f);
    }
}