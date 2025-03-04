package banduty.stoneycore.event;

import banduty.stoneycore.items.item.SCWeapon;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.playerdata.StaminaData;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerAttackHit implements BetterCombatClientEvents.PlayerAttackHit {
    @Override
    public void onPlayerAttackStart(ClientPlayerEntity player, AttackHand attackHand, List<Entity> list, @Nullable Entity entity) {
        if (player.getMainHandStack().getItem() instanceof SCWeapon) {
            IEntityDataSaver dataSaver = (IEntityDataSaver) player;

            if (player.isCreative()) return;

            if (StaminaData.getStamina(dataSaver) < 1 || StaminaData.isStaminaBlocked(dataSaver)) {
                return;
            }

            ClientPlayNetworking.send(ModMessages.ATTACK_ID, PacketByteBufs.create());
        }
    }
}
