package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.SCAccessory;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleVisorC2SPacket() implements CustomPacketPayload {
    public static final Type<ToggleVisorC2SPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "toggle_visor")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleVisorC2SPacket> STREAM_CODEC =
            StreamCodec.unit(new ToggleVisorC2SPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public void handle(IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            ItemStack itemStack = player.getItemBySlot(EquipmentSlot.HEAD);
            for (ItemStack accessoryStack : SCUnderArmor.getAccessories(itemStack)) {
                if (accessoryStack.getItem() instanceof SCAccessory scAccessory && scAccessory.hasOpenVisor(accessoryStack)) {
                    boolean current = accessoryStack.getOrDefault(SCDataComponents.VISOR_OPEN.get(), false);
                    accessoryStack.set(SCDataComponents.VISOR_OPEN.get(), !current);
                }
            }
        }
    }
}