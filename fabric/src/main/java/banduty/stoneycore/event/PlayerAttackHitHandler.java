package banduty.stoneycore.event;

import banduty.stoneycore.networking.payload.AttackC2SPacket;
import banduty.stoneycore.util.data.entitydata.IEntityDataSaver;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.entitydata.StaminaData;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerAttackHitHandler implements BetterCombatClientEvents.PlayerAttackHit {
    @Override
    public void onPlayerAttackStart(LocalPlayer player, AttackHand attackHand, List<Entity> list, @Nullable Entity entity) {
        if (WeaponDefinitionsStorage.isMelee(player.getMainHandItem())) {
            IEntityDataSaver dataSaver = (IEntityDataSaver) player;

            if (player.isCreative()) return;

            if (StaminaData.isStaminaBlocked(dataSaver) || player.getAttributeValue(SCAttributes.MAX_STAMINA) <= 0) {
                return;
            }

            ClientPlayNetworking.send(new AttackC2SPacket());
        }
    }
}
