package banduty.stoneycore.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

@Environment(EnvType.CLIENT)
public class UnderArmourLeggingsModel extends BipedEntityModel<LivingEntity> {
	private final ModelPart armorRightLeg;
	private final ModelPart armorLeftLeg;
	public UnderArmourLeggingsModel(ModelPart root) {
		super(root);
		this.setVisible(false);
		this.armorRightLeg = root.getChild("armorRightLeg");
		this.armorLeftLeg = root.getChild("armorLeftLeg");
	}

	@Override
	protected Iterable<ModelPart> getHeadParts() {
		return ImmutableList.of(this.head);
	}

	@Override
	protected Iterable<ModelPart> getBodyParts() {
		return ImmutableList.of(this.armorRightLeg, this.armorLeftLeg);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = BipedEntityModel.getModelData(Dilation.NONE, 0f);
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("armorRightLeg", ModelPartBuilder.create().uv(0, 105).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.3F)), ModelTransform.pivot(-2.0F, 12.0F, 0.0F));

		modelPartData.addChild("armorLeftLeg", ModelPartBuilder.create().uv(0, 105).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.3F)).mirrored(false), ModelTransform.pivot(2.0F, 12.0F, 0.0F));
		return TexturedModelData.of(modelData, 128, 128);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		this.armorRightLeg.copyTransform(this.rightLeg);
		this.armorRightLeg.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);

		this.armorLeftLeg.copyTransform(this.leftLeg);
		this.armorLeftLeg.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}