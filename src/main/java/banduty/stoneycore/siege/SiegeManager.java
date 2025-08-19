package banduty.stoneycore.siege;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandManager;
import banduty.stoneycore.lands.util.LandState;
import banduty.stoneycore.networking.ModMessages;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import java.util.*;
import java.util.stream.Collectors;

public class SiegeManager {
    private static final Map<ServerWorld, List<Siege>> sieges = new HashMap<>();

    public static class Siege {
        public final Land attackingLand;
        public final Land defendingLand;
        private final int initialRadius;
        public final Set<UUID> attackers = new HashSet<>();
        public final Set<UUID> defenders = new HashSet<>();
        public final Set<UUID> disabledPlayers = new HashSet<>();

        private final ServerBossBar attackerBar;
        private final ServerBossBar defenderBar;

        public Siege(UUID attacker, UUID defender, ServerWorld world, Land attackingLand, Land defendingLand) {
            this.attackingLand = attackingLand;
            this.defendingLand = defendingLand;
            this.initialRadius = defendingLand.getRadius();
            String attackerName = LandManager.getLandName(world, attacker).getString();
            String defenderName = LandManager.getLandName(world, defender).getString();

            attackerBar = new ServerBossBar(
                    Text.literal("Defenders Remaining: " + defenderName),
                    BossBar.Color.BLUE,
                    BossBar.Style.NOTCHED_10
            );
            attackerBar.setPercent(1.0f);

            defenderBar = new ServerBossBar(
                    Text.literal("Attackers Remaining: " + attackerName),
                    BossBar.Color.RED,
                    BossBar.Style.NOTCHED_10
            );
            defenderBar.setPercent(1.0f);
        }

        public boolean isParticipant(UUID playerId) {
            return attackers.contains(playerId) || defenders.contains(playerId);
        }

        public boolean isDisabled(UUID playerId) {
            return disabledPlayers.contains(playerId);
        }

        public void disablePlayer(UUID playerId, ServerWorld world) {
            disabledPlayers.add(playerId);
            if (StoneyCore.getConfig().landOptions.removeClaimedSiege()) updateDefendingLandRadius(world);
        }

        private void updateDefendingLandRadius(ServerWorld world) {
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
                defendingLand.setRadius(newRadius, world);

                for (UUID uuid : defenders) {
                    ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
                    if (player != null) {
                        sendTitle(player, Text.literal(previousRadius + " ➝ " + newRadius));
                    }
                }
            }
        }

        public boolean isActive() {
            return !attackers.isEmpty() || !defenders.isEmpty();
        }

        public void updateBossBar(ServerWorld world) {
            int totalAttackers = attackers.size();
            int totalDefenders = defenders.size();

            Set<UUID> onlineAttackers = attackers.stream()
                    .filter(uuid -> !disabledPlayers.contains(uuid))
                    .filter(uuid -> world.getServer().getPlayerManager().getPlayer(uuid) != null)
                    .collect(Collectors.toSet());

            Set<UUID> onlineDefenders = defenders.stream()
                    .filter(uuid -> !disabledPlayers.contains(uuid))
                    .filter(uuid -> world.getServer().getPlayerManager().getPlayer(uuid) != null)
                    .collect(Collectors.toSet());

            int aliveAttackers = onlineAttackers.size();
            int aliveDefenders = onlineDefenders.size();

            float defendersRatio = totalDefenders == 0 ? 0f : (float) aliveDefenders / totalDefenders;
            float attackersRatio = totalAttackers == 0 ? 0f : (float) aliveAttackers / totalAttackers;

            attackerBar.setName(Text.translatable("text.land." + attackingLand.getLandType().id().getNamespace() + ".defenders_remaining",
                    attackingLand.getLandTitle(world).getString(), defendingLand.getLandTitle(world).getString(), aliveDefenders, totalDefenders));
            defenderBar.setName(Text.translatable("text.land." + defendingLand.getLandType().id().getNamespace() + ".attackers_remaining",
                    attackingLand.getLandTitle(world).getString(), defendingLand.getLandTitle(world).getString(), aliveAttackers, totalAttackers));

            attackerBar.setPercent(defendersRatio);
            defenderBar.setPercent(attackersRatio);

            attackerBar.clearPlayers();
            defenderBar.clearPlayers();

            for (UUID uuid : attackers) {
                ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
                if (player != null) attackerBar.addPlayer(player);
            }

            for (UUID uuid : defenders) {
                ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
                if (player != null) defenderBar.addPlayer(player);
            }
        }

