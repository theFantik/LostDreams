package net.fantik.lostdreams.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.client.model.LucidWispModel;
import net.fantik.lostdreams.client.model.MeteorModel;
import net.fantik.lostdreams.entity.LucidWasteEntity;
import net.fantik.lostdreams.entity.MeteorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MeteorRenderer extends MobRenderer<MeteorEntity, MeteorModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LostDreams.MOD_ID, "textures/entity/meteor.png"
    );

    public MeteorRenderer(EntityRendererProvider.Context context) {
        super(context, new MeteorModel(context.bakeLayer(MeteorModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(MeteorEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(MeteorEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
