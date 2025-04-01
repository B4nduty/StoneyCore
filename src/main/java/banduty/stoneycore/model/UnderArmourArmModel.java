package banduty.stoneycore.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class UnderArmourArmModel extends BipedEntityModel<LivingEntity> {
	public final ModelPart armorRightArm;
	public final ModelPart armorLeftArm;

	public UnderArmourArmModel(ModelPart root) {
		super(root);
		this.armorRightArm = root.getChild("armorRightArm");
		this.armorLeftArm = root.getChild("armorLeftArm");
	}

	@Override
	protected Iterable<ModelPart> getHeadParts() {
		return ImmutableList.of(this.head);
	}

	@Override
	protected Iterable<ModelPart> getBodyParts() {
		return ImmutableList.of(this.armorRightArm, this.armorLeftArm);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = BipedEntityModel.getModelData(Dilation.NONE, 0f);
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("armorRightArm", ModelPartBuilder.create().uv(24, 80).cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.33F)), ModelTransform.pivot(-7.0F, 2.0F, 0.0F));

		modelPartData.addChild("armorLeftArm", ModelPartBuilder.create().uv(24, 80).mirrored().cuboid(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.33F)).mirrored(false), ModelTransform.pivot(7.0F, 2.0F, 0.0F));
		return TexturedModelData.of(modelData, 128, 128);
	}
}