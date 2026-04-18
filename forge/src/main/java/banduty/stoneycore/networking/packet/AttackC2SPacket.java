package banduty.stoneycore.networking.packet;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.combat.melee.SCDamageType;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AttackC2SPacket() {
    public static void handle(AttackC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack mainHandStack = player.getMainHandItem();
                SCDamageType damageType = SCDamageType.determine(mainHandStack, player);
                double stamina = StoneyCore.getConfig().combatOptions().attackStaminaConstant();
                if (damageType == SCDamageType.SLASHING)  stamina *= 0.5;
                StaminaData.removeStamina(player, stamina * WeightUtil.getCachedWeight(player));
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