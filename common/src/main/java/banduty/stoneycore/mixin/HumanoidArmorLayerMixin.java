package banduty.stoneycore.mixin;

import banduty.stoneycore.client.render.UnderArmourRenderer;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> extends RenderLayer<T, M> {
    public HumanoidArmorLayerMixin(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Shadow
    protected abstract A getArmorModel(EquipmentSlot slot);

    @SuppressWarnings("unchecked")
    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onRenderArmorPiece(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }

            ItemStack itemStack = livingEntity.getItemBySlot(slot);
            A model = getArmorModel(slot);

            if (itemStack.getItem() instanceof SCUnderArmor) {
                this.getParentModel().copyPropertiesTo(model);
                HumanoidModel<LivingEntity> humanoidModel = (HumanoidModel<LivingEntity>) model;
                humanoidModel.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                UnderArmourRenderer.INSTANCE.renderBaseArmor(poseStack, buffer, itemStack, livingEntity, packedLight, humanoidModel,
                        limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
                UnderArmourRenderer.INSTANCE.renderAttachments(poseStack, buffer, itemStack, livingEntity, packedLight, humanoidModel,
                        limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
                ci.cancel();
            }
        }
    }
}