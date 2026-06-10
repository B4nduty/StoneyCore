package banduty.stoneycore.mixin;

import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import banduty.stoneycore.util.definitionsloader.ArmorAttachmentDefinitionsStorage;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnderMan.class)
public class EnderManMixin {
    @Inject(method = "isLookingAtMe", at = @At("HEAD"), cancellable = true)
    private void makeStaringSafe(Player player, CallbackInfoReturnable<Boolean> cir) {
        ItemStack helmet = player.getInventory().getArmor(3);

        if (helmet.isEmpty() || !(helmet.getItem() instanceof SCUnderArmor)) {
            return;
        }

        List<ItemStack> attachments = SCUnderArmor.getArmorAttachments(helmet);
        if (attachments == null || attachments.isEmpty()) {
            return;
        }

        for (ItemStack attachment : attachments) {
            var data = ArmorAttachmentDefinitionsStorage.getData(attachment);
            if (!(data.visoredHelmet().getPath().isEmpty() || data.visoredHelmet().getPath().equals("empty"))) {
                if (!attachment.getOrDefault(SCDataComponents.VISOR_OPEN.get(), false)) {
                    cir.setReturnValue(false);
                    return;
                }
            }
        }
    }
}
