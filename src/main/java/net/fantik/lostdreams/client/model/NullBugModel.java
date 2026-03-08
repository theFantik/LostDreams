package net.fantik.lostdreams.client.model;

import net.fantik.lostdreams.entity.NullBugEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class NullBugModel extends EntityModel<NullBugEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.parse("lostdreams:null_bug"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart right_arm2;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart left_arm2;
    private final ModelPart right_leg;
    private final ModelPart left_leg;

    public NullBugModel(ModelPart root) {
        this.root = root.getChild("root");
        this.body = this.root.getChild("body");
        this.head = this.body.getChild("head");
        this.right_arm2 = this.body.getChild("right_arm2");
        this.right_arm = this.body.getChild("right_arm");
        this.left_arm = this.body.getChild("left_arm");
        this.left_arm2 = this.body.getChild("left_arm2");
        this.right_leg = this.root.getChild("right_leg");
        this.left_leg = this.root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -3.0F, -5.0F, 4.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 2.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 12).addBox(-2.0F, -1.25F, -3.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.75F, -5.0F));
        head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(14, 12).addBox(0.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.25F, -3.0F, 2.0273F, -0.5194F, -0.2391F));
        head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(14, 16).addBox(-1.0F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.25F, -3.0F, 2.0273F, 0.5194F, 0.2391F));

        PartDefinition right_arm2 = body.addOrReplaceChild("right_arm2", CubeListBuilder.create(), PartPose.offset(-1.75F, -2.0F, 0.0F));
        right_arm2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.25F, 0.0F, -0.75F, 2.0273F, 0.5194F, 0.2391F));

        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-1.75F, -2.0F, -2.75F));
        right_arm.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(8, 18).addBox(-2.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.25F, 0.0F, -0.75F, 2.0273F, 0.5194F, 0.2391F));

        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(1.75F, -2.0F, -2.75F));
        left_arm.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(18, 18).addBox(1.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.25F, 0.0F, -0.75F, 2.0273F, -0.5194F, -0.2391F));

        PartDefinition left_arm2 = body.addOrReplaceChild("left_arm2", CubeListBuilder.create(), PartPose.offset(1.75F, -2.0F, 0.0F));
        left_arm2.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(18, 15).addBox(1.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.25F, 0.0F, -0.75F, 2.0273F, -0.5194F, -0.2391F));

        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.75F, -2.0F, 5.0F));
        right_leg.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(4, 18).addBox(-2.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.25F, 0.0F, -1.0F, 2.0273F, 0.5194F, 0.2391F));

        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.75F, -2.0F, 5.0F));
        left_leg.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(18, 12).addBox(1.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.25F, 0.0F, -1.0F, 2.0273F, -0.5194F, -0.2391F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(NullBugEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

        // ===== АТАКА =====
        if (entity.attackTick > 0) {
            float progress = entity.attackTick / 10.0F; // от 1.0 до 0.0
            // Голова резко вниз и обратно
            this.head.xRot = -Mth.sin(progress * Mth.PI) * 0.44F; // ~25 градусов
            return; // остальные анимации не играют во время атаки
        }

        // ===== ХОДЬБА =====
        // Крест-накрест: right_arm + left_leg вместе, left_arm + right_leg вместе
        float walkSpeed = 0.6662F;
        float walkAmount = limbSwingAmount * 0.8F;

        this.right_arm.xRot  =  Mth.cos(limbSwing * walkSpeed)               * walkAmount;
        this.left_arm.xRot   = -Mth.cos(limbSwing * walkSpeed)               * walkAmount;
        this.right_arm2.xRot =  Mth.cos(limbSwing * walkSpeed + Mth.PI / 2F) * walkAmount * 0.7F;
        this.left_arm2.xRot  = -Mth.cos(limbSwing * walkSpeed + Mth.PI / 2F) * walkAmount * 0.7F;
        this.right_leg.xRot  = -Mth.cos(limbSwing * walkSpeed)               * walkAmount;
        this.left_leg.xRot   =  Mth.cos(limbSwing * walkSpeed)               * walkAmount;

        // ===== IDLE — покачивание головы =====
        this.head.xRot = Mth.sin(ageInTicks * 0.05F) * 0.05F - 0.044F; // ~2.5 градуса
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}