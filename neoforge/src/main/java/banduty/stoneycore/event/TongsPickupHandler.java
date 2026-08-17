package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.hotiron.QuenchItem;
import banduty.stoneycore.items.custom.tongs.Tongs;
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

        if (!(pickedStack.getItem() instanceof QuenchItem quenchItem)
                || !quenchItem.isIgnited(pickedStack)) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean handled = false;

        // Check main hand for empty tongs.
        if (mainHand.getItem() instanceof Tongs tongs
                && tongs.getCapturedItem(mainHand).isEmpty()) {

            tongs.setCapturedItem(
                    mainHand,
                    pickedStack.copyWithCount(1)
            );

            pickedStack.shrink(1);

            if (pickedStack.isEmpty()) {
                handled = true;
            }
        }

        // Check off hand for empty tongs.
        else if (offHand.getItem() instanceof Tongs tongs
                && tongs.getCapturedItem(offHand).isEmpty()) {

            tongs.setCapturedItem(
                    offHand,
                    pickedStack.copyWithCount(1)
            );

            pickedStack.shrink(1);

            if (pickedStack.isEmpty()) {
                handled = true;
            }
        }

        if (handled) {
            event.getItemEntity().discard();
            event.setCanPickup(TriState.FALSE);
        }
    }
}