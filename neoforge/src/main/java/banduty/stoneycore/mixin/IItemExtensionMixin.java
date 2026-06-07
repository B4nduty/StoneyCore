package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IItemExtension.class)
public interface IItemExtensionMixin {

    @Inject(method = "getCraftingRemainingItem", at = @At("HEAD"), cancellable = true)
    private void stoneycore$getCraftingRemainingItem(ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        if (itemStack.getItem() == SCItems.MANUSCRIPT) { cir.setReturnValue(itemStack); return; }
        if (itemStack.getItem() == SCItems.TONGS) { cir.setReturnValue(itemStack); return; }
        if (itemStack.getItem() == SCItems.SMITHING_HAMMER) {
            ItemStack newStack = itemStack.copy();
            newStack.setDamageValue(itemStack.getDamageValue() + 1);
            if (newStack.getDamageValue() >= newStack.getMaxDamage()) {
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
            cir.setReturnValue(newStack);
        }
    }

    @Inject(method = "getArmorTexture", at = @At("HEAD"), cancellable = true)
    private void getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel, CallbackInfoReturnable<ResourceLocation> cir) {
        if (stack.getItem() instanceof SCUnderArmor scUnderArmor) {

            var materialKey = scUnderArmor.getMaterial().unwrapKey().orElse(null);
            if (materialKey != null) {
                ResourceLocation customTexture = ResourceLocation.fromNamespaceAndPath(
                        StoneyCore.MOD_ID,
                        "textures/models/armor/a_layer_1.png"
                );

                cir.setReturnValue(customTexture);
            }
        }
    }
}
