package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.PDKeys;
import banduty.stoneycore.util.data.playerdata.SCAttributes;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerStaminaHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (NBTDataHelper.get((IEntityDataSaver) player, PDKeys.FIRST_JOIN, false)) {
                StaminaData.loadStamina(player);
                return;
            }

            player.displayClientMessage(Component.literal("""
                            §4StoneyCore §radds an overlay that makes a noise effect.
                            If you have §4epilepsy §rit is §lhighly recommended §rto §4disable Noise Effect.
                            """),
                    false);

            NBTDataHelper.set((IEntityDataSaver) player, PDKeys.FIRST_JOIN, true);

            StaminaData.setStamina(player, player.getAttributeValue(SCAttributes.MAX_STAMINA.get()));
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
        if (!(event.getOriginal() instanceof IEntityDataSaver oldSaver)) return;
        if (!(event.getEntity() instanceof IEntityDataSaver newSaver)) return;

        // Only copy if death OR always copy (recommended)
        event.getOriginal().reviveCaps(); // important if using capabilities

        CompoundTag oldData = oldSaver.stoneycore$getPersistentData();
        CompoundTag newData = newSaver.stoneycore$getPersistentData();

        newData.merge(oldData);

        event.getOriginal().invalidateCaps();
    }
}
