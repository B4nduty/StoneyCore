package banduty.stoneycore.mixin;

import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "getArmPose", at = @At("RETURN"), cancellable = true)
    private static void stoneycore$getArmPose(AbstractClientPlayer abstractClientPlayer, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> callbackInfoReturnable) {
        ItemStack itemStack = abstractClientPlayer.getMainHandItem();
        if (SCRangeWeaponUtil.getWeaponState(itemStack).isCharged() ||
                SCRangeWeaponUtil.getWeaponState(itemStack).isShooting() ||
                SCRangeWeaponUtil.getWeaponState(itemStack).isReloading()) {
            callbackInfoReturnable.setReturnValue(HumanoidModel.ArmPose.BOW_AND_ARROW);
        }

        if (abstractClientPlayer.isUsingItem() && abstractClientPlayer.getUseItem() == abstractClientPlayer.getMainHandItem() && abstractClientPlayer.getMainHandItem().is(SCTags.WEAPONS_SHIELD.getTag())) {
            callbackInfoReturnable.setReturnValue(HumanoidModel.ArmPose.BLOCK);
        }
    }
}