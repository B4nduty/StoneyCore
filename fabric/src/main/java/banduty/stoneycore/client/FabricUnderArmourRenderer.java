package banduty.stoneycore.client;

import banduty.stoneycore.client.render.UnderArmourRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class FabricUnderArmourRenderer implements ArmorRenderer {
    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, ItemStack stack,
                       LivingEntity entity, EquipmentSlot slot, int packedLight, HumanoidModel<LivingEntity> contextModel) {
        UnderArmourRenderer.INSTANCE.renderBaseArmor(poseStack, bufferSource, stack, packedLight, contextModel);

        UnderArmourRenderer.INSTANCE.renderAccessories(poseStack, bufferSource, stack, entity, packedLight, contextModel);
    }
}