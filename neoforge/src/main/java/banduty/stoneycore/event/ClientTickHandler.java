package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.StoneyCoreClient;
import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import banduty.stoneycore.networking.payload.SiegeYawC2SPacket;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.animal.horse.Horse;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

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

            PacketDistributor.sendToServer(new SiegeYawC2SPacket(yaw, pitch));
        }
    }
}