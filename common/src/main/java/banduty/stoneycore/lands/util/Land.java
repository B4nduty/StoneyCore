package banduty.stoneycore.lands.util;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.LandType;
import banduty.stoneycore.lands.LandTypeRegistry;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.PDKeys;
import banduty.streq.StrEq;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class Land {
    private UUID owner;
    private int radius;
    private final BlockPos corePos;
    private String name;
    private final int maxAllies;
    private long expandItemStored = 0L;
    private final LandType landType;
    private final Set<UUID> allies = new HashSet<>();
    private final LongSet claimed = new LongOpenHashSet();

    public Land(UUID owner, BlockPos coreBlock, int radius, LandType landType, String name, int maxAllies) {
        this.owner    = owner;
        this.corePos = coreBlock;
        this.radius   = radius;
        this.landType = landType;
        this.name = name;
        this.maxAllies = maxAllies;
    }

    public void initializeClaim(ServerLevel serverLevel, int radius, Queue<ClaimWorker> taskQueue) {
        long coreX = corePos.getX();
        long coreZ = corePos.getZ();
        long radiusSquared = (long) radius * radius;

        LandState state = LandState.get(serverLevel);

        // Use more efficient candidate generation
        List<BlockPos> candidates = generateCandidatesInCircle(coreX, coreZ, radius, state);

        ClaimWorker worker = getClaimWorker(serverLevel, candidates, radius);
        // DON'T set the radius here - wait until the worker completes
        taskQueue.add(worker);
    }

    private List<BlockPos> generateCandidatesInCircle(long coreX, long coreZ, int radius, LandState state) {
        List<BlockPos> candidates = new ArrayList<>();
        int diameter = radius * 2;

        // Process in chunks for better memory usage
        for (int chunkX = -radius; chunkX <= radius; chunkX += 16) {
            for (int chunkZ = -radius; chunkZ <= radius; chunkZ += 16) {
                for (int dx = chunkX; dx < Math.min(chunkX + 16, radius + 1); dx++) {
                    for (int dz = chunkZ; dz < Math.min(chunkZ + 16, radius + 1); dz++) {
                        if ((long) dx * dx + (long) dz * dz <= (long) radius * radius) {
                            BlockPos pos = new BlockPos((int)(coreX + dx), 0, (int)(coreZ + dz));
                            if (state.getLandAt(pos).map(l -> l.owner.equals(owner)).orElse(true)) {
                                candidates.add(pos);
                            }
                        }
                    }
                }
            }
        }

        return candidates;
    }


    private ClaimWorker getClaimWorker(ServerLevel serverLevel, List<BlockPos> candidates, int radius) {
        return new ClaimWorker(serverLevel, this, candidates, radius, success -> {});
    }

    public void depositExpandItem(Player player, ServerLevel serverLevel, int amount) {
        if (!(player instanceof ServerPlayer serverPlayerEntity)) return;

        LandState state = LandState.get(serverLevel);
        expandItemStored += amount;

        int radiusToIncrease = 0;
        long totalCost = 0;
        double testRadius = radius;

        int maxRadius = StoneyCore.getConfig().technicalOptions().maxLandExpandRadius();
        double maxAllowedRadius = maxRadius < 0 ? Double.MAX_VALUE : maxRadius + landType.baseRadius();

        Map<String, Double> vars = new HashMap<>(1);
        String formula = landType.expandFormula();

        while (testRadius < maxAllowedRadius) {
            vars.put("radius", testRadius);
            int cost = Math.max(0, (int) StrEq.evaluate(formula, vars));

            if (totalCost + cost > expandItemStored) break;

            totalCost += cost;
            testRadius++;
            radiusToIncrease++;
        }

        if (radiusToIncrease <= 0) {
            player.displayClientMessage(Component.translatable(
                    "component.land." + landType.id().getNamespace() + ".stored", expandItemStored
            ), true);
            return;
        }

        expandItemStored -= totalCost;

        // Create a new land with the CURRENT radius (not the target radius)
        Land newLand = this.copy();
        // The initializeClaim method will handle the radius increase
        newLand.initializeClaim(serverLevel, radius + radiusToIncrease, Services.PLATFORM.getClaimTasks());

        state.removeLand(this);
        state.addLand(newLand);

        Services.PLATFORM.sendTitle(serverPlayerEntity, Component.translatable(
                "component.land.stoneycore.expansion.increased", radius, radius + radiusToIncrease
        ));
        NBTDataHelper.set((IEntityDataSaver) player, PDKeys.LAND_EXPANDED, true);
    }

    public Land copy() {
        Land copy = new Land(this.owner, this.corePos, this.radius, this.landType, this.name, this.maxAllies);

        copy.expandItemStored = this.expandItemStored;
        copy.allies.addAll(this.allies);

        return copy;
    }

    public long getNeededExpandItemAmount() {
        String formula = landType.expandFormula();
        Map<String, Double> variables = new HashMap<>();
        variables.put("radius", (double) radius);
        return Math.max(1, (int) StrEq.evaluate(formula, variables));
    }

    public BlockPos getCorePos() {
        return corePos;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius, ServerLevel serverLevel) {
        int actualRadius = this.radius;
        this.radius = radius;
        if (actualRadius > radius) removeClaimsOutsideRadius(serverLevel);
    }

    public void removeClaimsOutsideRadius(ServerLevel serverLevel) {
        if (claimed.isEmpty()) return;

        long coreX = corePos.getX();
        long coreZ = corePos.getZ();
        long limit2 = (long) radius * radius;

        List<Long> toRemove = new ArrayList<>();

        for (long posKey : claimed) {
            BlockPos pos = BlockPos.of(posKey);
            long dx = pos.getX() - coreX;
            long dz = pos.getZ() - coreZ;

            if ((dx * dx + dz * dz) > limit2) {
                toRemove.add(posKey);
            }
        }

        if (!toRemove.isEmpty()) {
            for (long key : toRemove) {
                claimed.remove(key);
            }
            LandState.get(serverLevel).unmarkClaimed(toRemove);
        }
    }

    public UUID getOwnerUUID() {
        return owner;
    }

    public void setOwnerUUID(UUID owner) {
        this.owner = owner;
    }

    public static String getOwnerName(ServerLevel serverLevel, UUID uuid) {
        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
        if (player != null) {
            return player.getGameProfile().getName();
        }

        if (serverLevel.getServer().getProfileCache() == null) return "Unknown";

        GameProfile profile = serverLevel.getServer().getProfileCache().get(uuid).orElse(null);
        return profile != null ? profile.getName() : "Unknown";
    }

    public long getExpandItemStored() {
        return expandItemStored;
    }

    public boolean isAlreadyClaimed(long key) {
        return claimed.contains(key);
    }

    public void addClaims(Collection<Long> keys) {
        claimed.addAll(keys);
    }

    public Set<BlockPos> getClaimed() {
        Set<BlockPos> set = new HashSet<>(claimed.size());
        claimed.forEach(posKey -> set.add(BlockPos.of(posKey)));
        return Collections.unmodifiableSet(set);
    }

    public Set<UUID> getAllies() {
        return Collections.unmodifiableSet(allies);
    }

    public void addAlly(UUID playerUuid) {
        allies.add(playerUuid);
    }

    public void removeAlly(UUID playerUuid) {
        allies.remove(playerUuid);
    }

    public boolean isAlly(UUID playerUuid) {
        return allies.contains(playerUuid);
    }

    public LandType getLandType() {
        return landType;
    }

    public Component getLandTitle(ServerLevel ServerLevel) {
        if (!getName().isBlank()) return Component.literal(getName());
        return Component.translatable("component.land." + getLandType().id().getNamespace() + ".land_name", getOwnerName(ServerLevel, getOwnerUUID()));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = !name.isBlank() ? name : this.name;
    }

    public MutableComponent getLandTag(ServerLevel ServerLevel) {
        return Component.literal("[")
                .append(getLandTitle(ServerLevel))
                .append("]")
                .setStyle(Style.EMPTY.withColor(LandColors.getColorForLand(this)));
    }

    public int getMaxAllies() {
        return maxAllies;
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("Owner", owner);
        nbt.putInt("Radius", radius);
        nbt.putString("CustomName", name);
        nbt.putLong("CorePos", corePos.asLong());
        nbt.putLong("ExpandItemStored", expandItemStored);
        nbt.putInt("MaxAllies", maxAllies);

        ListTag claims = new ListTag();
        for (long posKey : claimed) {
            claims.add(LongTag.valueOf(posKey));
        }
        nbt.put("Claims", claims);

        ListTag alliesList = new ListTag();
        for (UUID ally : allies) {
            alliesList.add(StringTag.valueOf(ally.toString()));
        }
        nbt.put("Allies", alliesList);

        nbt.putString("LandType", landType.id().toString());

        return nbt;
    }

    public static Land fromNbt(CompoundTag nbt) {
        UUID owner   = nbt.getUUID("Owner");
        int radius   = nbt.getInt("Radius");
        String name   = nbt.getString("CustomName");
        BlockPos corePos = BlockPos.of(nbt.getLong("CorePos"));
        ResourceLocation landTypeId = new ResourceLocation(nbt.getString("LandType"));
        int maxAllies   = nbt.getInt("MaxAllies");
        LandType landType = LandTypeRegistry.getById(landTypeId)
                .orElseThrow(() -> new IllegalStateException("Unknown LandType: " + landTypeId));

        Land k = new Land(owner, corePos, radius, landType, name, maxAllies);
        k.expandItemStored = nbt.getLong("ExpandItemStored");
        ListTag claims = nbt.getList("Claims", Tag.TAG_LONG);
        for (Tag claim : claims) {
            long posKey = ((LongTag) claim).getAsLong();
            k.claimed.add(posKey);
        }

        if (nbt.contains("Allies", Tag.TAG_LIST)) {
            ListTag alliesList = nbt.getList("Allies", Tag.TAG_STRING);
            for (Tag element : alliesList) {
                try {
                    UUID allyUuid = UUID.fromString(element.getAsString());
                    k.allies.add(allyUuid);
                } catch (IllegalArgumentException ignored) {
                    // skip invalid UUID strings
                }
            }
        }

        return k;
    }
}