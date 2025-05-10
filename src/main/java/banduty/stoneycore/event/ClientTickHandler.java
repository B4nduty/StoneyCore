package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class ClientTickHandler implements ClientTickEvents.StartTick {
    @Override
    public void onStartTick(MinecraftClient client) {
        PlayerEntity playerEntity = client.player;
        if (playerEntity != null) {
            if (SCRangeWeaponUtil.getWeaponState(playerEntity.getMainHandStack()).isReloading() && StoneyCore.getConfig().getRealisticCombat()) {
                playerEntity.setVelocity(0, playerEntity.getVelocity().y, 0);
            }
        }
    }
}