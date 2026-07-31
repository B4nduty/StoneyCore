package banduty.stoneycore.items.custom.hotiron;

import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public interface QuenchItem {

    int getIgniteDuration();

    boolean destroysOnQuench();

    default Item getQuenchResult() {
        return Items.IRON_INGOT;
    }

    default boolean isFinished(ItemStack stack) {
        return stack.getOrDefault(SCDataComponents.FINISHED.get(), false);
    }

    default void setFinished(ItemStack stack) {
        stack.set(SCDataComponents.FINISHED.get(), true);
    }

    default Long getIgniteTime(ItemStack stack) {
        return stack.get(SCDataComponents.IGNITE_TIME.get());
    }

    default boolean isIgnited(ItemStack stack) {
        return getIgniteTime(stack) != null;
    }

    default void igniteItem(ItemStack stack, Entity entity) {
        stack.set(SCDataComponents.IGNITE_TIME.get(), entity.level().getGameTime());
    }

    default void unlimitedItem(ItemStack stack) {
        stack.remove(SCDataComponents.IGNITE_TIME.get());
    }

    default boolean tickBurnout(ItemStack stack, Level level, Entity entity) {
        Long igniteTime = getIgniteTime(stack);
        if (igniteTime == null) return false;

        long elapsed = level.getGameTime() - igniteTime;
        if (elapsed < getIgniteDuration()) return false;

        stack.shrink(1);
        entity.spawnAtLocation(getQuenchResult());

        level.playSound(
                null,
                entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.GENERIC_EXTINGUISH_FIRE,
                entity.getSoundSource(),
                0.5f,
                1.8f + level.getRandom().nextFloat() * (3.4f - 1.8f)
        );

        return true;
    }

    default void quench(ItemStack stack, Player player) {
        if (destroysOnQuench()) {
            stack.shrink(1);
            player.addItem(new ItemStack(getQuenchResult()));
        } else {
            stack.remove(SCDataComponents.IGNITE_TIME.get());
        }
        setFinished(stack);
    }

    default void quenchDropped(ItemStack stack, Entity entity) {
        if (destroysOnQuench()) {
            stack.shrink(1);
            entity.spawnAtLocation(getQuenchResult());
        } else {
            stack.remove(SCDataComponents.IGNITE_TIME.get());
        }
        setFinished(stack);
    }

    default void playQuenchEffects(Level level, BlockPos pos, int particleCount) {
        if (level.isClientSide()) return;

        level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE,
                SoundSource.PLAYERS, 0.5f, 1.8f + level.random.nextFloat() * (3.4f - 1.8f));

        if (level instanceof ServerLevel serverLevel) {
            RandomSource random = level.random;
            for (int i = 0; i < particleCount; i++) {
                double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
                double y = pos.getY() + 0.8 + random.nextDouble() * 0.4;
                double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
                serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 0, 0.05, 0, 0.01);
            }
        }
    }

    default void playQuenchSoundClient(Player player) {
        float pitch = 1.8f + player.getRandom().nextFloat() * (3.4f - 1.8f);
        player.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.5f, pitch);
    }
}