package banduty.stoneycore.client;

import banduty.stoneycore.client.render.UnderArmourRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class NeoForgeUnderArmourRenderer implements IClientItemExtensions {

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                           EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        if (itemStack.getItem() instanceof ArmorItem armorItem) {
            HumanoidModel<LivingEntity> customModel = UnderArmourRenderer.INSTANCE.getModel(armorItem);
            if (customModel != null) {
                return customModel;
            }
        }
        return original;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setupModelAnimations(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot,
                                     Model model, float limbSwing, float limbSwingAmount, float partialTick,
                                     float ageInTicks, float netHeadYaw, float headPitch) {

        if (model instanceof HumanoidModel<?> humanoidModel) {
            PoseStack poseStack = new PoseStack();
            MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            int packedLight = Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(livingEntity, partialTick);

            UnderArmourRenderer.INSTANCE.renderAccessories(poseStack, bufferSource, itemStack, livingEntity, packedLight, (HumanoidModel<LivingEntity>) humanoidModel);
        }
    }
}