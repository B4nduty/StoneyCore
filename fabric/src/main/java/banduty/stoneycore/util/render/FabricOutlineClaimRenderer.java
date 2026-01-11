package banduty.stoneycore.util.render;

import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.networking.ModMessages;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class FabricOutlineClaimRenderer implements OutlineClaimRendererHelper {
    @Override
    public void renderOutlineClaim(ServerPlayer player) {
        Optional<Land> optionalLand = LandState.get(player.serverLevel()).getLandByOwner(player.getUUID());

        if (optionalLand.isEmpty()) {
            sendClearPacket(player);
            return;
        }

        boolean shouldRender = false;
        Land land = optionalLand.get();

        if (player.getItemBySlot(EquipmentSlot.HEAD).is(land.getLandType().coreItem())) {
            shouldRender = true;
        }

        if (AccessoriesCapability.getOptionally(player).isPresent()) {
            for (SlotEntryReference equipped : AccessoriesCapability.get(player).getAllEquipped()) {
                ItemStack equippedStack = equipped.stack();
                if (equippedStack.getTag() != null && equippedStack.getTag().getBoolean(
                        BuiltInRegistries.ITEM.getKey(land.getLandType().coreItem()).getPath())) {
                    shouldRender = true;
                }
            }
        }

        if (!shouldRender) {
            sendClearPacket(player);
            return;
        }

        List<BlockPos> borderPositions = OutlineClaimRenderer.calculateBorderPositions(player, land);
        sendOutlinePacket(player, borderPositions);
    }

    @Override
    public void sendClearPacket(ServerPlayer player) {
        FriendlyByteBuf clearBuf = PacketByteBufs.create();
        clearBuf.writeInt(0);
        ServerPlayNetworking.send(player, ModMessages.OUTLINE_CLAIM_PACKET_ID, clearBuf);
    }

    @Override
    public void sendOutlinePacket(ServerPlayer player, List<BlockPos> borderPositions) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(borderPositions.size());
        borderPositions.forEach(buf::writeBlockPos);
        ServerPlayNetworking.send(player, ModMessages.OUTLINE_CLAIM_PACKET_ID, buf);
    }
}