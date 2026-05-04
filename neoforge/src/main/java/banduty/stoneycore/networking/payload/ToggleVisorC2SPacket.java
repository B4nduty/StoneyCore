package banduty.stoneycore.networking.payload;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.SCAccessoryItem;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
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
        if (context.player() instanceof ServerPlayer player && ModList.get().isLoaded("accessories")) {
            if (AccessoriesCapability.getOptionally(player).isPresent()) {
                for (SlotEntryReference equipped : AccessoriesCapability.get(player).getAllEquipped()) {
                    ItemStack stack = equipped.stack();
                    if (!stack.isEmpty() && stack.getItem() instanceof SCAccessoryItem scAccessoryItem &&
                            scAccessoryItem.hasOpenVisor(stack)) {
                        boolean current = Boolean.TRUE.equals(stack.get(SCDataComponents.VISOR_OPEN));
                        stack.set(SCDataComponents.VISOR_OPEN, !current);
                    }
                }
            }
        }
    }
}