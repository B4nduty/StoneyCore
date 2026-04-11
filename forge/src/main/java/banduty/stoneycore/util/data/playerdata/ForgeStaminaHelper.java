package banduty.stoneycore.util.data.playerdata;

import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.networking.packet.StaminaBlockedS2CPacket;
import banduty.stoneycore.networking.packet.StaminaValueS2CPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class ForgeStaminaHelper implements StaminaHelper {
    @Override
    public void syncStaminaBlocked(boolean blocked, ServerPlayer player) {
        ModMessages.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player), new StaminaBlockedS2CPacket(blocked)
        );
    }

    @Override
    public void syncStaminaValue(double stamina, ServerPlayer player) {
        ModMessages.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),  new StaminaValueS2CPacket(stamina));
    }
}
