package banduty.stoneycore.lands.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.*;

public class LandState extends PersistentState {
    private final Map<BlockPos, Land> landMap = new HashMap<>();
    private final Map<BlockPos, Land> claimedChunks = new HashMap<>();
    private final Map<UUID, Land> ownerMap = new HashMap<>();

    private static BlockPos toColumn(BlockPos pos) {
        return new BlockPos(pos.getX(), 0, pos.getZ());
    }

    public static LandState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
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
        markDirty();
    }

    public void removeLand(Land land) {
        landMap.remove(land.getCorePos());
        ownerMap.remove(land.getOwnerUUID());
        for (BlockPos pos : land.getClaimed()) {
            claimedChunks.remove(pos);
        }
        markDirty();
    }

    public Optional<Land> getLandByOwner(UUID owner) {
        return Optional.ofNullable(ownerMap.get(owner));
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
            BlockPos pos = BlockPos.fromLong(key);
            claimedChunks.put(pos, land);
        }
        markDirty();
    }

    public void unmarkClaimed(Collection<Long> keys) {
        for (long key : keys) {
            BlockPos pos = BlockPos.fromLong(key);
            claimedChunks.remove(pos);
        }
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        for (Land land : landMap.values()) {
            list.add(land.toNbt());
        }
        nbt.put("Lands", list);
        return nbt;
    }

    public static LandState fromNbt(NbtCompound nbt) {
        LandState state = new LandState();
        NbtList list = nbt.getList("Lands", NbtElement.COMPOUND_TYPE);
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
