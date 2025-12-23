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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.*;

public class OutlineClaimRenderer {

    public static void renderOutlineClaim(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        Optional<Land> optionalLand = LandState.get(world).getLandByOwner(player.getUUID());

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
                if (equippedStack.getTag() != null && equippedStack.getTag().getBoolean(BuiltInRegistries.ITEM.getKey(land.getLandType().coreItem()).getPath())) {
                    shouldRender = true;
                }
            }
        }

        if (!shouldRender) {
            sendClearPacket(player);
            return;
        }

        Set<BlockPos> claimed = land.getClaimed();
        List<BlockPos> borderPositions = new ArrayList<>();

        for (BlockPos pos : claimed) {
            if (isBorderBlock(pos, claimed)) {
                borderPositions.add(getAdjustedTopPosition(world, pos));
            }
        }

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(borderPositions.size());
        borderPositions.forEach(buf::writeBlockPos);
        ServerPlayNetworking.send(player, ModMessages.OUTLINE_CLAIM_PACKET_ID, buf);
    }

    private static void sendClearPacket(ServerPlayer player) {
        FriendlyByteBuf clearBuf = PacketByteBufs.create();
        clearBuf.writeInt(0);
        ServerPlayNetworking.send(player, ModMessages.OUTLINE_CLAIM_PACKET_ID, clearBuf);
    }

    private static BlockPos getAdjustedTopPosition(ServerLevel serverLevel, BlockPos pos) {
        int minY = serverLevel.getMinBuildHeight();
        BlockPos.MutableBlockPos checkPos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).mutable();
        BlockState state = serverLevel.getBlockState(checkPos);

        while ((state.is(Blocks.AIR) || (!state.canOcclude() && !state.is(Blocks.WATER))) && checkPos.getY() > minY) {
            checkPos.move(0, -1, 0);
            state = serverLevel.getBlockState(checkPos);
        }

        return checkPos.immutable();
    }

    private static boolean isBorderBlock(BlockPos pos, Set<BlockPos> claimed) {
        int x = pos.getX();
        int z = pos.getZ();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (!claimed.contains(new BlockPos(x + dx, pos.getY(), z + dz))) {
                    return true;
                }
            }
        }
        return false;
    }
}