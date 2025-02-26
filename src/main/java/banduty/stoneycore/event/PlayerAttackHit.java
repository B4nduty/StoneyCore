package banduty.stoneycore.event;

import banduty.stoneycore.items.item.SCWeapon;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
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
            var persistentData = dataSaver.stoneycore$getPersistentData();

            if (player.isCreative()) return;

            boolean staminaBlocked = persistentData.getBoolean("stamina_blocked");
            int stamina = persistentData.getInt("stamina_int");
            int staminaCost = 10;

            if (stamina < staminaCost || staminaBlocked) {
                return;
            }

            ClientPlayNetworking.send(ModMessages.ATTACK_ID, PacketByteBufs.create());
        }
    }
}
