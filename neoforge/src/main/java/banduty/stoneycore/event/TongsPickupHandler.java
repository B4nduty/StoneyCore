package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.Tongs;
import banduty.stoneycore.items.custom.hotiron.QuenchItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(
        modid = StoneyCore.MOD_ID,
        bus = EventBusSubscriber.Bus.GAME
)
public class TongsPickupHandler {

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        ItemStack pickedStack = event.getItemEntity().getItem();

        // Only handle currently ignited QuenchItems.
        if (!(pickedStack.getItem() instanceof QuenchItem quenchItem)
                || !quenchItem.isIgnited(pickedStack)) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        // Check main hand for empty tongs.
        if (mainHand.getItem() instanceof Tongs tongs
                && tongs.getCapturedItem(mainHand).isEmpty()) {

            tongs.setCapturedItem(
                    mainHand,
                    pickedStack.copyWithCount(1)
            );

            pickedStack.shrink(1);

            // The item entity still contains items if the original
            // stack contained more than one item.
            if (pickedStack.isEmpty()) {
                event.getItemEntity().discard();
            }

            // Prevent the captured item from being picked up normally.
            event.setCanPickup(TriState.FALSE);
            return;
        }

        // Check off hand for empty tongs.
        if (offHand.getItem() instanceof Tongs tongs
                && tongs.getCapturedItem(offHand).isEmpty()) {

            tongs.setCapturedItem(
                    offHand,
                    pickedStack.copyWithCount(1)
            );

            pickedStack.shrink(1);

            // The item entity still contains items if the original
            // stack contained more than one item.
            if (pickedStack.isEmpty()) {
                event.getItemEntity().discard();
            }

            // Prevent the captured item from being picked up normally.
            event.setCanPickup(TriState.FALSE);
        }
    }
}