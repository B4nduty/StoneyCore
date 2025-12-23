package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCoreClient;
import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.animal.horse.Horse;

public class ClientTickHandler implements ClientTickEvents.EndTick {
    @Override
    public void onEndTick(Minecraft minecraft) {
        StoneyCoreClient.LAND_TITLE_RENDERER.tick();

        LocalPlayer localPlayer = minecraft.player;
        if (localPlayer == null) return;
        if (SCRangeWeaponUtil.getWeaponState(localPlayer.getMainHandItem()).isReloading()) {
            localPlayer.setDeltaMovement(0, localPlayer.getDeltaMovement().y, 0);
            localPlayer.hasImpulse = true;
        }
        if (localPlayer.isPassenger() && (localPlayer.getVehicle() instanceof AbstractSiegeEntity || localPlayer.getVehicle() instanceof Horse)) {
            float yaw = localPlayer.getYRot();
            float pitch = localPlayer.getXRot();
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            ClientPlayNetworking.send(ModMessages.SIEGE_YAW_PITCH_C2S_ID, buf);
        }
    }
}
