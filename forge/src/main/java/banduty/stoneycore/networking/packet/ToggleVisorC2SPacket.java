package banduty.stoneycore.networking.packet;

import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ToggleVisorC2SPacket() {

    public static void handle(ToggleVisorC2SPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                if (AccessoriesCapability.getOptionally(player).isPresent()) {
                    for (SlotEntryReference equipped : AccessoriesCapability.get(player).getAllEquipped()) {
                        ItemStack stack = equipped.stack();
                        if (!stack.isEmpty() && stack.getItem() instanceof SCAccessoryItem scAccessoryItem &&
                                scAccessoryItem.hasOpenVisor(stack)) {
                            boolean current = NBTDataHelper.get(stack, INBTKeys.VISOR_OPEN, false);
                            NBTDataHelper.set(stack, INBTKeys.VISOR_OPEN, !current);
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static ToggleVisorC2SPacket decode(FriendlyByteBuf buf) {
        return new ToggleVisorC2SPacket();
    }

    public static void encode(ToggleVisorC2SPacket msg, FriendlyByteBuf buf) {
        // No data to encode
    }
}