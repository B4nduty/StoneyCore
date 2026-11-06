package banduty.stoneycore.entity.projectile;

import banduty.stoneycore.util.BlockDamageTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Shared block-damage logic for siege projectiles. */
public final class SiegeImpact {

    private SiegeImpact() {}

    public static int radiusFor(double baseDamage) {
        return Math.max(1, (int) (baseDamage / 8));
    }

    /** Damages a flattened sphere of blocks around the hit position (not a full sphere: the hit face axis is excluded). */
    public static void crater(ServerLevel level, BlockHitResult hit, double baseDamage) {
        RandomSource random = level.random;
        int baseRadius = radiusFor(baseDamage);
        Direction.Axis axis = hit.getDirection().getAxis();
        int spreadX = axis == Direction.Axis.X ? 0 : baseRadius;
        int spreadY = axis == Direction.Axis.Y ? 0 : baseRadius;
        int spreadZ = axis == Direction.Axis.Z ? 0 : baseRadius;

        float damagePerBlock = (float) baseDamage / 40f;
        BlockPos center = hit.getBlockPos();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = -spreadX - 1; x <= spreadX + 1; x++) {
            for (int y = -spreadY - 1; y <= spreadY + 1; y++) {
                for (int z = -spreadZ - 1; z <= spreadZ + 1; z++) {
                    int effectiveRadius = baseRadius + (random.nextFloat() < 0.1f ? 1 : 0);
                    if (x * x + y * y + z * z > effectiveRadius * effectiveRadius) continue;
                    if (random.nextFloat() < 0.199f * (effectiveRadius - baseRadius) + 0.001f) continue;

                    cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    BlockState state = level.getBlockState(cursor);
                    float hardness = state.getDestroySpeed(level, cursor);
                    if (state.isAir() || hardness < 0) continue;

                    BlockDamageTracker.damageBlock(level, cursor.immutable(),
                            damagePerBlock * ((float) baseRadius / effectiveRadius), hardness);
                }
            }
        }
    }
}
