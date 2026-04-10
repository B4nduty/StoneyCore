package banduty.stoneycore.lands.visitor;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.util.Land;
import banduty.stoneycore.lands.util.LandState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class VisitorManager extends SavedData {
    private final Map<UUID, LandVisitorData> visitorData = new HashMap<>();
    private int ticksUntilSpawnTry = 0;

    public static VisitorManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                VisitorManager::fromNbt,
                VisitorManager::new,
                "visitor_manager"
        );
    }

    public void tick(ServerLevel level) {
        if (!StoneyCore.getConfig().landOptions().landVisitors()) return;
        List<UUID> toRemove = new ArrayList<>();

        for (Map.Entry<UUID, LandVisitorData> entry : visitorData.entrySet()) {
            UUID visitorId = entry.getKey();
            LandVisitorData data = entry.getValue();

            Entity entity = level.getEntity(visitorId);

            // If entity is null, the chunk is unloaded. Freeze their data tick.
            if (entity == null) {
                continue;
            }

            // If entity is dead or removed from the world, queue for cleanup.
            if (!entity.isAlive()) {
                toRemove.add(visitorId);
                continue;
            }

            Villager visitor = (Villager) entity;
            data.setLastKnownPos(visitor.blockPosition()); // Update position dynamically
            data.tick();

            if (data.shouldLeave()) {
                toRemove.add(visitorId);
                removeVisitor(level, visitorId, true);
            } else if (data.isReadyToSettle()) {
                settleVisitor(level, visitorId, data);
            }
        }

        if (!toRemove.isEmpty()) {
            toRemove.forEach(visitorData::remove);
            setDirty();
        }

        if (ticksUntilSpawnTry++ >= 5 * 20) {
            trySpawnVisitor(level);
            ticksUntilSpawnTry = 0;
        }
    }

    private void trySpawnVisitor(ServerLevel level) {
        LandState landState = LandState.get(level);

        List<Land> eligibleLands = landState.getAllLands().stream()
                .filter(land -> canReceiveVisitor(land, level))
                .toList();

        if (eligibleLands.isEmpty()) return;

        Land targetLand = eligibleLands.get(new Random().nextInt(eligibleLands.size()));
        double spawnChance = targetLand.getLandType().spawnChance();

        if (spawnChance <= 0 || Math.random() >= spawnChance) return;

        BlockPos spawnPoint = findSpawnPointOutsideClaim(level, targetLand);
        if (spawnPoint == null) return;

        Villager visitor = new Villager(EntityType.VILLAGER, level);
        visitor.setPos(spawnPoint.getX() + 0.5, spawnPoint.getY(), spawnPoint.getZ() + 0.5);

        level.addFreshEntity(visitor);
        moveTo(visitor, targetLand, visitor.getX(), visitor.getY(), visitor.getZ(), 1.0);

        LandVisitorData data = new LandVisitorData(
                visitor.getUUID(),
                targetLand.getOwnerUUID(),
                targetLand.getCorePos(),
                generateMood(targetLand),
                VillagerProfession.NONE
        );

        data.setLastKnownPos(spawnPoint);
        visitorData.put(visitor.getUUID(), data);
        setDirty();

        notifyLandOwner(level, targetLand, "visitor_spawned");
        StoneyCore.LOG.info("[VisitorManager] Spawned visitor for land owned by {}",
                Land.getOwnerName(level, targetLand.getOwnerUUID()));
    }

    public void moveTo(Villager villager, Land targetLand, double x, double y, double z, double speed) {
        villager.getNavigation().moveTo(villager.getNavigation().createPath(x, y, z, targetLand.getRadius() - 5), speed);
    }

    private void notifyLandOwner(ServerLevel level, Land land, String eventType) {
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(land.getOwnerUUID());
        if (owner == null) return;

        switch (eventType) {
            case "visitor_spawned":
                owner.displayClientMessage(Component.literal("§e A villager is visiting your land!"), false);
                owner.displayClientMessage(Component.literal("§7Make them happy by providing beds and workstations!"), false);
                break;
            case "visitor_settled":
                owner.displayClientMessage(Component.literal("§a A villager has decided to settle in your land!"), false);
                owner.displayClientMessage(Component.literal("§aThey will now live here permanently and can trade with you!"), false);
                break;
            case "visitor_left":
                owner.displayClientMessage(Component.literal("§c A visitor has left your land due to unhappiness..."), false);
                break;
        }
    }

    private boolean canReceiveVisitor(Land land, ServerLevel level) {
        LandVisitorData data = getVisitorDataForLand(land.getOwnerUUID());
        if (data != null && data.isVisiting()) return false;

        int maxVisitors = land.getLandType().maxVisitorsPerLand();
        long currentVisitors = visitorData.values().stream()
                .filter(v -> v.getLandOwner().equals(land.getOwnerUUID()))
                .count();

        return currentVisitors < maxVisitors && land.getClaimed().size() > 10;
    }

    private BlockPos findSpawnPointOutsideClaim(ServerLevel level, Land land) {
        Random random = new Random();
        int radius = land.getRadius() + 6 + random.nextInt(10);

        for (int attempts = 0; attempts < 50; attempts++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            int x = land.getCorePos().getX() + (int)(Math.cos(angle) * radius);
            int z = land.getCorePos().getZ() + (int)(Math.sin(angle) * radius);

            BlockPos spawnPos = new BlockPos(x, level.getMinBuildHeight(), z);
            spawnPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, spawnPos);

            if (!LandState.get(level).isClaimed(spawnPos)) {
                return spawnPos.offset(0, 1, 0);
            }
        }
        return null;
    }

    private int generateMood(Land land) {
        int baseMood = Math.min(10, land.getClaimed().size() / 20) + Math.min(20, land.getAllies().size());
        return Math.max(10, baseMood + new Random().nextInt(10) - 5);
    }

    private void settleVisitor(ServerLevel level, UUID visitorId, LandVisitorData data) {
        Entity entity = level.getEntity(visitorId);
        if (!(entity instanceof Villager visitor)) return;

        data.settle();
        visitor.setVillagerData(visitor.getVillagerData().setProfession(data.getProfession()));
        visitor.setPersistenceRequired();

        LandState.get(level).getLandAtCorePos(data.getLandCore()).ifPresent(land ->
                notifyLandOwner(level, land, "visitor_settled"));

        StoneyCore.LOG.debug("[VisitorManager] Visitor {} settled", visitor.getName().getString());
        setDirty();
    }

    private void removeVisitor(ServerLevel level, UUID visitorId, boolean isLeaving) {
        Entity entity = level.getEntity(visitorId);
        LandVisitorData data = visitorData.get(visitorId);

        if (entity != null) {
            entity.discard();
        }

        if (isLeaving && data != null && data.isVisiting() && data.getMood() < 20) {
            LandState.get(level).getLandAtCorePos(data.getLandCore()).ifPresent(land ->
                    notifyLandOwner(level, land, "visitor_left"));
        }

        // Note: The removal from the map is handled gracefully in the tick loop or public handle method.
    }

    // Called from VisitorTracker when a villager actually dies
    public void handleVisitorDeath(UUID visitorId) {
        if (visitorData.remove(visitorId) != null) {
            setDirty();
        }
    }

    public LandVisitorData getVisitorDataForLand(UUID landOwner) {
        return visitorData.values().stream()
                .filter(v -> v.getLandOwner().equals(landOwner) && v.isVisiting())
                .findFirst()
                .orElse(null);
    }

    public boolean isVisitor(UUID entityId) { return visitorData.containsKey(entityId); }
    public LandVisitorData getVisitorData(UUID entityId) { return visitorData.get(entityId); }

    public Optional<UUID> getVisitorAtPosition(BlockPos pos) {
        return visitorData.entrySet().stream()
                .filter(entry -> entry.getValue().getLastKnownPos() != null && entry.getValue().getLastKnownPos().equals(pos))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public Optional<BlockPos> getVisitorPosition(UUID visitorId) {
        LandVisitorData data = visitorData.get(visitorId);
        return data != null ? Optional.ofNullable(data.getLastKnownPos()) : Optional.empty();
    }

    public Collection<UUID> getAllVisitors() { return Collections.unmodifiableSet(visitorData.keySet()); }
    public Collection<LandVisitorData> getAllVisitorData() { return Collections.unmodifiableCollection(visitorData.values()); }

    public int getVisitorCountForLand(UUID landOwner) {
        return (int) visitorData.values().stream().filter(v -> v.getLandOwner().equals(landOwner)).count();
    }

    public int getActiveVisitorCountForLand(UUID landOwner) {
        return (int) visitorData.values().stream().filter(v -> v.getLandOwner().equals(landOwner) && v.isVisiting()).count();
    }

    public int getSettledCountForLand(UUID landOwner) {
        return (int) visitorData.values().stream().filter(v -> v.getLandOwner().equals(landOwner) && v.isSettled()).count();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (LandVisitorData data : visitorData.values()) {
            list.add(data.toNbt());
        }
        tag.put("Visitors", list);
        return tag;
    }

    public static VisitorManager fromNbt(CompoundTag tag) {
        VisitorManager manager = new VisitorManager();
        ListTag list = tag.getList("Visitors", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            LandVisitorData data = LandVisitorData.fromNbt(list.getCompound(i));
            manager.visitorData.put(data.getVisitorId(), data);
        }
        return manager;
    }
}