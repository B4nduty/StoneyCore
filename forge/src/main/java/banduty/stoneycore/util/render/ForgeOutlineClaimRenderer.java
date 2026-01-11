package banduty.stoneycore.util.render;

import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.networking.packet.OutlineClaimS2CPacket;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

public class ForgeOutlineClaimRenderer implements OutlineClaimRendererHelper {

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
        ModMessages.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OutlineClaimS2CPacket(null));
    }

    @Override
    public void sendOutlinePacket(ServerPlayer player, List<BlockPos> borderPositions) {
        ModMessages.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OutlineClaimS2CPacket(borderPositions));
    }
}