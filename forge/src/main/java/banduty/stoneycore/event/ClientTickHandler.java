package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.StoneyCoreClient;
import banduty.stoneycore.entity.custom.AbstractSiegeEntity;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.networking.packet.SiegeYawC2SPacket;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientTickHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

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

            ModMessages.CHANNEL.send(PacketDistributor.SERVER.noArg(), new SiegeYawC2SPacket(yaw, pitch));
        }
    }
}