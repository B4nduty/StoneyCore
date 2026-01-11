package banduty.stoneycore.networking.packet;

import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public class ReloadC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
                               FriendlyByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            ItemStack itemStack = player.getMainHandItem();

            if (WeaponDefinitionsStorage.isRanged(itemStack) && SCRangeWeaponUtil.getAmmoRequirement(itemStack) != SCRangeWeaponUtil.AmmoRequirement.EMPTY) {
                if (!SCRangeWeaponUtil.getWeaponState(itemStack).isCharged()) {
                    SCRangeWeaponUtil.handleReload(player.level(), player, itemStack);
                }
            }
        });
    }

}