package net.fantik.lostdreams.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fantik.lostdreams.entity.LucidWasteEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class LucidWispModel extends EntityModel<LucidWasteEntity> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath("lostdreams", "lucid_wisp"), "main"
	);

	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart part3;
	private final ModelPart part2;
	private final ModelPart part1;
	private final ModelPart head;

	public LucidWispModel(ModelPart root) {
		this.root = root.getChild("root");
		this.body = this.root.getChild("body");
		this.part3 = this.body.getChild("part3");
		this.part2 = this.body.getChild("part2");
		this.part1 = this.body.getChild("part1");
		this.head = this.body.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root",
				CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body = root.addOrReplaceChild("body",
				CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, 0.0F));

		body.addOrReplaceChild("part3",
				CubeListBuilder.create()
						.texOffs(0, 18).addBox(-4.0F, -0.9F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -4.4F, 0.0F));

		PartDefinition part2 = body.addOrReplaceChild("part2",
				CubeListBuilder.create(), PartPose.offset(0.0F, -2.4F, 0.0F));
		part2.addOrReplaceChild("cube_r1",
				CubeListBuilder.create()
						.texOffs(0, 0).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.3F, 0.0F, 0.0F, 0.3927F, 0.0F));

		PartDefinition part1 = body.addOrReplaceChild("part1",
				CubeListBuilder.create(), PartPose.offset(0.0F, 0.6F, 0.0F));
		part1.addOrReplaceChild("cube_r2",
				CubeListBuilder.create()
						.texOffs(0, 9).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.4363F, 0.0F));

		body.addOrReplaceChild("head",
				CubeListBuilder.create()
						.texOffs(0, 27).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
						.texOffs(24, 27).addBox(-3.0F, -4.0F, -3.4F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(24, 30).addBox(1.0F, -4.0F, -3.4F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -6.4F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(LucidWasteEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root.getAllParts().forEach(ModelPart::resetPose);

		// Боббинг вверх-вниз
		float bob = (float) Math.sin(ageInTicks * 0.05f) * 0.4f;
		this.body.y = -3.0f + bob;

		// part1 вращается в одну сторону
		this.part1.yRot = ageInTicks * 0.05f;

		// part2 вращается в другую сторону
		this.part2.yRot = -ageInTicks * 0.05f;

		// part3 вращается как part1
		this.part3.yRot = ageInTicks * 0.05f;
	}



	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}
}