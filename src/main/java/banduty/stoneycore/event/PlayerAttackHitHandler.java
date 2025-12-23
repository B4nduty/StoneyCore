package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.StaminaData;
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
        if (WeaponDefinitionsLoader.isMelee(player.getMainHandItem())) {
            IEntityDataSaver dataSaver = (IEntityDataSaver) player;

            if (player.isCreative()) return;

            if (StaminaData.isStaminaBlocked(dataSaver) || player.getAttributeValue(StoneyCore.MAX_STAMINA.get()) <= 0) {
                return;
            }

            ClientPlayNetworking.send(ModMessages.ATTACK_ID, PacketByteBufs.create());
        }
    }
}
