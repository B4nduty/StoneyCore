package banduty.stoneycore.event;

import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.networking.packet.AttackC2SPacket;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.SCAttributes;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = banduty.stoneycore.StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlayerAttackHitHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();

        if (player.level().isClientSide() && player instanceof LocalPlayer localPlayer) {
            if (WeaponDefinitionsStorage.isMelee(localPlayer.getMainHandItem())) {
                IEntityDataSaver dataSaver = (IEntityDataSaver) localPlayer;

                if (localPlayer.isCreative()) return;

                if (StaminaData.isStaminaBlocked(dataSaver) || localPlayer.getAttributeValue(SCAttributes.MAX_STAMINA.get()) <= 0) {
                    return;
                }

                ModMessages.CHANNEL.sendToServer(new AttackC2SPacket());
            }
        }
    }
}