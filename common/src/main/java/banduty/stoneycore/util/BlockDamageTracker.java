package banduty.stoneycore.util;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BlockDamageTracker {
    private static final Map<BlockPos, Float> blockDamageMap = new HashMap<>();

    private BlockDamageTracker() {}

    public static void damageBlock(ServerLevel serverLevel, BlockPos pos, float damageFactor, float hardness) {
        if (hardness < 0 || serverLevel.getBlockState(pos).isAir()) return;

        float prev = blockDamageMap.getOrDefault(pos, 0.0f);
        float next = prev + damageFactor / hardness;

        if (next >= 1.0f) {
            serverLevel.destroyBlock(pos, !StoneyCore.getConfig().technicalOptions().breakOrRemoveSiegeDestroy());
            blockDamageMap.remove(pos);
            clearProgress(serverLevel, pos);
        } else {
            blockDamageMap.put(pos, next);
            sendProgress(serverLevel, pos, next);
        }
    }

    public static void clean(ServerLevel serverLevel) {
        Iterator<Map.Entry<BlockPos, Float>> it = blockDamageMap.entrySet().iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next().getKey();
            if (serverLevel.getBlockState(pos).isAir()) {
                clearProgress(serverLevel, pos);
                it.remove();
            }
        }
    }

    private static void sendProgress(ServerLevel serverLevel, BlockPos pos, float progress) {
        int stage = Math.min(9, (int)(progress * 10));
        int visualId = getVisualId(pos);

        for (ServerPlayer player : serverLevel.players()) {
            player.connection.send(new ClientboundBlockDestructionPacket(visualId, pos, stage));
        }
    }

    private static void clearProgress(ServerLevel serverLevel, BlockPos pos) {
        int visualId = getVisualId(pos);

        for (ServerPlayer player : serverLevel.players()) {
            player.connection.send(new ClientboundBlockDestructionPacket(visualId, pos, -1));
        }
    }

    private static int getVisualId(BlockPos pos) {
        return Long.hashCode(pos.asLong());
    }
}
