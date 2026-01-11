package banduty.stoneycore.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class UnderArmourArmModel extends HumanoidModel<LivingEntity> {
	public final ModelPart armorRightArm;
	public final ModelPart armorLeftArm;

	public UnderArmourArmModel(ModelPart root) {
		super(root);
		this.armorRightArm = root.getChild("armorRightArm");
		this.armorLeftArm = root.getChild("armorLeftArm");
	}

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(this.armorRightArm, this.armorLeftArm);
	}

	public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = HumanoidModel.createMesh(CubeDeformation.NONE, 0f);
        PartDefinition modelPartData = modelData.getRoot();
		modelPartData.addOrReplaceChild("armorRightArm", CubeListBuilder.create().texOffs(24, 80).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.33F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

		modelPartData.addOrReplaceChild("armorLeftArm", CubeListBuilder.create().texOffs(24, 80).mirror().addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.33F)).mirror(false), PartPose.offset(5.0F, 2.0F, 0.0F));
		return LayerDefinition.create(modelData, 128, 128);
	}
}