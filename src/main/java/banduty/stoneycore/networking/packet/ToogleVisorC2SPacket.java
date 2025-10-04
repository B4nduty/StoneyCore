package banduty.stoneycore.networking.packet;

import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class ToogleVisorC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        server.execute(() -> {
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
        });
    }
}
