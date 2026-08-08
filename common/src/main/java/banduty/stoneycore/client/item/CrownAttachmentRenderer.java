package banduty.stoneycore.client.item;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.client.render.armor.ArmorAttachmentPosition;
import banduty.stoneycore.client.render.armor.ArmorAttachmentRenderer;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorContents;
import banduty.stoneycore.model.CrownModel;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import static banduty.stoneycore.util.SCInventoryItemFinder.findUnderArmor;

public class CrownAttachmentRenderer implements ArmorAttachmentRenderer {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    StoneyCore.MOD_ID,
                    "textures/entity/armor/crown.png");

    private final CrownModel crownModel;

    public CrownAttachmentRenderer() {
        this.crownModel = new CrownModel(CrownModel.getTexturedModelData().bakeRoot());
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

        poseStack.pushPose();

        crownModel.resetModel();

        ItemStack target = findUnderArmor(entity, ArmorItem.Type.HELMET);

        if (!target.isEmpty() && (target.getItem() instanceof SCUnderArmor)) {
            UnderArmorContents contents = target.getOrDefault(SCDataComponents.UNDER_ARMOR_CONTENTS.get(),
                    UnderArmorContents.EMPTY);

            ArmorAttachmentPosition.applyPositionAndRotation(contents, itemStack, entity, itemStack.getItem(),
                    crownModel::moveModel, crownModel::rotateModel);
        }

        VertexConsumer consumer = bufferSource.getBuffer(
                RenderType.armorCutoutNoCull(TEXTURE)
        );

        int color = DyedItemColor.getOrDefault(itemStack, -1);

        crownModel.renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                color
        );

        poseStack.popPose();
    }
}