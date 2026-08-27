package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.data.IEntityDataSaver;
import banduty.stoneycore.stamina.StaminaData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class AttackCancelHandler {

    @SubscribeEvent
    public static void onClientAttack(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() && Minecraft.getInstance().player != null) {
            LocalPlayer player = Minecraft.getInstance().player;

            if (StaminaData.isStaminaBlocked((IEntityDataSaver) player)) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        }
    }
}