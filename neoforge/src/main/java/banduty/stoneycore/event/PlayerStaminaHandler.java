package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.data.entitydata.IEntityDataSaver;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.entitydata.StaminaData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PlayerStaminaHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (((IEntityDataSaver) player).stoneycore$getPersistentData().getBoolean("firstJoin")) {
                StaminaData.loadStamina(player);
                return;
            }

            player.displayClientMessage(Component.literal("""
                            §4StoneyCore §radds an overlay that makes a noise effect.
                            If you have §4epilepsy §rit is §lhighly recommended §rto §4disable Noise Effect.
                            """),
                    false);

            ((IEntityDataSaver) player).stoneycore$getPersistentData().putBoolean("firstJoin", true);

            StaminaData.setStamina(player, player.getAttributeValue(SCAttributes.MAX_STAMINA));
        }
    }


    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            IEntityDataSaver saver = (IEntityDataSaver) player;
            StaminaData.saveStamina(saver, StaminaData.getStamina(player));
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        var oldPlayer = event.getOriginal();
        var newPlayer = event.getEntity();

        if (oldPlayer instanceof IEntityDataSaver oldSaver && newPlayer instanceof IEntityDataSaver newSaver) {
            CompoundTag oldData = oldSaver.stoneycore$getPersistentData();
            // We create a copy to avoid referencing the same object in the old player's memory
            newSaver.stoneycore$getPersistentData().merge(oldData.copy());
        }
    }
}
