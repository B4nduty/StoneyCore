package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.payload.AttackC2SPacket;
import banduty.stoneycore.util.data.entitydata.IEntityDataSaver;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.entitydata.StaminaData;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class PlayerAttackHitHandler {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();

        if (player.level().isClientSide() && player instanceof LocalPlayer localPlayer) {
            if (WeaponDefinitionsStorage.isMelee(localPlayer.getMainHandItem())) {
                IEntityDataSaver dataSaver = (IEntityDataSaver) localPlayer;

                if (localPlayer.isCreative()) return;

                if (StaminaData.isStaminaBlocked(dataSaver) || localPlayer.getAttributeValue(SCAttributes.MAX_STAMINA) <= 0) {
                    return;
                }

                PacketDistributor.sendToServer(new AttackC2SPacket());
            }
        }
    }
}