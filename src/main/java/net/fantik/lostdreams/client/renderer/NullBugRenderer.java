package net.fantik.lostdreams.client.renderer;

import net.fantik.lostdreams.LostDreams;
import net.fantik.lostdreams.client.model.NullBugModel;
import net.fantik.lostdreams.entity.NullBugEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class NullBugRenderer extends MobRenderer<NullBugEntity, NullBugModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.parse("lostdreams:textures/entity/null_bug.png");

    public NullBugRenderer(EntityRendererProvider.Context context) {
        super(context, new NullBugModel(context.bakeLayer(NullBugModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(NullBugEntity entity) {
        return TEXTURE;
    }
}