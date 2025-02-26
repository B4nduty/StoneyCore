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
public class UnderArmourArmModel extends BipedEntityModel<LivingEntity> {
	public final ModelPart armorRightArm;
	public UnderArmourArmModel(ModelPart root) {
		super(root);
		this.armorRightArm = root.getChild("armorRightArm");
	}

	@Override
	protected Iterable<ModelPart> getHeadParts() {
		return ImmutableList.of(this.head);
	}

	@Override
	protected Iterable<ModelPart> getBodyParts() {
		return ImmutableList.of(this.armorRightArm);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = BipedEntityModel.getModelData(Dilation.NONE, 0f);
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("armorRightArm", ModelPartBuilder.create().uv(24, 80).cuboid(-5.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.33F)), ModelTransform.pivot(-4.0F, 2.0F, 0.0F));

		return TexturedModelData.of(modelData, 128, 128);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		this.armorRightArm.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}