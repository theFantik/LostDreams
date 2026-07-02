package net.fantik.lostdreams.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.client.model.MeteorModel;
import net.fantik.lostdreams.client.model.MeteorWatcherModel;
import net.fantik.lostdreams.entity.MeteorEntity;
import net.fantik.lostdreams.entity.MeteorWatcherEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MeteorWatcherRenderer extends MobRenderer<MeteorWatcherEntity, MeteorWatcherModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LostDreams.MOD_ID, "textures/entity/meteor_watcher.png"
    );

    public MeteorWatcherRenderer(EntityRendererProvider.Context context) {
        super(context, new MeteorWatcherModel(context.bakeLayer(MeteorWatcherModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(MeteorWatcherEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(MeteorWatcherEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
