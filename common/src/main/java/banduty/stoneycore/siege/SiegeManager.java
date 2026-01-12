package banduty.stoneycore.siege;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandManager;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.stream.Collectors;

public class SiegeManager {
    private static final Map<ServerLevel, List<Siege>> sieges = new HashMap<>();

    public static class Siege {
        public final Land attackingLand;
        public final Land defendingLand;
        private final int initialRadius;
        public final Set<UUID> attackers = new HashSet<>();
        public final Set<UUID> defenders = new HashSet<>();
        public final Set<UUID> disabledPlayers = new HashSet<>();

        private final ServerBossEvent attackerBar;
        private final ServerBossEvent defenderBar;

        public Siege(UUID attacker, UUID defender, ServerLevel serverLevel, Land attackingLand, Land defendingLand) {
            this.attackingLand = attackingLand;
            this.defendingLand = defendingLand;
            this.initialRadius = defendingLand.getRadius();
            String attackerName = LandManager.getLandName(serverLevel, attacker).getString();
            String defenderName = LandManager.getLandName(serverLevel, defender).getString();

            attackerBar = new ServerBossEvent(
                    Component.literal("Defenders Remaining: " + defenderName),
                    BossEvent.BossBarColor.BLUE,
                    BossEvent.BossBarOverlay.NOTCHED_10
            );
            attackerBar.setProgress(1.0f);

            defenderBar = new ServerBossEvent(
                    Component.literal("Attackers Remaining: " + attackerName),
                    BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.NOTCHED_10
            );
            defenderBar.setProgress(1.0f);
        }

        public boolean isParticipant(UUID playerId) {
            return attackers.contains(playerId) || defenders.contains(playerId);
        }

        public boolean isDisabled(UUID playerId) {
            return disabledPlayers.contains(playerId);
        }

        public void disablePlayer(UUID playerId, ServerLevel serverLevel) {
            disabledPlayers.add(playerId);
            if (StoneyCore.getConfig().landOptions().removeClaimedSiege()) updateDefendingLandRadius(serverLevel);
        }

        private void updateDefendingLandRadius(ServerLevel serverLevel) {
            int totalDefenders = defenders.size();
            if (totalDefenders == 0) return;

            long deadDefenders = defenders.stream()
                    .filter(disabledPlayers::contains)
                    .count();

            int blocksPerDefender = Math.max(initialRadius / totalDefenders, 1);

            int previousRadius = defendingLand.getRadius();
            int newRadius = initialRadius - (int) (deadDefenders * blocksPerDefender);
            newRadius = Math.max(newRadius, 3);

            if (newRadius != previousRadius) {
                defendingLand.setRadius(newRadius, serverLevel);

                for (UUID uuid : defenders) {
                    ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
                    if (player != null) {
                        Services.PLATFORM.sendTitle(player, Component.literal(previousRadius + " --> " + newRadius));
                    }
                }
            }
        }

        public boolean isActive() {
            return !attackers.isEmpty() || !defenders.isEmpty();
        }

