package banduty.stoneycore.mixin;

import banduty.stoneycore.items.SCItems;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FabricItem.class)
public interface FabricItemMixin {
    @Inject(method = "getRecipeRemainder", at = @At("HEAD"), cancellable = true)
    private void stoneycore$getRecipeRemainder(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.getItem() == SCItems.MANUSCRIPT) { cir.setReturnValue(stack); return; }
        if (stack.getItem() == SCItems.TONGS) { cir.setReturnValue(stack); return; }
        if (stack.getItem() == SCItems.SMITHING_HAMMER) {
            ItemStack newStack = stack.copy();
            newStack.setDamageValue(stack.getDamageValue() + 1);
            if (newStack.getDamageValue() >= newStack.getMaxDamage()) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
            cir.setReturnValue(newStack);
        }
    }
}
