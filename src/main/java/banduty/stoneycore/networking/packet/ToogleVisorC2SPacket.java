package banduty.stoneycore.networking.packet;

import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public class ToogleVisorC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler,
                               FriendlyByteBuf buf, PacketSender responseSender) {
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