        public void updateBossEvent(ServerLevel serverLevel) {
            int totalAttackers = attackers.size();
            int totalDefenders = defenders.size();

            Set<UUID> onlineAttackers = attackers.stream()
                    .filter(uuid -> !disabledPlayers.contains(uuid))
                    .filter(uuid -> serverLevel.getServer().getPlayerList().getPlayer(uuid) != null)
                    .collect(Collectors.toSet());

            Set<UUID> onlineDefenders = defenders.stream()
                    .filter(uuid -> !disabledPlayers.contains(uuid))
                    .filter(uuid -> serverLevel.getServer().getPlayerList().getPlayer(uuid) != null)
                    .collect(Collectors.toSet());

            int aliveAttackers = onlineAttackers.size();
            int aliveDefenders = onlineDefenders.size();

            float defendersRatio = totalDefenders == 0 ? 0f : (float) aliveDefenders / totalDefenders;
            float attackersRatio = totalAttackers == 0 ? 0f : (float) aliveAttackers / totalAttackers;

            attackerBar.setName(Component.translatable("Component.land." + attackingLand.getLandType().id().getNamespace() + ".defenders_remaining",
                    attackingLand.getLandTitle(serverLevel).getString(), defendingLand.getLandTitle(serverLevel).getString(), aliveDefenders, totalDefenders));
            defenderBar.setName(Component.translatable("Component.land." + defendingLand.getLandType().id().getNamespace() + ".attackers_remaining",
                    attackingLand.getLandTitle(serverLevel).getString(), defendingLand.getLandTitle(serverLevel).getString(), aliveAttackers, totalAttackers));

            attackerBar.setProgress(defendersRatio);
            defenderBar.setProgress(attackersRatio);

            clearBossEvents();

            for (UUID uuid : attackers) {
                ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
                if (player != null) attackerBar.addPlayer(player);
            }

            for (UUID uuid : defenders) {
                ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
                if (player != null) defenderBar.addPlayer(player);
            }
        }

        public void clearBossEvents() {
            attackerBar.removeAllPlayers();
            defenderBar.removeAllPlayers();
        }
    }

    public static Optional<Siege> getPlayerSiege(ServerLevel serverLevel, UUID playerId) {
        List<Siege> levelSieges = sieges.get(serverLevel);
        if (levelSieges == null) return Optional.empty();

        return levelSieges.stream()
                .filter(siege -> siege.isParticipant(playerId))
                .findFirst();
    }

    public static boolean isPlayerInLandUnderSiege(ServerLevel serverLevel, Player player) {
        Optional<Land> land = LandManager.getLandByPosition(serverLevel, player.getOnPos());
        return land.isPresent() && isLandDefenseSiege(serverLevel, land.get());
    }

    public static boolean isLandDefenseSiege(ServerLevel serverLevel, Land land) {
        List<Siege> levelSieges = sieges.getOrDefault(serverLevel, Collections.emptyList());
        return levelSieges.stream().anyMatch(siege -> siege.defendingLand.equals(land));
    }

    public static boolean isLandAttackingSiege(ServerLevel serverLevel, Land land) {
        List<Siege> levelSieges = sieges.getOrDefault(serverLevel, Collections.emptyList());
        return levelSieges.stream().anyMatch(siege -> siege.attackingLand.equals(land));
    }

    public static void tick(ServerLevel serverLevel) {
        List<Siege> levelSieges = sieges.get(serverLevel);
        if (levelSieges == null) return;

        LandState landState = LandState.get(serverLevel);
        PlayerList playerManager = serverLevel.getServer().getPlayerList();
        Iterator<Siege> iterator = levelSieges.iterator();

        while (iterator.hasNext()) {
            Siege siege = iterator.next();

            Set<UUID> onlineAttackers = siege.attackers.stream()
                    .filter(uuid -> !siege.disabledPlayers.contains(uuid))
                    .filter(uuid -> serverLevel.getServer().getPlayerList().getPlayer(uuid) != null)
                    .collect(Collectors.toSet());

            int aliveAttackers = onlineAttackers.size();

            boolean defendingExists = landState.isLandRegistered(siege.defendingLand);
            boolean attackingExists = landState.isLandRegistered(siege.attackingLand);

            // End siege if either land is destroyed
            if (!defendingExists || !attackingExists || aliveAttackers == 0) {
                boolean attackersWon = !defendingExists;

                Land winner = attackersWon ? siege.attackingLand : siege.defendingLand;
                Land loser = attackersWon ? siege.defendingLand : siege.attackingLand;
                Set<UUID> winners = attackersWon ? siege.attackers : siege.defenders;
                Set<UUID> losers = attackersWon ? siege.defenders : siege.attackers;

                sendVictoryTitles(winners, playerManager, winner, true);
                sendVictoryTitles(losers, playerManager, loser, false);

                siege.clearBossEvents();
                iterator.remove();
            } else {
                siege.updateBossEvent(serverLevel);
            }
        }
    }

