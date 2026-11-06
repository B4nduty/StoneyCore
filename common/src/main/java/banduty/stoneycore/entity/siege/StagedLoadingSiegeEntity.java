package banduty.stoneycore.entity.siege;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Engines loaded through an ordered sequence of items (cannon, ribauldequin).
 * The sequence is a {@link LoadingStage} array; the last stage must be {@link LoadingStage#ofIgnition}.
 */
public abstract class StagedLoadingSiegeEntity extends AbstractSiegeEntity {

    protected static final EntityDataAccessor<Integer> LOAD_STAGE =
            SynchedEntityData.defineId(StagedLoadingSiegeEntity.class, EntityDataSerializers.INT);

    protected StagedLoadingSiegeEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    protected abstract LoadingStage[] loadingStages();

    /** Called when the ignition stage is completed. Must start the engine's firing and cooldown. */
    protected abstract void fire(ServerLevel level, Player operator);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LOAD_STAGE, 0);
    }

    public int getLoadStage() { return entityData.get(LOAD_STAGE); }

    public void setLoadStage(int stage) { entityData.set(LOAD_STAGE, Math.max(0, stage)); }

    /** True once everything except the ignition step is loaded. */
    public boolean isReadyToFire() {
        return getLoadStage() >= loadingStages().length - 1;
    }

    @Override
    protected InteractionResult operate(Player player, ItemStack stack, ServerLevel level) {
        if (getCooldown() > 0) return InteractionResult.SUCCESS;

        LoadingStage[] stages = loadingStages();
        int index = getLoadStage();
        if (index >= stages.length) return InteractionResult.SUCCESS;

        LoadingStage required = stages[index];

        if (!required.matches(stack.getItem())) {
            player.displayClientMessage(Component.translatable("siege.loading.next",
                    required.item().getDefaultInstance().getHoverName()), true);
            return InteractionResult.SUCCESS;
        }

        if (!player.isCreative() && !required.hasEnough(stack)) {
            player.displayClientMessage(Component.translatable("siege.loading.need_amount", required.amount(),
                    required.item().getDefaultInstance().getHoverName()), true);
            return InteractionResult.FAIL;
        }

        required.apply(player, stack);

        if (required.ignites()) {
            setOwner(player);
            setLoadStage(0);
            fire(level, player);
        } else {
            setLoadStage(index + 1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LoadStage", getLoadStage());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setLoadStage(tag.getInt("LoadStage"));
    }
}
