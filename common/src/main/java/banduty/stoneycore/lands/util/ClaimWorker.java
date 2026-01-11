package banduty.stoneycore.lands.util;

import banduty.stoneycore.StoneyCore;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Consumer;

public class ClaimWorker {
    private final ServerLevel serverLevel;
    private final Land land;
    private final Consumer<Boolean> onComplete;

    private final LongArrayFIFOQueue priorityQueue = new LongArrayFIFOQueue();
    private final LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
    private final LongSet processed = new LongOpenHashSet();
    private final LongSet invalid = new LongOpenHashSet();

    private final LongArrayList acceptedKeys = new LongArrayList();
    private final LongSet acceptedSet = new LongOpenHashSet();

    private int totalAccepted = 0;
    private final long startTime;

    private int claimsThisPeriod = 0;
    private int logTick = 0;

    private long lastProgressCheckTime;
    private final int requiredTicksForZeroCpsStop;
    private int consecutiveZeroTicks = 0;

    private final int maxWorkPerTick;
    private final BlockPos corePos;
    private final int targetRadius;
    private final long radiusSquared;

    private static final int[][] NEIGHBOR_OFFSETS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };

    public ClaimWorker(ServerLevel serverLevel, Land land, Iterable<BlockPos> candidates, int radius, Consumer<Boolean> onComplete) {
        this.serverLevel = serverLevel;
        this.land = land;
        this.onComplete = onComplete;
        this.maxWorkPerTick = StoneyCore.getConfig().technicalOptions().maxWorkPerTick();
        this.corePos = land.getCorePos();
        this.targetRadius = radius;
        this.radiusSquared = (long) radius * radius;

        long coreKey = corePos.asLong();
        if (!land.isAlreadyClaimed(coreKey) && isWithinRadius(corePos)) {
            acceptedKeys.add(coreKey);
            acceptedSet.add(coreKey);
            totalAccepted++;
            addNeighborsToQueue(coreKey);
        }

        for (BlockPos claimedPos : land.getClaimed()) {
            long key = claimedPos.asLong();
            if (!processed.contains(key) && !acceptedSet.contains(key) && isWithinRadius(claimedPos)) {
                priorityQueue.enqueue(key);
                processed.add(key);
                acceptedSet.add(key);
                totalAccepted++;
            }
        }

        if (!acceptedKeys.isEmpty()) {
            commitAcceptedClaims();
        }

        for (BlockPos pos : candidates) {
            long key = pos.asLong();
            if (!land.isAlreadyClaimed(key) && !processed.contains(key) && !acceptedSet.contains(key) && isWithinRadius(pos)) {
                if (ClaimUtils.isInvalidClaimColumn(serverLevel, pos, land.getLandType())) {
                    invalid.add(key);
                } else {
                    queue.enqueue(key);
                    processed.add(key);
                }
            }
        }

        this.requiredTicksForZeroCpsStop = Math.max(5, radius / 50);
        long currentTime = System.currentTimeMillis();
        this.startTime = currentTime;
        this.lastProgressCheckTime = currentTime;
    }

    private boolean isWithinRadius(BlockPos pos) {
        long dx = pos.getX() - corePos.getX();
        long dz = pos.getZ() - corePos.getZ();
        return (dx * dx + dz * dz) <= radiusSquared;
    }

    public boolean tick() {
        if (priorityQueue.isEmpty() && queue.isEmpty()) {
            finish();
            return true;
        }

        int workDone = 0;
        int timesProcessed = 0;
        int initialTotalSize = priorityQueue.size() + queue.size();

        // Process priority queue first (connected claims)
        while (workDone < maxWorkPerTick && timesProcessed <= maxWorkPerTick && !priorityQueue.isEmpty()) {
            long key = priorityQueue.dequeueLong();
            if (processClaim(key)) {
                workDone++;
            }
            timesProcessed++;
        }

        timesProcessed = 0;

        // Then process regular queue
        while (workDone < maxWorkPerTick && timesProcessed <= maxWorkPerTick && !queue.isEmpty()) {
            long key = queue.dequeueLong();
            if (processClaim(key)) {
                workDone++;
            }
            timesProcessed++;
        }

        if (!acceptedKeys.isEmpty()) {
            commitAcceptedClaims();
        }

        return logProgress(workDone, initialTotalSize);
    }

    private void commitAcceptedClaims() {
        land.addClaims(acceptedKeys);
        LandState.get(serverLevel).markClaimed(acceptedKeys, land);
        acceptedKeys.clear();
    }

    private boolean processClaim(long key) {
        if (invalid.contains(key) || acceptedSet.contains(key)) {
            return false;
        }

        BlockPos claimPos = BlockPos.of(key);

        if (!isWithinRadius(claimPos)) {
            invalid.add(key);
            return false;
        }

        boolean hasAcceptedNeighbor = hasAcceptedNeighbor(key);

        boolean blockedPath = ClaimUtils.pathContainsInvalidBlock(serverLevel, corePos, claimPos, land.getLandType());

        boolean canClaim = !blockedPath || hasAcceptedNeighbor;

        if (!canClaim) {
            queue.enqueue(key);
            return false;
        }

        acceptedKeys.add(key);
        acceptedSet.add(key);
        totalAccepted++;
        claimsThisPeriod++;

        addNeighborsToQueue(key);

        return true;
    }

    private boolean hasAcceptedNeighbor(long key) {
        int x = BlockPos.getX(key);
        int z = BlockPos.getZ(key);

        for (int[] offset : NEIGHBOR_OFFSETS) {
            long neighborKey = BlockPos.asLong(x + offset[0], 0, z + offset[1]);
            if (acceptedSet.contains(neighborKey)) {
                return true;
            }
        }
        return false;
    }

    private void addNeighborsToQueue(long key) {
        int x = BlockPos.getX(key);
        int z = BlockPos.getZ(key);

        for (int[] offset : NEIGHBOR_OFFSETS) {
            long neighborKey = BlockPos.asLong(x + offset[0], 0, z + offset[1]);
            BlockPos neighborPos = BlockPos.of(neighborKey);

            if (isWithinRadius(neighborPos) &&
                    !processed.contains(neighborKey) &&
                    !invalid.contains(neighborKey) &&
                    !acceptedSet.contains(neighborKey)) {

                // Validate the neighbor before adding to queue
                if (!ClaimUtils.isInvalidClaimColumn(serverLevel, neighborPos, land.getLandType())) {
                    priorityQueue.enqueue(neighborKey);
                    processed.add(neighborKey);
                } else {
                    invalid.add(neighborKey);
                }
            }
        }
    }

    private boolean logProgress(int workDone, int initialTotalSize) {
        logTick++;
        long now = System.currentTimeMillis();
        double secondsSinceLastCheck = (now - lastProgressCheckTime) / 1000.0;

        if (workDone == 0) {
            consecutiveZeroTicks++;
        } else {
            consecutiveZeroTicks = 0;
        }

        boolean shouldFinish = (priorityQueue.isEmpty() && queue.isEmpty()) ||
                (consecutiveZeroTicks >= requiredTicksForZeroCpsStop);

        if (shouldFinish) {
            StoneyCore.LOG.info("[ClaimWorker] Stopping early after {} zero-progress ticks", consecutiveZeroTicks);
            finish();
            return true;
        }

        if (logTick >= 20 && secondsSinceLastCheck > 1.0) {
            double cps = claimsThisPeriod / secondsSinceLastCheck;
            int remaining = priorityQueue.size() + queue.size();
            double progress = totalAccepted / (double) (totalAccepted + remaining) * 100;

            StoneyCore.LOG.info("[ClaimWorker] Progress: {}%, ~{} blocks/s, target radius: {}",
                    String.format("%.1f", progress),
                    String.format("%.1f", cps),
                    targetRadius);

            claimsThisPeriod = 0;
            logTick = 0;
            lastProgressCheckTime = now;
        }

        return false;
    }

    private void finish() {
        long elapsedMs = System.currentTimeMillis() - startTime;
        double totalCps = elapsedMs > 0 ? totalAccepted / (elapsedMs / 1000.0) : 0;

        land.setRadius(targetRadius, serverLevel);

        StoneyCore.LOG.info("[ClaimWorker] Finished claiming {} blocks in {}ms (~{}/s) for land '{}' with radius {}",
                totalAccepted, elapsedMs, String.format("%.1f", totalCps),
                land.getLandTitle(serverLevel).getString(), targetRadius);

        onComplete.accept(totalAccepted > 0);
    }
}