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

	public class UnderArmourHelmetModel extends HumanoidModel<LivingEntity> {
		public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation(StoneyCore.MOD_ID, "under_armour_helmet"), "main");

		private final ModelPart armorHead;
		public UnderArmourHelmetModel(ModelPart root) {
			super(root);
			this.setAllVisible(false);
			this.armorHead = root.getChild("armorHead");
		}

        @Override
        protected Iterable<ModelPart> headParts() {
			return ImmutableList.of(this.armorHead);
		}

		@Override
        protected Iterable<ModelPart> bodyParts() {
			return ImmutableList.of();
		}

		public static LayerDefinition getTexturedModelData() {
            MeshDefinition modelData = HumanoidModel.createMesh(CubeDeformation.NONE, 0f);
            PartDefinition modelPartData = modelData.getRoot();
			modelPartData.addOrReplaceChild("armorHead", CubeListBuilder.create().texOffs(32, 64).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.55F)), PartPose.offset(0.0F, 0.0F, 0.0F));
			return LayerDefinition.create(modelData, 128, 128);
		}

        @Override
		public void renderToBuffer(PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
			this.armorHead.copyFrom(this.head);
			this.armorHead.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
		}
	}