package net.fantik.lostdreams.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.client.model.LucidWispModel;
import net.fantik.lostdreams.entity.LucidWasteEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class LucidWispRenderer extends MobRenderer<LucidWasteEntity, LucidWispModel> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LostDreams.MOD_ID, "textures/entity/lucid_wisp.png"
    );

    public LucidWispRenderer(EntityRendererProvider.Context context) {
        super(context, new LucidWispModel(context.bakeLayer(LucidWispModel.LAYER_LOCATION)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(LucidWasteEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(LucidWasteEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}