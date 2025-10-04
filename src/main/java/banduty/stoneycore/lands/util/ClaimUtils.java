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

        BlockPos.Mutable checkPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos).mutableCopy();
        BlockState state = world.getBlockState(checkPos);

        while ((state.isOf(Blocks.AIR) || (!state.isOpaque() && !state.isOf(Blocks.WATER) && !state.isOf(Blocks.LAVA)))
                && checkPos.getY() > world.getBottomY()) {
            checkPos.move(0, -1, 0);
            state = world.getBlockState(checkPos);
        }

        boolean isWater = state.getFluidState().isIn(FluidTags.WATER);
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
        int x0 = start.getX(), z0 = start.getZ();
        int x1 = end.getX(),   z1 = end.getZ();

        int dx = Math.abs(x1 - x0);
        int dz = Math.abs(z1 - z0);

        int sx = Integer.compare(x1, x0);
        int sz = Integer.compare(z1, z0);

        int err = dx - dz;
        BlockPos.Mutable pos = new BlockPos.Mutable();

        while (true) {
            pos.set(x0, 0, z0);
            if (isInvalidClaimColumn(world, pos, landType)) return true;

            if (x0 == x1 && z0 == z1) break;

            int e2 = err << 1;
            if (e2 > -dz) { err -= dz; x0 += sx; }
            if (e2 < dx)  { err += dx; z0 += sz; }
        }
        return false;
    }
}