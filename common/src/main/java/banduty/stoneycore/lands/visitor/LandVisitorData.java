package banduty.stoneycore.lands.visitor;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.UUID;

public class LandVisitorData {
    private final UUID visitorId;
    private final UUID landOwner;
    private final BlockPos landCore;
    private BlockPos lastKnownPos;
    private int mood;
    private int ticksVisiting = 0;
    private boolean isSettled = false;
    private final VillagerProfession profession;
    private static final int SETTLE_THRESHOLD_TICKS = 12000; // 10 minutes
    private static final int VISIT_DURATION_TICKS = 24000; // 20 minutes

    public LandVisitorData(UUID visitorId, UUID landOwner, BlockPos landCore, int mood, VillagerProfession profession) {
        this.visitorId = visitorId;
        this.landOwner = landOwner;
        this.landCore = landCore;
        this.lastKnownPos = landCore; // Default to core until updated
        this.mood = mood;
        this.profession = profession;
    }

    public void tick() {
        if (!StoneyCore.getConfig().landOptions().landVisitors()) return;
        if (isSettled) return;
        ticksVisiting++;

        // Natural mood decay over time (lose 1 mood every 2 minutes)
        if (ticksVisiting % 2400 == 0) {
            worsenMood(1);
        }

        // Random mood fluctuation
        if (ticksVisiting % 1200 == 0 && Math.random() < 0.3) {
            int fluctuation = (int) (Math.random() * 6) - 3; // -3 to +3
            if (fluctuation > 0) {
                improveMood(fluctuation);
            } else if (fluctuation < 0) {
                worsenMood(-fluctuation);
            }
        }
    }

    public boolean shouldLeave() {
        if (isSettled) return false;
        return mood < 5 || ticksVisiting > VISIT_DURATION_TICKS;
    }

    public boolean isReadyToSettle() {
        return !isSettled && ticksVisiting >= SETTLE_THRESHOLD_TICKS && mood >= 80;
    }

    public void settle() {
        this.isSettled = true;
    }

    public void improveMood(int amount) {
        this.mood = Math.min(100, this.mood + amount);
    }

    public void worsenMood(int amount) {
        this.mood = Math.max(0, this.mood - amount);
    }

    public UUID getVisitorId() { return visitorId; }
    public UUID getLandOwner() { return landOwner; }
    public BlockPos getLandCore() { return landCore; }
    public BlockPos getLastKnownPos() { return lastKnownPos; }
    public void setLastKnownPos(BlockPos pos) { this.lastKnownPos = pos; }
    public int getMood() { return mood; }
    public int getTicksVisiting() { return ticksVisiting; }
    public boolean isSettled() { return isSettled; }
    public boolean isVisiting() { return !isSettled; }
    public VillagerProfession getProfession() { return profession; }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("VisitorId", visitorId);
        tag.putUUID("LandOwner", landOwner);
        tag.putLong("LandCore", landCore.asLong());
        if (lastKnownPos != null) {
            tag.putLong("LastKnownPos", lastKnownPos.asLong());
        }
        tag.putInt("Mood", mood);
        tag.putInt("TicksVisiting", ticksVisiting);
        tag.putBoolean("IsSettled", isSettled);
        tag.putString("Profession", profession.name());
        return tag;
    }

    public static LandVisitorData fromNbt(CompoundTag tag) {
        UUID visitorId = tag.getUUID("VisitorId");
        UUID landOwner = tag.getUUID("LandOwner");
        BlockPos landCore = BlockPos.of(tag.getLong("LandCore"));
        int mood = tag.getInt("Mood");
        int ticksVisiting = tag.getInt("TicksVisiting");
        boolean isSettled = tag.getBoolean("IsSettled");
        ResourceLocation professionId = new ResourceLocation(tag.getString("Profession"));
        VillagerProfession profession = BuiltInRegistries.VILLAGER_PROFESSION.get(professionId);

        LandVisitorData data = new LandVisitorData(visitorId, landOwner, landCore, mood, profession);
        data.ticksVisiting = ticksVisiting;
        data.isSettled = isSettled;

        if (tag.contains("LastKnownPos")) {
            data.lastKnownPos = BlockPos.of(tag.getLong("LastKnownPos"));
        }

        return data;
    }
}