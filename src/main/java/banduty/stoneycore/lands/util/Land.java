package banduty.stoneycore.lands.util;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.event.StartTickHandler;
import banduty.stoneycore.lands.LandType;
import banduty.stoneycore.lands.LandTypeRegistry;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.streq.StrEq;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Land {
    private final UUID owner;
    private int radius;
    private final BlockPos corePos;
    private String customName;
    private long expandItemStored = 0L;
    private final LandType landType;
    private final Set<UUID> allies = new HashSet<>();
    private final LongSet claimed = new LongOpenHashSet();

    public Land(UUID owner, BlockPos coreBlock, int radius, LandType landType, String name) {
        this.owner    = owner;
        this.corePos = coreBlock;
        this.radius   = radius;
        this.landType = landType;
        this.customName = name;
    }

    public void initializeClaim(ServerWorld world, int radiusToIncrease, Queue<ClaimWorker> taskQueue) {
        int newRadius = radius + radiusToIncrease;
        long coreX = corePos.getX();
        long coreZ = corePos.getZ();
        long limit2 = (long) newRadius * newRadius;
        LandState state = LandState.get(world);

        List<BlockPos> candidates = new ArrayList<>((int) (Math.PI * newRadius * newRadius));
        for (int dx = -newRadius; dx <= newRadius; dx++) {
            for (int dz = -newRadius; dz <= newRadius; dz++) {
                if ((long) dx * dx + (long) dz * dz <= limit2) {
                    BlockPos pos = new BlockPos((int)(coreX + dx), 0, (int)(coreZ + dz));
                    if (state.getLandAt(pos).map(l -> l.owner.equals(owner)).orElse(true)) {
                        candidates.add(pos);
                    }
                }
            }
        }

        ClaimWorker worker = getClaimWorker(world, candidates, newRadius);
        radius = newRadius;
        taskQueue.add(worker);
    }


    private @NotNull ClaimWorker getClaimWorker(ServerWorld world, List<BlockPos> candidates, int radius) {
        return new ClaimWorker(world, this, candidates, radius, success -> {});
    }

    public boolean columnInvalid(ServerWorld world, long posKey) {
        return ClaimUtils.isInvalidClaimColumn(world, BlockPos.fromLong(posKey), landType);
    }

    public boolean pathBlocked(ServerWorld world, long fromKey, long toKey) {
        return ClaimUtils.pathContainsInvalidBlock(world, BlockPos.fromLong(fromKey), BlockPos.fromLong(toKey), landType);
    }

    public void depositExpandItem(PlayerEntity player, ServerWorld world, int amount) {
        if (!(player instanceof ServerPlayerEntity serverPlayerEntity)) return;

        LandState state = LandState.get(world);
        expandItemStored += amount;

        int radiusToIncrease = 0;
        long totalCost = 0;
        double testRadius = radius;

        int maxRadius = StoneyCore.getConfig().technicalOptions.maxLandExpandRadius();
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
            player.sendMessage(Text.translatable(
                    "text.land." + landType.id().getNamespace() + ".stored", expandItemStored
            ), true);
            return;
        }

        expandItemStored -= totalCost;
        Land newLand = this.copy();
        newLand.initializeClaim(world, radiusToIncrease, StartTickHandler.CLAIM_TASKS);

        state.removeLand(this);
        state.addLand(newLand);

        sendTitle(serverPlayerEntity, Text.translatable(
                "text.land.stoneycore.expansion.increased", radius, radius + radiusToIncrease
        ));
        ((IEntityDataSaver) player).stoneycore$getPersistentData().putBoolean("land_expanded", true);
    }

    public Land copy() {
        Land copy = new Land(this.owner, this.corePos, this.radius, this.landType, this.customName);

        copy.expandItemStored = this.expandItemStored;
        copy.allies.addAll(this.allies);

        return copy;
    }

    private static void sendTitle(ServerPlayerEntity player, Text mainTitle) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeText(mainTitle);
        ServerPlayNetworking.send(player, ModMessages.LAND_TITLE_PACKET_ID, buffer);
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

    public void setRadius(int radius, ServerWorld world) {
        int actualRadius = this.radius;
        this.radius = radius;
        if (actualRadius > radius) removeClaimsOutsideRadius(world);
    }

    public void removeClaimsOutsideRadius(ServerWorld world) {
        if (claimed.isEmpty()) return;

        long coreX = corePos.getX();
        long coreZ = corePos.getZ();
        long limit2 = (long) radius * radius;

        List<Long> toRemove = new ArrayList<>();

        for (long posKey : claimed) {
            BlockPos pos = BlockPos.fromLong(posKey);
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
            LandState.get(world).unmarkClaimed(toRemove);
        }
    }

    public UUID getOwnerUUID() {
        return owner;
    }

    public static String getOwnerName(ServerWorld world, UUID uuid) {
        ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
        if (player != null) {
            return player.getGameProfile().getName();
        }

        if (world.getServer().getUserCache() == null) return "Unknown";

        GameProfile profile = world.getServer().getUserCache().getByUuid(uuid).orElse(null);
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
        claimed.forEach(posKey -> set.add(BlockPos.fromLong(posKey)));
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

    public Text getLandTitle(ServerWorld serverWorld) {
        if (hasCustomName()) return Text.literal(getCustomName());
        return Text.translatable("text.land." + getLandType().id().getNamespace() + ".land_name", getOwnerName(serverWorld, getOwnerUUID()));
    }

    public String getCustomName() {
        return customName;
    }

    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    public void setCustomName(String name) {
        this.customName = name != null && !name.isEmpty() ? name : this.customName;
    }

    public MutableText getLandTag(ServerWorld serverWorld) {
        return Text.literal("[")
                .append(getLandTitle(serverWorld))
                .append("]")
                .setStyle(Style.EMPTY.withColor(LandColors.getColorForLand(this)));
    }


    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("Owner", owner);
        nbt.putInt("Radius", radius);
        nbt.putString("CustomName", customName);
        nbt.putLong("CorePos", corePos.asLong());
        nbt.putLong("ExpandItemStored", expandItemStored);

        NbtList claims = new NbtList();
        for (long posKey : claimed) {
            claims.add(NbtLong.of(posKey));
        }
        nbt.put("Claims", claims);

        NbtList alliesList = new NbtList();
        for (UUID ally : allies) {
            NbtCompound allyNbt = new NbtCompound();
            allyNbt.putUuid("UUID", ally);
            alliesList.add(allyNbt);
        }
        nbt.put("Allies", alliesList);

        nbt.putString("LandType", landType.id().toString());

        return nbt;
    }

    public static Land fromNbt(NbtCompound nbt) {
        UUID owner   = nbt.getUuid("Owner");
        int radius   = nbt.getInt("Radius");
        String name   = nbt.getString("CustomName");
        BlockPos corePos = BlockPos.fromLong(nbt.getLong("CorePos"));
        Identifier landTypeId = new Identifier(nbt.getString("LandType"));
        LandType landType = LandTypeRegistry.getById(landTypeId)
                .orElseThrow(() -> new IllegalStateException("Unknown LandType: " + landTypeId));

        Land k = new Land(owner, corePos, radius, landType, name);
        k.expandItemStored = nbt.getLong("ExpandItemStored");
        NbtList claims = nbt.getList("Claims", NbtElement.LONG_TYPE);
        for (NbtElement claim : claims) {
            long posKey = ((NbtLong) claim).longValue();
            k.claimed.add(posKey);
        }

        if (nbt.contains("Allies", NbtElement.LIST_TYPE)) {
            NbtList alliesList = nbt.getList("Allies", NbtElement.COMPOUND_TYPE);
            for (NbtElement element : alliesList) {
                NbtCompound allyNbt = (NbtCompound) element;
                UUID allyUuid = allyNbt.getUuid("UUID");
                k.allies.add(allyUuid);
            }
        }

        return k;
    }
}