package banduty.stoneycore.event;

import banduty.stoneycore.data.IEntityDataSaver;
import banduty.stoneycore.stamina.StaminaData;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class AttackCancelHandler implements ClientPreAttackCallback {
    @Override
    public boolean onClientPlayerPreAttack(Minecraft client, LocalPlayer player, int clickCount) {
        return StaminaData.isStaminaBlocked((IEntityDataSaver) player);
    }
}
