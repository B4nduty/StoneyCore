package banduty.stoneycore.lands.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class LandState extends SavedData {
    private final Map<BlockPos, Land> landMap = new HashMap<>();
    private final Map<BlockPos, Land> claimedChunks = new HashMap<>();
    private final Map<UUID, Land> ownerMap = new HashMap<>();

    private static BlockPos toColumn(BlockPos pos) {
        return new BlockPos(pos.getX(), 0, pos.getZ());
    }

    public static LandState get(ServerLevel serverLevel) {
        return serverLevel.getDataStorage().computeIfAbsent(
                LandState::fromNbt,
                LandState::new,
                "lands"
        );
    }

    public void addLand(Land land) {
        landMap.put(land.getCorePos(), land);
        ownerMap.put(land.getOwnerUUID(), land);
        for (BlockPos pos : land.getClaimed()) {
            claimedChunks.put(pos, land);
        }
        setDirty();
    }

    public void removeLand(Land land) {
        landMap.remove(land.getCorePos());
        ownerMap.remove(land.getOwnerUUID());
        for (BlockPos pos : land.getClaimed()) {
            claimedChunks.remove(pos);
        }
        setDirty();
    }

    public Optional<Land> getLandByOwner(UUID owner) {
        return Optional.ofNullable(ownerMap.get(owner));
    }

    public Optional<Land> getLandByPlayer(UUID playerUUID) {
        Land land = ownerMap.get(playerUUID);
        if (land != null) return Optional.of(land);

        for (Land l : landMap.values()) {
            if (l.isAlly(playerUUID)) return Optional.of(l);
        }

        return Optional.empty();
    }


    public Collection<Land> getAllLands() {
        return Collections.unmodifiableCollection(landMap.values());
    }

    public boolean isLandRegistered(Land land) {
        return landMap.containsKey(land.getCorePos());
    }

    public boolean isClaimed(BlockPos pos) {
        return getLandAt(pos).isPresent();
    }

    public boolean isOwner(BlockPos pos, UUID uuid) {
        return getLandAt(pos).map(k -> k.getOwnerUUID().equals(uuid)).orElse(false);
    }

    public boolean isAllay(BlockPos pos, UUID uuid) {
        return getLandAt(pos).map(k -> k.isAlly(uuid)).orElse(false);
    }

    public Optional<Land> getLandAt(BlockPos pos) {
        BlockPos column = toColumn(pos);
        return Optional.ofNullable(claimedChunks.get(column));
    }

    public Optional<Land> getLandAtCorePos(BlockPos blockPos) {
        return Optional.ofNullable(landMap.get(blockPos));
    }

    public void markClaimed(Collection<Long> keys, Land land) {
        for (long key : keys) {
            BlockPos pos = BlockPos.of(key);
            claimedChunks.put(pos, land);
        }
        setDirty();
    }

    public void unmarkClaimed(Collection<Long> keys) {
        for (long key : keys) {
            BlockPos pos = BlockPos.of(key);
            claimedChunks.remove(pos);
        }
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag list = new ListTag();
        for (Land land : landMap.values()) {
            list.add(land.toNbt());
        }
        compoundTag.put("Lands", list);
        return compoundTag;
    }

    public static LandState fromNbt(CompoundTag compoundTag) {
        LandState state = new LandState();
        ListTag list = compoundTag.getList("Lands", Tag.TAG_COMPOUND);
        for (int i = 0, size = list.size(); i < size; i++) {
            Land land = Land.fromNbt(list.getCompound(i));
            state.landMap.put(land.getCorePos(), land);
            state.ownerMap.put(land.getOwnerUUID(), land);
            for (BlockPos pos : land.getClaimed()) {
                state.claimedChunks.put(pos, land);
            }
        }
        return state;
    }
}
