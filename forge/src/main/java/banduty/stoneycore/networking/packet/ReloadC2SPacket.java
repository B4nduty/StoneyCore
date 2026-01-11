package banduty.stoneycore.networking.packet;

import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ReloadC2SPacket() {
    public static void handle(ReloadC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack itemStack = player.getMainHandItem();

                if (WeaponDefinitionsStorage.isRanged(itemStack) && SCRangeWeaponUtil.getAmmoRequirement(itemStack) != SCRangeWeaponUtil.AmmoRequirement.EMPTY) {
                    if (!SCRangeWeaponUtil.getWeaponState(itemStack).isCharged()) {
                        SCRangeWeaponUtil.handleReload(player.level(), player, itemStack);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static ReloadC2SPacket decode(FriendlyByteBuf buf) {
        return new ReloadC2SPacket();
    }

    public static void encode(ReloadC2SPacket msg, FriendlyByteBuf buf) {
        // No data to encode
    }
}