package banduty.stoneycore.networking.packet;

import banduty.stoneycore.items.item.SCRangeWeapon;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class ReloadC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
            ItemStack itemStack = player.getMainHandStack();

            if (itemStack.getItem() instanceof SCRangeWeapon khRangeWeapons && khRangeWeapons.ammoRequirement() != null) {
                if (!SCRangeWeaponUtil.getWeaponState(itemStack).isCharged()) itemStack.getOrCreateNbt().putBoolean("sc_recharge", true);
            }
        });
    }

}