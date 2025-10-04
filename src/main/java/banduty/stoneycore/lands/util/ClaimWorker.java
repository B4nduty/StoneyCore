package banduty.stoneycore.lands.util;

import banduty.stoneycore.StoneyCore;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.function.Consumer;

public class ClaimWorker {
    private final ServerWorld world;
    private final Land land;
    private final Consumer<Boolean> onComplete;

    // Two queues: high priority (likely claimable) and normal queue
    private final LongArrayFIFOQueue priorityQueue = new LongArrayFIFOQueue();
    private final LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
    private final LongSet invalid = new LongOpenHashSet();

    private final LongArrayList acceptedKeys = new LongArrayList();
    private final Long2ByteOpenHashMap neighborMask = new Long2ByteOpenHashMap();

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

        neighborMask.defaultReturnValue((byte) 0);

        for (BlockPos claimedPos : land.getClaimed()) {
            long key = claimedPos.asLong();
            priorityQueue.enqueue(key);
            updateNeighborMask(key);
        }

        for (BlockPos pos : candidates) {
            long key = pos.asLong();
            if (ClaimUtils.isInvalidClaimColumn(world, BlockPos.fromLong(key), land.getLandType())) {
                invalid.add(key);
            } else if (!land.isAlreadyClaimed(key)) {
                queue.enqueue(key);
            }
        }

        this.requiredTicksForZeroCpsStop = Math.max(1, radius / 100);
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
        int initialQueueSize = queue.size() + priorityQueue.size();

        while (workDone < maxWorkPerTick && (!priorityQueue.isEmpty() || !queue.isEmpty())) {
            long key = !priorityQueue.isEmpty() ? priorityQueue.dequeueLong() : queue.dequeueLong();

            workDone++;

            if (invalid.contains(key)) continue;

            boolean blockedPath = ClaimUtils.pathContainsInvalidBlock(world, BlockPos.fromLong(coreKey), BlockPos.fromLong(key), land.getLandType());
            boolean hasNeighbor = totalAccepted == 0 || fastHasAcceptedNeighbor(key);

            if (blockedPath && !hasNeighbor) {
                queue.enqueue(key); // push back for later
                continue;
            }

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

        if ((priorityQueue.isEmpty() && queue.isEmpty()) || (workDone == 0 && queue.size() + priorityQueue.size() == initialQueueSize)) {
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
                totalAccepted, elapsed, totalCps, land.getLandTitle(world).getString());
        onComplete.accept(totalAccepted > 0);
    }

    private boolean fastHasAcceptedNeighbor(long key) {
        long xyNormalizedKey = BlockPos.asLong(BlockPos.unpackLongX(key), 0, BlockPos.unpackLongZ(key));
        return neighborMask.get(xyNormalizedKey) != 0;
    }

    private void updateNeighborMask(long key) {
        int x = BlockPos.unpackLongX(key);
        int z = BlockPos.unpackLongZ(key);

        for (int i = 0; i < NEIGHBOR_OFFSETS.length; i++) {
            int nx = x + NEIGHBOR_OFFSETS[i][0];
            int nz = z + NEIGHBOR_OFFSETS[i][1];
            long neighborKey = BlockPos.asLong(nx, 0, nz);
            neighborMask.addTo(neighborKey, (byte) (1 << i));
        }
    }

}
