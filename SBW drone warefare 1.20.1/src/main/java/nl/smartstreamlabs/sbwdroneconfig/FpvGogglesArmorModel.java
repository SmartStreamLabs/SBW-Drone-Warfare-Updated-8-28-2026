package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public final class FpvGogglesArmorModel extends HumanoidModel<LivingEntity> {
    private final ModelPart frame;
    private final ModelPart lens;
    private final ModelPart leftStrap;
    private final ModelPart rightStrap;
    private final ModelPart rearBand;
    private final ModelPart antenna;
    private final ModelPart antennaTip;
    private final ModelPart leftLight;
    private final ModelPart rightLight;

    private FpvGogglesArmorModel(ModelPart root) {
        super(root);
        this.frame = this.head.getChild("fpv_frame");
        this.lens = this.head.getChild("fpv_lens");
        this.leftStrap = this.head.getChild("fpv_left_strap");
        this.rightStrap = this.head.getChild("fpv_right_strap");
        this.rearBand = this.head.getChild("fpv_rear_band");
        this.antenna = this.head.getChild("fpv_antenna");
        this.antennaTip = this.head.getChild("fpv_antenna_tip");
        this.leftLight = this.head.getChild("fpv_left_light");
        this.rightLight = this.head.getChild("fpv_right_light");
    }

    static FpvGogglesArmorModel create() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        CubeDeformation crisp = new CubeDeformation(0.02F);
        head.addOrReplaceChild("fpv_frame", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.6F, -6.1F, -5.35F, 9.2F, 2.9F, 1.25F, crisp), PartPose.ZERO);
        head.addOrReplaceChild("fpv_lens", CubeListBuilder.create().texOffs(0, 5)
                .addBox(-3.85F, -5.55F, -5.72F, 7.7F, 1.75F, 0.25F, CubeDeformation.NONE), PartPose.ZERO);
        head.addOrReplaceChild("fpv_left_strap", CubeListBuilder.create().texOffs(0, 8)
                .addBox(4.05F, -5.55F, -4.15F, 0.75F, 1.25F, 7.35F, crisp), PartPose.ZERO);
        head.addOrReplaceChild("fpv_right_strap", CubeListBuilder.create().texOffs(0, 8)
                .addBox(-4.8F, -5.55F, -4.15F, 0.75F, 1.25F, 7.35F, crisp), PartPose.ZERO);
        head.addOrReplaceChild("fpv_rear_band", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-4.0F, -5.4F, 3.05F, 8.0F, 0.95F, 0.75F, crisp), PartPose.ZERO);
        head.addOrReplaceChild("fpv_antenna", CubeListBuilder.create().texOffs(0, 12)
                .addBox(3.15F, -8.8F, -4.75F, 0.25F, 2.45F, 0.25F, CubeDeformation.NONE), PartPose.ZERO);
        head.addOrReplaceChild("fpv_antenna_tip", CubeListBuilder.create().texOffs(3, 12)
                .addBox(2.85F, -9.1F, -4.95F, 0.85F, 0.35F, 0.35F, CubeDeformation.NONE), PartPose.ZERO);
        head.addOrReplaceChild("fpv_left_light", CubeListBuilder.create().texOffs(6, 12)
                .addBox(-3.55F, -4.55F, -5.95F, 0.55F, 0.45F, 0.12F, CubeDeformation.NONE), PartPose.ZERO);
        head.addOrReplaceChild("fpv_right_light", CubeListBuilder.create().texOffs(9, 12)
                .addBox(3.0F, -4.55F, -5.95F, 0.55F, 0.45F, 0.12F, CubeDeformation.NONE), PartPose.ZERO);

        FpvGogglesArmorModel model = new FpvGogglesArmorModel(LayerDefinition.create(mesh, 16, 16).bakeRoot());
        model.setAllVisible(false);
        model.head.visible = true;
        return model;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (!this.head.visible) {
            return;
        }
        poseStack.pushPose();
        this.head.translateAndRotate(poseStack);
        render(this.frame, poseStack, buffer, packedLight, packedOverlay, 0.015F, 0.02F, 0.02F, alpha);
        render(this.leftStrap, poseStack, buffer, packedLight, packedOverlay, 0.01F, 0.012F, 0.012F, alpha);
        render(this.rightStrap, poseStack, buffer, packedLight, packedOverlay, 0.01F, 0.012F, 0.012F, alpha);
        render(this.rearBand, poseStack, buffer, packedLight, packedOverlay, 0.01F, 0.012F, 0.012F, alpha);
        render(this.lens, poseStack, buffer, packedLight, packedOverlay, 0.22F, 0.28F, 0.30F, alpha);
        render(this.antenna, poseStack, buffer, packedLight, packedOverlay, 0.025F, 0.03F, 0.03F, alpha);
        render(this.antennaTip, poseStack, buffer, packedLight, packedOverlay, 0.035F, 0.04F, 0.04F, alpha);
        render(this.leftLight, poseStack, buffer, packedLight, packedOverlay, 0.12F, 1.0F, 0.26F, alpha);
        render(this.rightLight, poseStack, buffer, packedLight, packedOverlay, 1.0F, 0.18F, 0.15F, alpha);
        poseStack.popPose();
    }

    private static void render(ModelPart part, PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        part.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
