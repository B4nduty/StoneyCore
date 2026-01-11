package banduty.stoneycore.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class UnderArmourLeggingsModel extends HumanoidModel<LivingEntity> {
	private final ModelPart armorRightLeg;
	private final ModelPart armorLeftLeg;
	public UnderArmourLeggingsModel(ModelPart root) {
		super(root);
		this.setAllVisible(false);
		this.armorRightLeg = root.getChild("armorRightLeg");
		this.armorLeftLeg = root.getChild("armorLeftLeg");
	}

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.armorRightLeg, this.armorLeftLeg);
	}

	public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = HumanoidModel.createMesh(CubeDeformation.NONE, 0f);
        PartDefinition modelPartData = modelData.getRoot();
		modelPartData.addOrReplaceChild("armorRightLeg", CubeListBuilder.create().texOffs(0, 105).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

		modelPartData.addOrReplaceChild("armorLeftLeg", CubeListBuilder.create().texOffs(0, 105).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.3F)).mirror(false), PartPose.offset(2.0F, 12.0F, 0.0F));
		return LayerDefinition.create(modelData, 128, 128);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		this.armorRightLeg.copyFrom(this.rightLeg);
		this.armorRightLeg.render(poseStack, vertexConsumer, light, overlay, red, green, blue, alpha);

		this.armorLeftLeg.copyFrom(this.leftLeg);
		this.armorLeftLeg.render(poseStack, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}