package banduty.stoneycore.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;


public interface PlayerPickupCallback {
    InteractionResult onPickup(Player player, ItemEntity itemEntity);

    Event<PlayerPickupCallback> EVENT = EventFactory.createArrayBacked(PlayerPickupCallback.class,
            (listeners) -> (player, itemEntity) -> {
                for (PlayerPickupCallback listener : listeners) {
                    InteractionResult result = listener.onPickup(player, itemEntity);
                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }
                return InteractionResult.PASS;
            });
}