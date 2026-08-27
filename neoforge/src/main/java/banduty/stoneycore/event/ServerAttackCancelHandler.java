package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.data.IEntityDataSaver;
import banduty.stoneycore.stamina.StaminaData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ServerAttackCancelHandler {

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (StaminaData.isStaminaBlocked((IEntityDataSaver) player)) {
            event.setCanceled(true);
        }
    }
}