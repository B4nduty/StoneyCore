package banduty.stoneycore.lands.util;

import banduty.stoneycore.lands.LandType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class ClaimUtils {

    /**
     * A column is considered invalid for claiming if the topmost motion-blocking block
     * (or the first non-air, opaque, water, or lava block found by scanning downward)
     * does not match the allowed terrain type for the land.
     * <p>
     * Scans downward from the top position until a solid block, water, or lava is found,
     * stopping at the world's minimum build height.
     * <p>
     * Terrain validity rules:
     * <p>
     *  - GROUND: only solid land blocks (not water or lava) are valid.
     * <p>
     *  - WATER: only water is valid.
     * <p>
     *  - LAVA: only lava is valid.
     * <p>
     *  - GW: ground or water is valid.
     * <p>
     *  - GL: ground or lava is valid.
     * <p>
     *  - WL: water or lava is valid.
     * <p>
     *  - GWL: all terrain types are valid (always returns false).
     */
    public static boolean isInvalidClaimColumn(ServerWorld world, BlockPos pos, LandType landType) {
        LandType.TerrainType terrain = landType.terrainType();
        if (terrain == LandType.TerrainType.GWL) return false;

        BlockPos checkPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos);
        BlockState state = world.getBlockState(checkPos);

        while ((state.isOf(Blocks.AIR) || (!state.isOpaque() && !state.isOf(Blocks.WATER) && !state.isOf(Blocks.LAVA)))
                && checkPos.getY() > world.getBottomY()) {
            checkPos = checkPos.down();
            state = world.getBlockState(checkPos);
        }

        boolean isWater = state.isOf(Blocks.WATER) || state.getFluidState().isIn(FluidTags.WATER);
        boolean isLava = state.isOf(Blocks.LAVA);
        boolean isGround = !isWater && !isLava;

        return switch (terrain) {
            case GROUND -> !isGround;
            case WATER -> !isWater;
            case LAVA -> !isLava;
            case GW -> !(isGround || isWater);
            case GL -> !(isGround || isLava);
            case WL -> !(isWater || isLava);
            default -> throw new IllegalStateException("Unexpected value: " + terrain);
        };
    }

    /**
     * Bresenham‑like line check, sampling every block step.
     * If *any* sampled column is invalid, path is blocked.
     */
    public static boolean pathContainsInvalidBlock(ServerWorld world, BlockPos start, BlockPos end, LandType landType) {
        int dx    = end.getX() - start.getX();
        int dz    = end.getZ() - start.getZ();
        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        if (steps == 0) return false;

        double stepX = dx / (double) steps;
        double stepZ = dz / (double) steps;

        for (int i = 0; i <= steps; i++) {
            int x = start.getX() + (int) Math.round(stepX * i);
            int z = start.getZ() + (int) Math.round(stepZ * i);
            // always test at y = 0 (your column logic will scan vertical)
            if (isInvalidClaimColumn(world, new BlockPos(x, 0, z), landType)) {
                return true;
            }
        }

        return false;
    }
}