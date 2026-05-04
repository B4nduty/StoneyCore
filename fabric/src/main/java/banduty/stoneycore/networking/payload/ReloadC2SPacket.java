package banduty.stoneycore.networking.payload;

import banduty.stoneycore.networking.SCPayloads;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record ReloadC2SPacket() implements CustomPacketPayload {
    public static final Type<ReloadC2SPacket> ID = new Type<>(SCPayloads.RELOAD_PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ReloadC2SPacket> CODEC = StreamCodec.unit(new ReloadC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(ReloadC2SPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ItemStack itemStack = context.player().getMainHandItem();
            if (WeaponDefinitionsStorage.isRanged(itemStack) &&
                    SCRangeWeaponUtil.getAmmoRequirement(itemStack) != SCRangeWeaponUtil.AmmoRequirement.EMPTY) {
                if (!SCRangeWeaponUtil.getWeaponState(itemStack).isCharged()) {
                    SCRangeWeaponUtil.handleReload(context.player().level(), context.player(), itemStack);
                }
            }
        });
    }
}