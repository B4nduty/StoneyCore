package banduty.stoneycore.lands.util;

import banduty.stoneycore.lands.LandType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class ClaimUtils {
    /**
     * A column is considered invalid for claiming if the topmost motion-blocking block
     * (or the first non-air, opaque, water, or lava block found by scanning downward)
     * does not match the allowed terrain type for the land.
     * <p>
     * Scans downward from the top position until a solid block, water, or lava is found,
     * stopping at the level's minimum build height.
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
    public static boolean isInvalidClaimColumn(ServerLevel level, BlockPos pos, LandType landType) {
        LandType.TerrainType terrain = landType.terrainType();

        if (terrain == LandType.TerrainType.GWL) return false;

        // Get the top motion-blocking position
        BlockPos.MutableBlockPos cursor =
                level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).mutable();

        int minY = level.getMinBuildHeight();

        BlockState state = level.getBlockState(cursor);

        // Fast downward scan for first valid "surface"
        while (cursor.getY() > minY &&
                (state.isAir() || (!state.canOcclude() && !state.getFluidState().isSource()))) {
            cursor.move(0, -1, 0);
            state = level.getBlockState(cursor);
        }

        boolean isWater = state.getFluidState().is(FluidTags.WATER);
        boolean isLava  = state.is(Blocks.LAVA);
        boolean isGround = !isWater && !isLava;

        return switch (terrain) {
            case GROUND -> !isGround;
            case WATER -> !isWater;
            case LAVA  -> !isLava;
            case GW    -> !(isGround || isWater);
            case GL    -> !(isGround || isLava);
            case WL    -> !(isWater  || isLava);
            default    -> true;
        };
    }

    /**
     * Bresenham-like line sampling.
     */
    public static boolean pathContainsInvalidBlock(ServerLevel level, BlockPos start, BlockPos end, LandType landType) {
        int x0 = start.getX(), z0 = start.getZ();
        int x1 = end.getX(),   z1 = end.getZ();

        int dx = Math.abs(x1 - x0);
        int dz = Math.abs(z1 - z0);

        int sx = Integer.compare(x1, x0);
        int sz = Integer.compare(z1, z0);

        int err = dx - dz;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        while (true) {
            pos.set(x0, 0, z0);

            if (isInvalidClaimColumn(level, pos, landType))
                return true;

            if (x0 == x1 && z0 == z1)
                break;

            int e2 = err << 1;

            if (e2 > -dz) { err -= dz; x0 += sx; }
            if (e2 < dx)  { err += dx; z0 += sz; }
        }

        return false;
    }
}