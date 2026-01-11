package banduty.stoneycore.event;

import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
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

            if (StaminaData.isStaminaBlocked(dataSaver) || player.getAttributeValue(Services.ATTRIBUTES.getMaxStamina()) <= 0) {
                return;
            }

            ClientPlayNetworking.send(ModMessages.ATTACK_ID, PacketByteBufs.create());
        }
    }
}