        public void clearBossBars() {
            attackerBar.clearPlayers();
            defenderBar.clearPlayers();
        }
    }

    public static Optional<Siege> getPlayerSiege(ServerWorld world, UUID playerId) {
        List<Siege> worldSieges = sieges.get(world);
        if (worldSieges == null) return Optional.empty();

        return worldSieges.stream()
                .filter(siege -> siege.isParticipant(playerId))
                .findFirst();
    }

    public static boolean isPlayerInLandUnderSiege(ServerWorld world, PlayerEntity player) {
        Optional<Land> land = LandManager.getLandByPosition(world, player.getBlockPos());
        return land.isPresent() && isLandDefenseSiege(world, land.get());
    }

    public static boolean isLandDefenseSiege(ServerWorld world, Land land) {
        List<Siege> worldSieges = sieges.getOrDefault(world, Collections.emptyList());
        return worldSieges.stream().anyMatch(siege -> siege.defendingLand.equals(land));
    }

    public static boolean isLandAttackingSiege(ServerWorld world, Land land) {
        List<Siege> worldSieges = sieges.getOrDefault(world, Collections.emptyList());
        return worldSieges.stream().anyMatch(siege -> siege.attackingLand.equals(land));
    }

    public static void tick(ServerWorld world) {
        List<Siege> worldSieges = sieges.get(world);
        if (worldSieges == null) return;

        LandState landState = LandState.get(world);
        PlayerManager playerManager = world.getServer().getPlayerManager();
        Iterator<Siege> iterator = worldSieges.iterator();

        while (iterator.hasNext()) {
            Siege siege = iterator.next();

            Set<UUID> onlineAttackers = siege.attackers.stream()
                    .filter(uuid -> !siege.disabledPlayers.contains(uuid))
                    .filter(uuid -> world.getServer().getPlayerManager().getPlayer(uuid) != null)
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

                siege.clearBossBars();
                iterator.remove();
            } else {
                siege.updateBossBar(world);
            }
        }
    }

    private static void sendVictoryTitles(Set<UUID> players, PlayerManager playerManager, Land land, boolean won) {
        for (UUID uuid : players) {
            ServerPlayerEntity player = playerManager.getPlayer(uuid);
            if (player != null) {
                String namespace = land.getLandType().id().getNamespace();
                Text victoryLoseKey = Text.translatable("text.land." + namespace + (won ? ".winner" : ".loser"));
                PacketByteBuf buffer = PacketByteBufs.create();
                buffer.writeText(victoryLoseKey);
                ServerPlayNetworking.send(player, ModMessages.LAND_TITLE_PACKET_ID, buffer);
            }
        }
    }

    public static void startSiege(ServerWorld world, UUID attacker, UUID defender) {
        List<Siege> worldSieges = sieges.computeIfAbsent(world, k -> new ArrayList<>());

        LandState stateManager = LandState.get(world);
        Optional<Land> maybeAttackingLand = stateManager.getLandByOwner(attacker);
        Optional<Land> maybeDefenderLand = stateManager.getLandByOwner(defender);

        if (maybeAttackingLand.isEmpty() || maybeDefenderLand.isEmpty()) return;

        Land attackingLand = maybeAttackingLand.get();
        Land defendingLand = maybeDefenderLand.get();

        // Check if siege already exists
        for (Siege siege : worldSieges) {
            if (siege.attackingLand == null || siege.defendingLand == null) continue;

            if (siege.attackingLand.equals(attackingLand) && siege.defendingLand.equals(defendingLand)) {
                // Siege already exists, don't create another one
                return;
            }
        }

        // No existing siege — create and populate a new one
        Siege newSiege = new Siege(attacker, defender, world, attackingLand, defendingLand);

        newSiege.attackers.add(attackingLand.getOwnerUUID());
        newSiege.attackers.addAll(attackingLand.getAllies());

        newSiege.defenders.add(defendingLand.getOwnerUUID());
        newSiege.defenders.addAll(defendingLand.getAllies());

        worldSieges.add(newSiege);
    }

    public static Set<ServerPlayerEntity> getAllAttackingPlayers(ServerWorld world) {
        PlayerManager playerManager = world.getServer().getPlayerManager();
        List<Siege> worldSieges = sieges.getOrDefault(world, Collections.emptyList());

        return worldSieges.stream()
                .flatMap(siege -> siege.attackers.stream())
                .map(playerManager::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static Set<ServerPlayerEntity> getAllDefendingPlayers(ServerWorld world) {
        PlayerManager playerManager = world.getServer().getPlayerManager();
        List<Siege> worldSieges = sieges.getOrDefault(world, Collections.emptyList());

        return worldSieges.stream()
                .flatMap(siege -> siege.defenders.stream())
                .map(playerManager::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static Set<ServerPlayerEntity> getAllParticipants(ServerWorld world) {
        Set<ServerPlayerEntity> participants = new HashSet<>();
        participants.addAll(getAllAttackingPlayers(world));
        participants.addAll(getAllDefendingPlayers(world));
        return participants;
    }

    public static ActionResult surrender(ServerWorld serverWorld, PlayerEntity player, Land land) {
        LandState stateManager = LandState.get(serverWorld);
        for (ServerPlayerEntity serverPlayerEntity : SiegeManager.getAllParticipants(serverWorld)) {
            sendTitle(serverPlayerEntity, Text.translatable("text.land." + land.getLandType().id().getNamespace() + ".surrender", land.getLandTitle(serverWorld).getString()));
        }
        stateManager.removeLand(land);
        player.getMainHandStack().decrement(1);
        return ActionResult.SUCCESS;
    }

    public static void sendTitle(ServerPlayerEntity player, Text mainTitle) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeText(mainTitle);
        ServerPlayNetworking.send(player, ModMessages.LAND_TITLE_PACKET_ID, buffer);
    }
}
