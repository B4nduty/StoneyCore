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
	public class UnderArmourHelmetModel extends BipedEntityModel<LivingEntity> {
		public final ModelPart head;
		public final ModelPart hat;
		public final ModelPart body;
		public final ModelPart rightArm;
		public final ModelPart leftArm;
		public final ModelPart rightLeg;
		public final ModelPart leftLeg;
		private final ModelPart armorHead;
		public UnderArmourHelmetModel(ModelPart root) {
			super(root);
			this.setVisible(false);
			this.head = root.getChild("head");
			this.hat = root.getChild("hat");
			this.body = root.getChild("body");
			this.rightArm = root.getChild("right_arm");
			this.leftArm = root.getChild("left_arm");
			this.rightLeg = root.getChild("right_leg");
			this.leftLeg = root.getChild("left_leg");
			this.armorHead = root.getChild("armorHead");
		}

		@Override
		protected Iterable<ModelPart> getHeadParts() {
			return ImmutableList.of(this.head);
		}

		@Override
		protected Iterable<ModelPart> getBodyParts() {
			return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat,
					this.armorHead);
		}

		public static TexturedModelData getTexturedModelData() {
			ModelData modelData = BipedEntityModel.getModelData(Dilation.NONE, 0f);
			ModelPartData modelPartData = modelData.getRoot();
			modelPartData.addChild("armorHead", ModelPartBuilder.create().uv(32, 64).cuboid(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.55F)), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
			return TexturedModelData.of(modelData, 128, 128);
		}

		@Override
		public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
			this.armorHead.copyTransform(this.head);
			this.armorHead.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		}
	}