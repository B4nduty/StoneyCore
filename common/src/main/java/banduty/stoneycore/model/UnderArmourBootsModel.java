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

public class UnderArmourBootsModel extends HumanoidModel<LivingEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation(StoneyCore.MOD_ID, "under_armour_boots"), "main");

	private final ModelPart armorRightBoot;
	private final ModelPart armorLeftBoot;

	public UnderArmourBootsModel(ModelPart root) {
		super(root);
		this.setAllVisible(false);
		this.armorRightBoot = root.getChild("armorRightBoot");
		this.armorLeftBoot = root.getChild("armorLeftBoot");
	}

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.armorRightBoot, this.armorLeftBoot);
	}

	public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = HumanoidModel.createMesh(CubeDeformation.NONE, 0f);
        PartDefinition modelPartData = modelData.getRoot();
		modelPartData.addOrReplaceChild("armorRightBoot", CubeListBuilder.create().texOffs(16, 111).addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.offset(-2.0F, 12.0F, 0.0F));

		modelPartData.addOrReplaceChild("armorLeftBoot", CubeListBuilder.create().texOffs(16, 111).mirror().addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.35F)).mirror(false), PartPose.offset(2.0F, 12.0F, 0.0F));
		return LayerDefinition.create(modelData, 128, 128);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		this.armorRightBoot.copyFrom(this.rightLeg);
		this.armorRightBoot.render(poseStack, vertexConsumer, light, overlay, red, green, blue, alpha);

		this.armorLeftBoot.copyFrom(this.leftLeg);
		this.armorLeftBoot.render(poseStack, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}