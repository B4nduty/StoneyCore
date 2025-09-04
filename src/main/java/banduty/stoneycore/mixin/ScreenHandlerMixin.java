package banduty.stoneycore.mixin;

import banduty.stoneycore.items.armor.SCAccessoryItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void stoneycore$onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        ScreenHandler handler = (ScreenHandler)(Object)this;
        if (actionType == SlotActionType.PICKUP && button == 1) {
            if (slotIndex >= 0 && slotIndex < handler.slots.size()) {
                ItemStack stack = handler.getSlot(slotIndex).getStack();
                if (!stack.isEmpty() && stack.getItem() instanceof SCAccessoryItem scAccessoryItem && scAccessoryItem.openVisorModel(stack) != null) {
                    toggleOpenVisorMode(stack);
                    ci.cancel();
                }
            }
        }
    }

    @Unique
    private void toggleOpenVisorMode(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        boolean current = nbt.getBoolean("visor_open");
        nbt.putBoolean("visor_open", !current);
    }
}