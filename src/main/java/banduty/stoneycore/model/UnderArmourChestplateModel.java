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
public class UnderArmourChestplateModel extends BipedEntityModel<LivingEntity> {
	private final ModelPart armorBody;
	private final ModelPart armorRightArm;
	private final ModelPart armorLeftArm;
	public UnderArmourChestplateModel(ModelPart root) {
		super(root);
		this.setVisible(false);
		this.armorBody = root.getChild("armorBody");
		this.armorRightArm = root.getChild("armorRightArm");
		this.armorLeftArm = root.getChild("armorLeftArm");
	}

	@Override
	protected Iterable<ModelPart> getHeadParts() {
		return ImmutableList.of(this.head);
	}

	@Override
	protected Iterable<ModelPart> getBodyParts() {
		return ImmutableList.of(this.armorBody, this.armorRightArm, this.armorLeftArm);
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = BipedEntityModel.getModelData(Dilation.NONE, 0f);
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("armorBody", ModelPartBuilder.create().uv(0, 80).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 16.0F, 4.0F, new Dilation(0.45F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));

		modelPartData.addChild("armorRightArm", ModelPartBuilder.create().uv(24, 80).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.33F)), ModelTransform.pivot(-4.0F, 2.0F, 0.0F));

		modelPartData.addChild("armorLeftArm", ModelPartBuilder.create().uv(24, 80).mirrored().cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new Dilation(0.33F)).mirrored(false), ModelTransform.pivot(4.0F, 2.0F, 0.0F));
		return TexturedModelData.of(modelData, 128, 128);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		this.armorBody.copyTransform(this.body);
		this.armorBody.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);

		this.armorRightArm.copyTransform(this.rightArm);
		this.armorRightArm.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);

		this.armorLeftArm.copyTransform(this.leftArm);
		this.armorLeftArm.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}