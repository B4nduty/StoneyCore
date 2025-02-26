package banduty.stoneycore.mixin;

import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "getArmPose", at = @At("RETURN"), cancellable = true)
    private static void stoneycore$getArmPose(AbstractClientPlayerEntity player, Hand hand, CallbackInfoReturnable<BipedEntityModel.ArmPose> callbackInfoReturnable) {
        ItemStack itemStack = player.getMainHandStack();
        if (SCRangeWeaponUtil.getWeaponState(itemStack).isCharged() ||
                SCRangeWeaponUtil.getWeaponState(itemStack).isShooting() ||
                SCRangeWeaponUtil.getWeaponState(itemStack).isReloading()) {
            callbackInfoReturnable.setReturnValue(BipedEntityModel.ArmPose.BOW_AND_ARROW);
        }

        if (player.isUsingItem() && player.getActiveItem() == player.getMainHandStack() && player.getMainHandStack().isIn(SCTags.WEAPONS_SHIELD.getTag())) {
            callbackInfoReturnable.setReturnValue(BipedEntityModel.ArmPose.BLOCK);
        }
    }
}