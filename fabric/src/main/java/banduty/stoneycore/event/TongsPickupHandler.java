package banduty.stoneycore.event;

import banduty.stoneycore.event.custom.PlayerPickupCallback;
import banduty.stoneycore.items.custom.hotiron.HotIron;
import banduty.stoneycore.items.custom.tongs.Tongs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TongsPickupHandler {

    public static void register() {
        PlayerPickupCallback.EVENT.register((player, itemEntity) -> {
            ItemStack pickedStack = itemEntity.getItem();

            // Only handle HotIron items
            if (!(pickedStack.getItem() instanceof HotIron))
                return InteractionResult.PASS;

            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            // Check main hand for empty tongs
            if (mainHand.getItem() instanceof Tongs tongs && tongs.getCapturedItem(mainHand).isEmpty()) {
                // Store the captured item in the tongs
                tongs.setCapturedItem(mainHand, pickedStack.copyWithCount(1));

                pickedStack.shrink(1);

                // Remove the picked item from the world
                if (pickedStack.getCount() <= 0) itemEntity.discard();

                // Also remove from player inventory if it somehow got there
                removeHotIronFromInventory(player, pickedStack);

                // Return FAIL to completely cancel the pickup event
                return InteractionResult.FAIL;
            }

            // Check off hand for empty tongs
            if (offHand.getItem() instanceof Tongs tongs && tongs.getCapturedItem(mainHand).isEmpty()) {
                // Store the captured item in the tongs
                tongs.setCapturedItem(mainHand, pickedStack.copyWithCount(1));

                pickedStack.shrink(1);

                // Remove the picked item from the world
                if (pickedStack.getCount() <= 0) itemEntity.discard();

                // Also remove from player inventory if it somehow got there
                removeHotIronFromInventory(player, pickedStack);

                // Return FAIL to completely cancel the pickup event
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });
    }

    private static void removeHotIronFromInventory(Player player, ItemStack hotIron) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, hotIron)) {
                player.getInventory().removeItem(i, 1);
                break;
            }
        }
    }
}