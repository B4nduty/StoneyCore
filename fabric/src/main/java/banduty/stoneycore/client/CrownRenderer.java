package banduty.stoneycore.client;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.model.CrownModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public class CrownRenderer implements ArmorRenderer {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    StoneyCore.MOD_ID,
                    "textures/entity/armor/crown.png");

    private CrownModel model;

    private CrownModel getModel() {
        if (model == null) {
            model = new CrownModel(
                    Minecraft.getInstance()
                            .getEntityModels()
                            .bakeLayer(CrownModel.LAYER_LOCATION)
            );
        }
        return model;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, ItemStack itemStack, LivingEntity entity, EquipmentSlot slot, int packedLight, HumanoidModel<LivingEntity> contextModel) {
        CrownModel crownModel = getModel();
        contextModel.copyPropertiesTo(model);

        VertexConsumer consumer =
                bufferSource.getBuffer(RenderType.armorCutoutNoCull(TEXTURE));

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
