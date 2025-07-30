package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCoreClient;
import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.network.PacketByteBuf;

public class ClientTickHandler implements ClientTickEvents.EndTick {
    @Override
    public void onEndTick(MinecraftClient minecraftClient) {
        StoneyCoreClient.LAND_TITLE_RENDERER.tick();

        ClientPlayerEntity clientPlayerEntity = minecraftClient.player;
        if (clientPlayerEntity == null) return;
        if (SCRangeWeaponUtil.getWeaponState(clientPlayerEntity.getMainHandStack()).isReloading()) {
            clientPlayerEntity.setVelocity(0, clientPlayerEntity.getVelocity().y, 0);
            clientPlayerEntity.velocityDirty = true;
        }
        if (clientPlayerEntity.hasVehicle() && (clientPlayerEntity.getVehicle() instanceof AbstractSiegeEntity || clientPlayerEntity.getVehicle() instanceof HorseEntity)) {
            float yaw = clientPlayerEntity.getYaw();
            float pitch = clientPlayerEntity.getPitch();
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeFloat(yaw);
            buf.writeFloat(pitch);
            ClientPlayNetworking.send(ModMessages.SIEGE_YAW_PITCH_C2S_ID, buf);
        }
    }
}
