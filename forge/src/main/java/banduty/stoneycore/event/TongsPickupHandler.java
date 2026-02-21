package banduty.stoneycore.event;

import banduty.stoneycore.items.hotiron.HotIron;
import banduty.stoneycore.items.tongs.Tongs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TongsPickupHandler {

    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {

        Player player = event.getEntity();
        ItemStack pickedStack = event.getItem().getItem();

        if (!(pickedStack.getItem() instanceof HotIron))
            return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean handled = false;

        // Check main hand for empty tongs
        if (mainHand.getItem() instanceof Tongs && !Tongs.hasTargetStack(mainHand)) {
            // Store the hot iron in the tongs
            Tongs.setTargetStack(mainHand, pickedStack.copyWithCount(1));
            handled = true;
        }
        // Check off hand for empty tongs (only if main hand wasn't used)
        else if (offHand.getItem() instanceof Tongs && !Tongs.hasTargetStack(offHand)) {
            // Store the hot iron in the tongs
            Tongs.setTargetStack(offHand, pickedStack.copyWithCount(1));
            handled = true;
        }

        // If we stored the item in tongs, remove it from the world and cancel pickup
        if (handled) {
            // Remove the item entity from the world
            event.getItem().discard();

            // Cancel the event so the item doesn't go to inventory
            event.setCanceled(true);
        }
    }
}