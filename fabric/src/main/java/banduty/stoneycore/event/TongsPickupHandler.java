package banduty.stoneycore.event;

import banduty.stoneycore.event.custom.PlayerPickupCallback;
import banduty.stoneycore.items.custom.hotiron.QuenchItem;
import banduty.stoneycore.items.custom.tongs.Tongs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TongsPickupHandler {

    public static void register() {
        PlayerPickupCallback.EVENT.register((player, itemEntity) -> {
            ItemStack pickedStack = itemEntity.getItem();

            // Only handle currently ignited QuenchItems.
            if (!(pickedStack.getItem() instanceof QuenchItem quenchItem)
                    || !quenchItem.isIgnited(pickedStack)) {
                return InteractionResult.PASS;
            }

            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            // Check main hand for empty tongs.
            if (mainHand.getItem() instanceof Tongs tongs
                    && tongs.getCapturedItem(mainHand).isEmpty()) {

                // Store one ignited item in the tongs.
                tongs.setCapturedItem(
                        mainHand,
                        pickedStack.copyWithCount(1)
                );

                pickedStack.shrink(1);

                // Remove the entity if the entire stack was captured.
                if (pickedStack.isEmpty()) {
                    itemEntity.discard();
                }

                // Prevent the captured item from also entering the inventory.
                removeQuenchItemFromInventory(player, pickedStack);

                return InteractionResult.FAIL;
            }

            // Check off hand for empty tongs.
            if (offHand.getItem() instanceof Tongs tongs
                    && tongs.getCapturedItem(offHand).isEmpty()) {

                // Store one ignited item in the offhand tongs.
                tongs.setCapturedItem(
                        offHand,
                        pickedStack.copyWithCount(1)
                );

                pickedStack.shrink(1);

                // Remove the entity if the entire stack was captured.
                if (pickedStack.isEmpty()) {
                    itemEntity.discard();
                }

                // Prevent the captured item from also entering the inventory.
                removeQuenchItemFromInventory(player, pickedStack);

                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });
    }

    private static void removeQuenchItemFromInventory(
            Player player,
            ItemStack quenchItem
    ) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (!(stack.getItem() instanceof QuenchItem stackQuenchItem)) {
                continue;
            }

            // Only remove an ignited QuenchItem.
            if (!stackQuenchItem.isIgnited(stack)) {
                continue;
            }

            if (ItemStack.isSameItemSameComponents(stack, quenchItem)) {
                player.getInventory().removeItem(i, 1);
                break;
            }
        }
    }
}