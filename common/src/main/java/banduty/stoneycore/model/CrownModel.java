package banduty.stoneycore.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class CrownModel extends HumanoidModel<LivingEntity> {
	private final ModelPart head;
	public CrownModel(ModelPart root) {
		super(root);

		this.head = root.getChild("head");
	}

    @Override
    protected Iterable<ModelPart> headParts() {
		return ImmutableList.of(this.head);
	}

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of();
	}

	public static LayerDefinition getTexturedModelData() {
		MeshDefinition modelData = HumanoidModel.createMesh(CubeDeformation.NONE, 0f);
        PartDefinition modelPartData = modelData.getRoot();
		PartDefinition head = modelPartData.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		head.addOrReplaceChild("crown_r1", CubeListBuilder.create().texOffs(64, 0).addBox(-5.5F, -9.0F, -5.75F, 11.0F, 5.0F, 11.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0436F, 0.0F, 0.0F));
		return LayerDefinition.create(modelData, 128, 64);
	}
}