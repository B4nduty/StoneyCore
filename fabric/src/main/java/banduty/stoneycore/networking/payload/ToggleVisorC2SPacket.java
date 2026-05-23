package banduty.stoneycore.networking.payload;

import banduty.stoneycore.items.custom.armor.ArmorAttachment;
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
        for (ItemStack armorAttachment : SCUnderArmor.getArmorAttachments(itemStack)) {
            if (armorAttachment.getItem() instanceof ArmorAttachment armorAttachmentI && armorAttachmentI.hasOpenVisor(armorAttachment)) {
                boolean current = armorAttachment.getOrDefault(SCDataComponents.VISOR_OPEN.get(), false);
                armorAttachment.set(SCDataComponents.VISOR_OPEN.get(), !current);
            }
        }
    }
}