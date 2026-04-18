package banduty.stoneycore.networking.packet;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.combat.melee.SCDamageType;
import banduty.stoneycore.util.WeightUtil;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public class AttackC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
                               FriendlyByteBuf buf, PacketSender responseSender) {
        ItemStack mainHandStack = player.getMainHandItem();
        SCDamageType damageType = SCDamageType.determine(mainHandStack, player);
        double stamina = StoneyCore.getConfig().combatOptions().attackStaminaConstant();
        if (damageType == SCDamageType.SLASHING)  stamina *= 0.5;
        StaminaData.removeStamina(player, stamina * WeightUtil.getCachedWeight(player));
    }
}
