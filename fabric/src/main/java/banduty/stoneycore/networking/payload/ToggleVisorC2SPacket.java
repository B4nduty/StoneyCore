package banduty.stoneycore.networking.payload;

import banduty.stoneycore.items.custom.armor.SCAccessoryItem;
import banduty.stoneycore.networking.SCPayloads;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record ToggleVisorC2SPacket() implements CustomPacketPayload {
    public static final Type<ToggleVisorC2SPacket> ID = new Type<>(SCPayloads.TOGGLE_VISOR_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleVisorC2SPacket> CODEC = StreamCodec.unit(new ToggleVisorC2SPacket());

    @Override public Type<? extends CustomPacketPayload> type() { return ID; }

    public static void handle(ToggleVisorC2SPacket payload, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            if (!FabricLoader.getInstance().isModLoaded("accessories")) return;

            AccessoriesCapability.getOptionally(context.player()).ifPresent(cap -> {
                for (SlotEntryReference equipped : cap.getAllEquipped()) {
                    ItemStack stack = equipped.stack();
                    if (!stack.isEmpty() && stack.getItem() instanceof SCAccessoryItem item && item.hasOpenVisor(stack)) {
                        boolean current = stack.getOrDefault(SCDataComponents.VISOR_OPEN, false);
                        stack.set(SCDataComponents.VISOR_OPEN, !current);
                    }
                }
            });
        });
    }
}