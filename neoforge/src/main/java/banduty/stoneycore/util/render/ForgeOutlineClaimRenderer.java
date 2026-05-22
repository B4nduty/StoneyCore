package banduty.stoneycore.util.render;

import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.networking.payload.OutlineClaimS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

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

        ItemStack itemStack = player.getItemBySlot(EquipmentSlot.HEAD);
        for (ItemStack accessoryStack : SCUnderArmor.getAccessories(itemStack)) {
            if (accessoryStack.getItem() == land.getLandType().coreItem()) {
                shouldRender = true;
                break;
            }
        }

        if (!shouldRender) {
            sendClearPacket(player);
            return;
        }

        List<BlockPos> borderPositions = OutlineClaimRenderer.calculateBorderPositions(player.serverLevel(), land);
        sendOutlinePacket(player, borderPositions);
    }

    @Override
    public void sendClearPacket(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new OutlineClaimS2CPacket(List.of()));
    }

    @Override
    public void sendOutlinePacket(ServerPlayer player, List<BlockPos> borderPositions) {
        PacketDistributor.sendToPlayer(player, new OutlineClaimS2CPacket(borderPositions));
    }
}