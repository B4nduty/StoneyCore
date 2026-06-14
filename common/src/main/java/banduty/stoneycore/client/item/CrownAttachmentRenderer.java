package banduty.stoneycore.client.item;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.client.render.ArmorAttachmentRenderer;
import banduty.stoneycore.model.CrownModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public class CrownAttachmentRenderer implements ArmorAttachmentRenderer {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    StoneyCore.MOD_ID,
                    "textures/entity/armor/crown.png");

    private final CrownModel crownModel;

    public CrownAttachmentRenderer() {
        this.crownModel = new CrownModel(
                Minecraft.getInstance()
                        .getEntityModels()
                        .bakeLayer(CrownModel.LAYER_LOCATION)
        );
    }

    @Override
    public void render(PoseStack poseStack,
                       MultiBufferSource bufferSource,
                       int packedLight,
                       LivingEntity entity,
                       ItemStack itemStack,
                       HumanoidModel<LivingEntity> contextModel,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTicks,
                       float ageInTicks,
                       float netHeadYaw,
                       float headPitch) {

        contextModel.copyPropertiesTo(crownModel);

        crownModel.setupAnim(entity, entity.walkAnimation.position(), entity.walkAnimation.speed(),
                (float) entity.tickCount + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true),
                entity.getYHeadRot() - entity.yBodyRot,
                entity.getXRot());

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(TEXTURE));

        int color = DyedItemColor.getOrDefault(itemStack, -1);

        crownModel.renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                color
        );
    }
}
