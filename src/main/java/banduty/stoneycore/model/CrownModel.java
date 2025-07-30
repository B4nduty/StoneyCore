package banduty.stoneycore.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;

public class CrownModel extends BipedEntityModel<LivingEntity> {
	private final ModelPart head;
	public CrownModel(ModelPart root) {
		super(root);

		this.head = root.getChild("head");
	}

	@Override
	protected Iterable<ModelPart> getHeadParts() {
		return ImmutableList.of(this.head);
	}

	@Override
	protected Iterable<ModelPart> getBodyParts() {
		return ImmutableList.of();
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = BipedEntityModel.getModelData(Dilation.NONE, 0f);
		ModelPartData modelPartData = modelData.getRoot();
		ModelPartData head = modelPartData.addChild("head", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

		head.addChild("crown_r1", ModelPartBuilder.create().uv(64, 0).cuboid(-5.5F, -9.0F, -5.75F, 11.0F, 5.0F, 11.0F, new Dilation(-0.4F)), ModelTransform.of(0.0F, 0.0F, 0.0F, -0.0436F, 0.0F, 0.0F));
		return TexturedModelData.of(modelData, 128, 64);
	}
}