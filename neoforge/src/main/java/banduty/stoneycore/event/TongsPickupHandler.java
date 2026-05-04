package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.hotiron.HotIron;
import banduty.stoneycore.items.custom.tongs.Tongs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class TongsPickupHandler {

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {

        Player player = event.getPlayer();
        ItemStack pickedStack = event.getItemEntity().getItem();

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
            event.getItemEntity().discard();

            // Cancel the event so the item doesn't go to inventory
            event.setCanPickup(TriState.FALSE);
        }
    }
}