    private static void sendVictoryTitles(Set<UUID> players, PlayerList playerManager, Land land, boolean won) {
        for (UUID uuid : players) {
            ServerPlayer player = playerManager.getPlayer(uuid);
            if (player != null) {
                String namespace = land.getLandType().id().getNamespace();
                Component victoryLoseKey = Component.translatable("Component.land." + namespace + (won ? ".winner" : ".loser"));
                Services.PLATFORM.sendTitle(player, victoryLoseKey);
            }
        }
    }

    public static void startSiege(ServerLevel serverLevel, UUID attacker, UUID defender) {
        List<Siege> levelSieges = sieges.computeIfAbsent(serverLevel, k -> new ArrayList<>());

        LandState stateManager = LandState.get(serverLevel);
        Optional<Land> maybeAttackingLand = stateManager.getLandByOwner(attacker);
        Optional<Land> maybeDefenderLand = stateManager.getLandByOwner(defender);

        if (maybeAttackingLand.isEmpty() || maybeDefenderLand.isEmpty()) return;

        Land attackingLand = maybeAttackingLand.get();
        Land defendingLand = maybeDefenderLand.get();

        // Check if siege already exists
        for (Siege siege : levelSieges) {
            if (siege.attackingLand == null || siege.defendingLand == null) continue;

            if (siege.attackingLand.equals(attackingLand) && siege.defendingLand.equals(defendingLand)) {
                // Siege already exists, don't create another one
                return;
            }
        }

        // No existing siege — create and populate a new one
        Siege newSiege = new Siege(attacker, defender, serverLevel, attackingLand, defendingLand);

        newSiege.attackers.add(attackingLand.getOwnerUUID());
        newSiege.attackers.addAll(attackingLand.getAllies());

        newSiege.defenders.add(defendingLand.getOwnerUUID());
        newSiege.defenders.addAll(defendingLand.getAllies());

        levelSieges.add(newSiege);
    }

    public static Set<ServerPlayer> getAllAttackingPlayers(ServerLevel serverLevel) {
        PlayerList playerManager = serverLevel.getServer().getPlayerList();
        List<Siege> levelSieges = sieges.getOrDefault(serverLevel, Collections.emptyList());

        return levelSieges.stream()
                .flatMap(siege -> siege.attackers.stream())
                .map(playerManager::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static Set<ServerPlayer> getAllDefendingPlayers(ServerLevel serverLevel) {
        PlayerList playerManager = serverLevel.getServer().getPlayerList();
        List<Siege> levelSieges = sieges.getOrDefault(serverLevel, Collections.emptyList());

        return levelSieges.stream()
                .flatMap(siege -> siege.defenders.stream())
                .map(playerManager::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static Set<ServerPlayer> getAllParticipants(ServerLevel serverLevel) {
        Set<ServerPlayer> participants = new HashSet<>();
        participants.addAll(getAllAttackingPlayers(serverLevel));
        participants.addAll(getAllDefendingPlayers(serverLevel));
        return participants;
    }

    public static InteractionResult surrender(ServerLevel serverLevel, Player player, Land land) {
        LandState stateManager = LandState.get(serverLevel);
        for (ServerPlayer serverPlayerEntity : SiegeManager.getAllParticipants(serverLevel)) {
            Services.PLATFORM.sendTitle(serverPlayerEntity, Component.translatable("Component.land." + land.getLandType().id().getNamespace() + ".surrender", land.getLandTitle(serverLevel).getString()));
        }
        stateManager.removeLand(land);
        player.getMainHandItem().shrink(1);
        return InteractionResult.SUCCESS;
    }

    public static List<Siege> getSiegesForLevel(ServerLevel serverLevel) {
        return sieges.computeIfAbsent(serverLevel, k -> new ArrayList<>());
    }
}
