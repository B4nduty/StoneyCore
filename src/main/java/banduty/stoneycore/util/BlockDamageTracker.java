package banduty.stoneycore.util;

import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BlockDamageTracker {
    private static final Map<BlockPos, Float> blockDamageMap = new HashMap<>();

    private BlockDamageTracker() {}

    public static void damageBlock(ServerWorld world, BlockPos pos, float damageFactor, float hardness) {
        if (hardness < 0 || world.getBlockState(pos).isAir()) return;

        float prev = blockDamageMap.getOrDefault(pos, 0.0f);
        float next = prev + damageFactor / hardness;

        if (next >= 1.0f) {
            world.breakBlock(pos, true);
            blockDamageMap.remove(pos);
            clearProgress(world, pos);
        } else {
            blockDamageMap.put(pos, next);
            sendProgress(world, pos, next);
        }
    }

    public static void clean(ServerWorld world) {
        Iterator<Map.Entry<BlockPos, Float>> it = blockDamageMap.entrySet().iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next().getKey();
            if (world.getBlockState(pos).isAir()) {
                clearProgress(world, pos);
                it.remove();
            }
        }
    }

    private static void sendProgress(ServerWorld world, BlockPos pos, float progress) {
        int stage = Math.min(9, (int)(progress * 10));
        int visualId = getVisualId(pos);

        for (ServerPlayerEntity player : world.getPlayers()) {
            player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(visualId, pos, stage));
        }
    }

    private static void clearProgress(ServerWorld world, BlockPos pos) {
        int visualId = getVisualId(pos);

        for (ServerPlayerEntity player : world.getPlayers()) {
            player.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(visualId, pos, -1));
        }
    }

    private static int getVisualId(BlockPos pos) {
        return Long.hashCode(pos.asLong());
    }
}
