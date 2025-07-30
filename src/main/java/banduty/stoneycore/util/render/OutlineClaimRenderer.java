package banduty.stoneycore.util.render;

import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.networking.ModMessages;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.*;

public class OutlineClaimRenderer {

    public static void renderOutlineClaim(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();
        Optional<Land> optionalLand = LandState.get(world).getLandByOwner(player.getUuid());

        if (optionalLand.isEmpty()) {
            sendClearPacket(player);
            return;
        }

        Land land = optionalLand.get();
        if (!player.getEquippedStack(EquipmentSlot.HEAD).isOf(land.getLandType().coreItem())) {
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

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(borderPositions.size());
        borderPositions.forEach(buf::writeBlockPos);
        ServerPlayNetworking.send(player, ModMessages.OUTLINE_CLAIM_PACKET_ID, buf);
    }

    private static void sendClearPacket(ServerPlayerEntity player) {
        PacketByteBuf clearBuf = PacketByteBufs.create();
        clearBuf.writeInt(0);
        ServerPlayNetworking.send(player, ModMessages.OUTLINE_CLAIM_PACKET_ID, clearBuf);
    }

    private static BlockPos getAdjustedTopPosition(ServerWorld world, BlockPos pos) {
        int minY = world.getBottomY();
        BlockPos.Mutable checkPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos).mutableCopy();
        BlockState state = world.getBlockState(checkPos);

        while ((state.isOf(Blocks.AIR) || (!state.isOpaque() && !state.isOf(Blocks.WATER))) && checkPos.getY() > minY) {
            checkPos.move(0, -1, 0);
            state = world.getBlockState(checkPos);
        }

        return checkPos.toImmutable();
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