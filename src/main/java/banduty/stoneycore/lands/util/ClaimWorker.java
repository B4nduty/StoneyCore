package banduty.stoneycore.lands.util;

import banduty.stoneycore.StoneyCore;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public class ClaimWorker {
    private final ServerWorld world;
    private final Land land;
    private final Consumer<Boolean> onComplete;

    // Two queues: high priority (likely claimable) and normal queue
    private final Deque<Long> priorityQueue = new ArrayDeque<>();
    private final Deque<Long> queue = new ArrayDeque<>();

    private final LongArrayList acceptedKeys = new LongArrayList();
    private final LongOpenHashSet visited = new LongOpenHashSet();
    private static final Int2ByteOpenHashMap neighborMask = new Int2ByteOpenHashMap();

    static {
        // Default return value avoids creating entries for empty masks
        neighborMask.defaultReturnValue((byte) 0);
    }

    private int totalAccepted = 0;
    private final long startTime;

    private int claimsThisPeriod = 0;
    private int logTick = 0;

    private long lastZeroCpsCheckTime;
    private long lastLogTime;
    private int claimsSinceLastZeroCheck = 0;
    private final int requiredTicksForZeroCpsStop;
    private int tickCPS = 0;

    private final int maxWorkPerTick = StoneyCore.getConfig().technicalOptions.maxWorkPerTick();

    private static final int[][] NEIGHBOR_OFFSETS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
    };

    public ClaimWorker(ServerWorld world, Land land, Iterable<BlockPos> candidates, int radius, Consumer<Boolean> onComplete) {
        this.world = world;
        this.land = land;
        this.onComplete = onComplete;

        for (BlockPos claimedPos : land.getClaimed()) {
            priorityQueue.add(claimedPos.asLong());
            visited.add(claimedPos.asLong());
        }

        for (BlockPos pos : candidates) {
            long key = pos.asLong();
            if (!land.isAlreadyClaimed(key)) {
                if (land.columnInvalid(world, key)) {
                    visited.add(key);
                } else {
                    queue.add(key);
                }
            }
        }

        this.requiredTicksForZeroCpsStop = Math.max(1, radius / 10);
        long currentTickTime = world.getServer().getTicks();
        this.startTime = currentTickTime;
        this.lastZeroCpsCheckTime = currentTickTime;
        this.lastLogTime = currentTickTime;
    }

    public boolean tick() {
        acceptedKeys.clear();
        if (priorityQueue.isEmpty() && queue.isEmpty()) return true;

        final long coreKey = land.getCorePos().asLong();
        int workDone = 0;
        int queueSizeBefore = queue.size() + priorityQueue.size();

        while (workDone < this.maxWorkPerTick && (!priorityQueue.isEmpty() || !queue.isEmpty())) {
            long key = !priorityQueue.isEmpty() ? priorityQueue.poll() : queue.poll();

            if (visited.contains(key)) continue;

            boolean blockedPath = land.pathBlocked(world, coreKey, key);
            boolean hasNeighbor = totalAccepted == 0 || land.hasAdjacentClaim(key) || fastHasAcceptedNeighbor(key);

            workDone++;

            if (blockedPath && !hasNeighbor) {
                queue.addLast(key);
                continue;
            }

            // Accept claim
            visited.add(key);
            acceptedKeys.add(key);
            updateNeighborMask(key);
            totalAccepted++;
            claimsThisPeriod++;
        }

        if (!acceptedKeys.isEmpty()) {
            land.addClaims(acceptedKeys);
            claimsSinceLastZeroCheck += acceptedKeys.size();
            LandState.get(world).markClaimed(acceptedKeys, land);
        }

        logProgress();

        if ((priorityQueue.isEmpty() && queue.isEmpty()) || (workDone == 0 && queue.size() + priorityQueue.size() == queueSizeBefore)) {
            finish();
            return true;
        }

        return false;
    }

    private void logProgress() {
        logTick++;
        long now = world.getServer().getTicks();
        double seconds = (now - lastZeroCpsCheckTime) / 20.0;
        if (seconds > 0) {
            double cps = claimsSinceLastZeroCheck / seconds;
            if (cps != 0) tickCPS = 0;
            if (cps == 0.0) {
                if (tickCPS >= requiredTicksForZeroCpsStop * 5000 / StoneyCore.getConfig().technicalOptions.maxWorkPerTick()) {
                    StoneyCore.LOGGER.info("[ClaimWorker] Stopping early due to 0 CPS check.");
                    queue.clear();
                    priorityQueue.clear();
                } else tickCPS++;
            } else if (logTick >= 10) {
                seconds = (now - lastLogTime) / 20.0;
                if (seconds > 0) {
                    cps = claimsThisPeriod / seconds;
                    StoneyCore.LOGGER.info("[ClaimWorker] Progress: {} claims so far (~{} CPS avg)",
                            totalAccepted, cps);
                }
                claimsThisPeriod = 0;
                lastLogTime = now;
                logTick = 0;
            }
        }
        claimsSinceLastZeroCheck = 0;
        lastZeroCpsCheckTime = now;
    }

    private void finish() {
        long elapsed = (world.getServer().getTicks() - startTime);
        double totalCps = totalAccepted / (elapsed / 20.0);
        StoneyCore.LOGGER.info("[ClaimWorker] Finished claiming {} blocks in {} ticks (~{} CPS) for land '{}'.",
                totalAccepted, elapsed, totalCps, land.getName(world));
        onComplete.accept(totalAccepted > 0);
    }

    private static int packXZ(int x, int z) {
        return (x * 73428767) ^ z;
    }

    private void updateNeighborMask(long key) {
        int x = BlockPos.unpackLongX(key);
        int z = BlockPos.unpackLongZ(key);

        for (int i = 0; i < NEIGHBOR_OFFSETS.length; i++) {
            int packedKey = packXZ(x + NEIGHBOR_OFFSETS[i][0], z + NEIGHBOR_OFFSETS[i][1]);

            neighborMask.addTo(packedKey, (byte) (1 << i));
        }
    }

    private boolean fastHasAcceptedNeighbor(long key) {
        return neighborMask.get(packXZ(BlockPos.unpackLongX(key), BlockPos.unpackLongZ(key))) != 0;
    }
}
