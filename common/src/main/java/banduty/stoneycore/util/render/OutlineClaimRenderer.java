package banduty.stoneycore.util.render;

import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OutlineClaimRenderer {
    public static void renderOutlineClaim(ServerPlayer player) {
        Services.OUTLINE_CLAIM_RENDERER.renderOutlineClaim(player);
    }

    protected static List<BlockPos> calculateBorderPositions(ServerPlayer player, Land land) {
        ServerLevel world = player.serverLevel();
        Set<BlockPos> claimed = land.getClaimed();
        List<BlockPos> borderPositions = new ArrayList<>();

        for (BlockPos pos : claimed) {
            if (isBorderBlock(pos, claimed)) {
                borderPositions.add(getAdjustedTopPosition(world, pos));
            }
        }

        return borderPositions;
    }

    protected static boolean isBorderBlock(BlockPos pos, Set<BlockPos> claimed) {
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

    protected static BlockPos getAdjustedTopPosition(ServerLevel serverLevel, BlockPos pos) {
        int minY = serverLevel.getMinBuildHeight();
        BlockPos.MutableBlockPos checkPos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).mutable();
        BlockState state = serverLevel.getBlockState(checkPos);

        while ((state.is(Blocks.AIR) || (!state.canOcclude() && !state.is(Blocks.WATER))) && checkPos.getY() > minY) {
            checkPos.move(0, -1, 0);
            state = serverLevel.getBlockState(checkPos);
        }

        return checkPos.immutable();
    }
}