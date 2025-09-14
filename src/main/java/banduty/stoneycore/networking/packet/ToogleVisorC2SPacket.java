package banduty.stoneycore.networking.packet;

import banduty.stoneycore.items.armor.SCAccessoryItem;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
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
                        NbtCompound nbt = stack.getOrCreateNbt();
                        boolean current = nbt.getBoolean("visor_open");
                        nbt.putBoolean("visor_open", !current);
                    }
                }
            }
        });
    }
}
