package banduty.stoneycore.lands.visitor;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.LandState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VisitorTracker {
    private static final Map<UUID, Map<BlockPos, Integer>> blockBreakCooldowns = new HashMap<>();
    private static final int MOOD_COOLDOWN_TICKS = 100; // 5 seconds (20 ticks per second)

    public static void onVillagerDeath(Villager villager, ServerLevel level) {
        if (!StoneyCore.getConfig().landOptions().landVisitors()) return;
        VisitorManager manager = VisitorManager.get(level);
        if (manager.isVisitor(villager.getUUID())) {
            // Actually handle the cleanup so they don't permanently occupy the land cap
            manager.handleVisitorDeath(villager.getUUID());
        }
    }

    public static void onTradeCompleted(Villager villager, ServerLevel level) {
        if (!StoneyCore.getConfig().landOptions().landVisitors()) return;
        VisitorManager manager = VisitorManager.get(level);
        if (!manager.isVisitor(villager.getUUID())) return;

        LandVisitorData data = manager.getVisitorData(villager.getUUID());
        data.improveMood(1);
    }

    public static void onBlockPlace(Player player, ServerLevel level, BlockState placedState, BlockPos pos) {
        if (!StoneyCore.getConfig().landOptions().landVisitors()) return;
        LandState.get(level).getLandAt(pos).ifPresent(land -> {
            VisitorManager manager = VisitorManager.get(level);
            int moodChange = getBlockMoodChange(placedState);

            for (LandVisitorData data : manager.getAllVisitorData()) {
                if (data.getLandOwner().equals(land.getOwnerUUID()) && data.isVisiting()) {
                    double distance = pos.distSqr(land.getCorePos());
                    if (distance < 400) {
                        if (moodChange > 0) {
                            data.improveMood(moodChange);
                        } else if (moodChange < 0) {
                            data.worsenMood(-moodChange);
                        }
                    }
                }
            }
        });
    }

    public static void onBlockBreak(Player player, ServerLevel level, BlockState brokenState, BlockPos pos) {
        if (!StoneyCore.getConfig().landOptions().landVisitors()) return;
        UUID playerId = player.getUUID();

        if (isOnCooldown(playerId, pos)) {
            return;
        }

        addCooldown(level, playerId, pos);

        LandState.get(level).getLandAt(pos).ifPresent(land -> {
            VisitorManager manager = VisitorManager.get(level);
            int moodChange = -getBlockMoodChange(brokenState);

            for (LandVisitorData data : manager.getAllVisitorData()) {
                if (data.getLandOwner().equals(land.getOwnerUUID()) && data.isVisiting()) {
                    double distance = pos.distSqr(land.getCorePos());
                    if (distance < 400) {
                        if (moodChange > 0) {
                            data.improveMood(moodChange);
                            if (moodChange >= 10) {
                                player.displayClientMessage(
                                        Component.literal("§aThe visitors cheer as you clear the area!"),
                                        true
                                );
                            }
                        } else if (moodChange < 0) {
                            data.worsenMood(-moodChange);
                            if (moodChange <= -10) {
                                player.displayClientMessage(
                                        Component.literal("§cThe visitors look distressed by your destruction!"),
                                        true
                                );
                            }
                        }
                    }
                }
            }
        });
    }

    private static boolean isOnCooldown(UUID playerId, BlockPos pos) {
        Map<BlockPos, Integer> playerCooldowns = blockBreakCooldowns.get(playerId);
        if (playerCooldowns == null) return false;
        return playerCooldowns.containsKey(pos);
    }

    private static void addCooldown(ServerLevel level, UUID playerId, BlockPos pos) {
        Map<BlockPos, Integer> playerCooldowns = blockBreakCooldowns.computeIfAbsent(playerId, k -> new HashMap<>());
        playerCooldowns.put(pos, MOOD_COOLDOWN_TICKS);
    }

    public static void tickCooldowns() {
        if (!StoneyCore.getConfig().landOptions().landVisitors()) return;
        Map<UUID, Map<BlockPos, Integer>> toRemove = new HashMap<>();

        for (Map.Entry<UUID, Map<BlockPos, Integer>> playerEntry : blockBreakCooldowns.entrySet()) {
            UUID playerId = playerEntry.getKey();
            Map<BlockPos, Integer> cooldowns = getCooldowns(playerEntry);

            if (cooldowns.isEmpty()) {
                toRemove.put(playerId, cooldowns);
            }
        }

        for (UUID playerId : toRemove.keySet()) {
            blockBreakCooldowns.remove(playerId);
        }
    }

    private static Map<BlockPos, Integer> getCooldowns(Map.Entry<UUID, Map<BlockPos, Integer>> playerEntry) {
        Map<BlockPos, Integer> cooldowns = playerEntry.getValue();
        Map<BlockPos, Integer> toRemoveFromPlayer = new HashMap<>();

        for (Map.Entry<BlockPos, Integer> cooldownEntry : cooldowns.entrySet()) {
            BlockPos pos = cooldownEntry.getKey();
            int ticksLeft = cooldownEntry.getValue() - 1;

            if (ticksLeft <= 0) {
                toRemoveFromPlayer.put(pos, ticksLeft);
            } else {
                cooldowns.put(pos, ticksLeft);
            }
        }

        for (BlockPos pos : toRemoveFromPlayer.keySet()) {
            cooldowns.remove(pos);
        }
        return cooldowns;
    }

    private static int getBlockMoodChange(BlockState blockState) {
        if (blockState.getBlock() instanceof BedBlock) {
            return 3;
        }

        String blockName = blockState.getBlock().getDescriptionId();

        if (blockName.contains("crafting_table") ||
                blockName.contains("furnace") ||
                blockName.contains("smoker") ||
                blockName.contains("blast_furnace") ||
                blockName.contains("composter") ||
                blockName.contains("barrel") ||
                blockName.contains("lectern") ||
                blockName.contains("stonecutter") ||
                blockName.contains("grindstone") ||
                blockName.contains("loom") ||
                blockName.contains("cartography_table") ||
                blockName.contains("smithing_table") ||
                blockName.contains("fletching_table")) {
            return 2;
        }

        if (blockName.contains("flower") ||
                blockName.contains("plant") ||
                blockName.contains("sapling") ||
                blockName.contains("lantern") ||
                blockName.contains("candle") ||
                blockName.contains("painting") ||
                blockName.contains("carpet")) {
            return 1;
        }

        if (blockName.contains("farmland") ||
                blockName.contains("wheat") ||
                blockName.contains("carrot") ||
                blockName.contains("potato") ||
                blockName.contains("beetroot") ||
                blockName.contains("melon") ||
                blockName.contains("pumpkin")) {
            return 1;
        }

        return 0;
    }
}