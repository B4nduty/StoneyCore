package banduty.stoneycore.lands.util;

import banduty.stoneycore.StoneyCore;
import it.unimi.dsi.fastutil.longs.*;
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
    private final LongSet acceptedSet = new LongOpenHashSet();
    private final LongSet invalid = new LongOpenHashSet();

    private final LongArrayList acceptedKeys = new LongArrayList();

    private final int maxWorkPerTick;
    private final BlockPos corePos;
    private final int targetRadius;
    private final long radiusSquared;

    private int totalAccepted = 0;

    private long startTime;
    private long lastProgressCheckTime;
    private int claimsThisPeriod = 0;

    private int consecutiveZeroTicks = 0;
    private final int requiredTicksForZeroCpsStop;

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

        this.requiredTicksForZeroCpsStop = Math.max(5, radius / 50);

        this.startTime = System.currentTimeMillis();
        this.lastProgressCheckTime = startTime;

        init(candidates);
    }

    private void init(Iterable<BlockPos> candidates) {
        long coreKey = corePos.asLong();

        // Start from core
        if (isWithinRadius(corePos) && !ClaimUtils.isInvalidClaimColumn(serverLevel, corePos, land.getLandType())) {
            accept(coreKey);
            addNeighborsToQueue(coreKey);
        } else {
            StoneyCore.LOG.warn("[ClaimWorker] Core position is invalid!");
        }

        // Add candidates
        for (BlockPos pos : candidates) {
            long key = pos.asLong();

            if (processed.contains(key) || acceptedSet.contains(key)) continue;
            if (!isWithinRadius(pos)) continue;

            if (ClaimUtils.isInvalidClaimColumn(serverLevel, pos, land.getLandType())) {
                invalid.add(key);
            } else {
                queue.enqueue(key);
                processed.add(key);
            }
        }

        if (!acceptedKeys.isEmpty()) {
            commitAcceptedClaims();
        }
    }

    public boolean tick() {
        if (priorityQueue.isEmpty() && queue.isEmpty()) {
            finish();
            return true;
        }

        int workDone = 0;

        // Priority queue first (BFS expansion)
        while (workDone < maxWorkPerTick && !priorityQueue.isEmpty()) {
            long key = priorityQueue.dequeueLong();
            if (processClaim(key)) workDone++;
        }

        // Retry queue
        int retrySize = queue.size();
        for (int i = 0; i < retrySize && workDone < maxWorkPerTick; i++) {
            long key = queue.dequeueLong();
            if (processClaim(key)) workDone++;
        }

        if (!acceptedKeys.isEmpty()) {
            commitAcceptedClaims();
        }

        return logProgress(workDone);
    }

    private void commitAcceptedClaims() {
        land.addClaims(acceptedKeys);
        LandState.get(serverLevel).markClaimed(acceptedKeys, land);
        acceptedKeys.clear();
    }

    private boolean processClaim(long key) {
        if (invalid.contains(key) || acceptedSet.contains(key)) return false;

        BlockPos claimPos = BlockPos.of(key);

        if (!isWithinRadius(claimPos)) {
            invalid.add(key);
            return false;
        }

        boolean hasAcceptedNeighbor = hasAcceptedNeighbor(key);
        boolean blockedPath = ClaimUtils.pathContainsInvalidBlock(serverLevel, corePos, claimPos, land.getLandType());

        if (blockedPath && !hasAcceptedNeighbor) {
            queue.enqueue(key);
            return false;
        }

        accept(key);
        addNeighborsToQueue(key);
        return true;
    }

    private void accept(long key) {
        acceptedSet.add(key);
        acceptedKeys.add(key);
        totalAccepted++;
        claimsThisPeriod++;
    }

    private boolean hasAcceptedNeighbor(long key) {
        int x = BlockPos.getX(key);
        int z = BlockPos.getZ(key);

        for (int[] offset : NEIGHBOR_OFFSETS) {
            long neighborKey = BlockPos.asLong(x + offset[0], 0, z + offset[1]);
            if (acceptedSet.contains(neighborKey)) return true;
        }
        return false;
    }

    private void addNeighborsToQueue(long key) {
        int x = BlockPos.getX(key);
        int z = BlockPos.getZ(key);

        for (int[] offset : NEIGHBOR_OFFSETS) {
            long neighborKey = BlockPos.asLong(x + offset[0], 0, z + offset[1]);

            if (processed.contains(neighborKey) || acceptedSet.contains(neighborKey) || invalid.contains(neighborKey))
                continue;

            BlockPos neighborPos = BlockPos.of(neighborKey);

            if (!isWithinRadius(neighborPos)) continue;

            if (ClaimUtils.isInvalidClaimColumn(serverLevel, neighborPos, land.getLandType())) {
                invalid.add(neighborKey);
            } else {
                priorityQueue.enqueue(neighborKey);
                processed.add(neighborKey);
            }
        }
    }


    private boolean isWithinRadius(BlockPos pos) {
        long dx = pos.getX() - corePos.getX();
        long dz = pos.getZ() - corePos.getZ();
        return (dx * dx + dz * dz) <= radiusSquared;
    }

    private boolean logProgress(int workDone) {
        long now = System.currentTimeMillis();

        if (workDone == 0) consecutiveZeroTicks++;
        else consecutiveZeroTicks = 0;

        if (consecutiveZeroTicks >= requiredTicksForZeroCpsStop) {
            StoneyCore.LOG.info("[ClaimWorker] Stopping early (no progress)");
            finish();
            return true;
        }

        if (now - lastProgressCheckTime >= 1000) {
            double cps = claimsThisPeriod / ((now - lastProgressCheckTime) / 1000.0);
            int remaining = priorityQueue.size() + queue.size();

            StoneyCore.LOG.info("[ClaimWorker] {} claimed, {} remaining (~{}/s)",
                    totalAccepted, remaining, String.format("%.1f", cps));

            claimsThisPeriod = 0;
            lastProgressCheckTime = now;
        }

        return false;
    }

    private void finish() {
        long elapsed = System.currentTimeMillis() - startTime;
        double cps = elapsed > 0 ? totalAccepted / (elapsed / 1000.0) : 0;

        land.setRadius(targetRadius, serverLevel);

        StoneyCore.LOG.info("[ClaimWorker] Finished claiming {} blocks in {}ms (~{}/s) for land '{}' with radius {}",
                totalAccepted, elapsed, String.format("%.1f", cps),
                land.getLandTitle(serverLevel).getString(), targetRadius);

        onComplete.accept(totalAccepted > 0);
    }
}