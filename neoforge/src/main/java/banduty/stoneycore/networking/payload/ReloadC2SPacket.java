package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ReloadC2SPacket() implements CustomPacketPayload {
    public static final Type<ReloadC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "reload_packet")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ReloadC2SPacket> STREAM_CODEC =
            StreamCodec.unit(new ReloadC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ItemStack itemStack = player.getMainHandItem();
            if (banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage.isRanged(itemStack) &&
                    banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil.getAmmoRequirement(itemStack) !=
                            banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil.AmmoRequirement.EMPTY) {
                if (!banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil.getWeaponState(itemStack).isCharged()) {
                    banduty.stoneycore.util.weaponutil.SCRangeWeaponUtil.handleReload(player.level(), player, itemStack);
                }
            }
        }
    }
}