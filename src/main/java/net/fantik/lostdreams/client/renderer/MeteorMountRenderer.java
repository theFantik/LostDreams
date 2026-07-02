package net.fantik.lostdreams.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.client.model.MeteorModel;
import net.fantik.lostdreams.entity.MeteorMountEntity;
import net.minecraft.client.renderer.MultiBufferSource;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

// Наследуемся от обычного EntityRenderer, теперь никаких ограничений на Mob нет!
public class MeteorMountRenderer extends EntityRenderer<MeteorMountEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(LostDreams.MOD_ID, "textures/entity/meteor.png");

    // Создаем поле для модели внутри рендерера
    private final MeteorModel model;

    public MeteorMountRenderer(EntityRendererProvider.Context context) {
        super(context);
        // Запекаем модель прямо здесь
        this.model = new MeteorModel(context.bakeLayer(MeteorModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(MeteorMountEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(MeteorMountEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        // Переворачиваем и позиционируем модель так же, как это делает Майнкрафт для мобов
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0D, -1.501D, 0.0D);

        // Получаем буфер для отрисовки текстуры
        VertexConsumer vertexConsumer = bufferSource.getBuffer(this.model.renderType(this.getTextureLocation(entity)));

        // Отрисовываем модель вручную
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}