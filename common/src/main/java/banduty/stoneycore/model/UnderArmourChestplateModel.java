package banduty.stoneycore.model;

import banduty.stoneycore.StoneyCore;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class UnderArmourChestplateModel extends HumanoidModel<LivingEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation(StoneyCore.MOD_ID, "under_armour_chestplate"), "main");

	private final ModelPart armorBody;
	private final ModelPart armorRightArm;
	private final ModelPart armorLeftArm;
	public UnderArmourChestplateModel(ModelPart root) {
		super(root);
		this.setAllVisible(false);
		this.armorBody = root.getChild("armorBody");
		this.armorRightArm = root.getChild("armorRightArm");
		this.armorLeftArm = root.getChild("armorLeftArm");
	}

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.armorBody, this.armorRightArm, this.armorLeftArm);
	}

	public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = HumanoidModel.createMesh(CubeDeformation.NONE, 0f);
        PartDefinition modelPartData = modelData.getRoot();
		modelPartData.addOrReplaceChild("armorBody", CubeListBuilder.create().texOffs(0, 80).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 16.0F, 4.0F, new CubeDeformation(0.45F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		modelPartData.addOrReplaceChild("armorRightArm", CubeListBuilder.create().texOffs(24, 80).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.33F)), PartPose.offset(-4.0F, 2.0F, 0.0F));

		modelPartData.addOrReplaceChild("armorLeftArm", CubeListBuilder.create().texOffs(24, 80).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.33F)).mirror(false), PartPose.offset(4.0F, 2.0F, 0.0F));
		return LayerDefinition.create(modelData, 128, 128);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		this.armorBody.copyFrom(this.body);
		this.armorBody.render(poseStack, vertexConsumer, light, overlay, red, green, blue, alpha);

		this.armorRightArm.copyFrom(this.rightArm);
		this.armorRightArm.render(poseStack, vertexConsumer, light, overlay, red, green, blue, alpha);

		this.armorLeftArm.copyFrom(this.leftArm);
		this.armorLeftArm.render(poseStack, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}