package banduty.stoneycore.networking.packet;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AttackC2SPacket() {
    public static void handle(AttackC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                StaminaData.removeStamina(player, StoneyCore.getConfig().combatOptions().attackStaminaConstant() * WeightUtil.getCachedWeight(player));
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static AttackC2SPacket decode(FriendlyByteBuf buf) {
        return new AttackC2SPacket();
    }

    public static void encode(AttackC2SPacket msg, FriendlyByteBuf buf) {
        // No data to encode
    }
}