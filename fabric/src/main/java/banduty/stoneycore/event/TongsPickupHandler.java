package banduty.stoneycore.event;

import banduty.stoneycore.event.custom.PlayerPickupCallback;
import banduty.stoneycore.items.hotiron.HotIron;
import banduty.stoneycore.items.tongs.Tongs;
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
            if (mainHand.getItem() instanceof Tongs && !Tongs.hasTargetStack(mainHand)) {
                // Store the hot iron in the tongs
                Tongs.setTargetStack(mainHand, pickedStack.copyWithCount(1));

                // Remove the picked item from the world
                itemEntity.discard();

                // Also remove from player inventory if it somehow got there
                removeHotIronFromInventory(player, pickedStack);

                // Return FAIL to completely cancel the pickup event
                return InteractionResult.FAIL;
            }

            // Check off hand for empty tongs
            if (offHand.getItem() instanceof Tongs && !Tongs.hasTargetStack(offHand)) {
                // Store the hot iron in the tongs
                Tongs.setTargetStack(offHand, pickedStack.copyWithCount(1));

                // Remove the picked item from the world
                itemEntity.discard();

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
            if (ItemStack.isSameItemSameTags(stack, hotIron)) {
                player.getInventory().removeItem(i, 1);
                break;
            }
        }
    }
}