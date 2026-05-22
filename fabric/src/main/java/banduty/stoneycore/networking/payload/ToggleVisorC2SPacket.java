package banduty.stoneycore.networking.payload;

import banduty.stoneycore.items.custom.armor.SCAccessory;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.networking.SCPayloads;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public record ToggleVisorC2SPacket() implements CustomPacketPayload {
    public static final Type<ToggleVisorC2SPacket> ID = new Type<>(SCPayloads.TOGGLE_VISOR_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleVisorC2SPacket> CODEC = StreamCodec.unit(new ToggleVisorC2SPacket());

    @Override public Type<? extends CustomPacketPayload> type() { return ID; }

    public void handle(ServerPlayNetworking.Context context) {
        ItemStack itemStack = context.player().getItemBySlot(EquipmentSlot.HEAD);
        for (ItemStack accessoryStack : SCUnderArmor.getAccessories(itemStack)) {
            if (accessoryStack.getItem() instanceof SCAccessory scAccessory && scAccessory.hasOpenVisor(accessoryStack)) {
                boolean current = accessoryStack.getOrDefault(SCDataComponents.VISOR_OPEN.get(), false);
                accessoryStack.set(SCDataComponents.VISOR_OPEN.get(), !current);
            }
        }
    }
}