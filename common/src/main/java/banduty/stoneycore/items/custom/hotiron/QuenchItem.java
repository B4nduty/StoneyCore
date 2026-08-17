package banduty.stoneycore.items.custom.hotiron;

import banduty.stoneycore.items.custom.tongs.Tongs;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public interface QuenchItem {

    int getIgniteDuration();

    boolean destroysOnQuench();

    default Item getQuenchResult() {
        return Items.IRON_INGOT;
    }

    /**
     * An item is finished when it is no longer ignited.
     * No IGNITED component = finished.
     */
    default boolean isFinished(ItemStack stack) {
        return !isIgnited(stack);
    }

    /**
     * Removes all temporary ignition data.
     * After this, the item is considered finished/cooled.
     */
    default void setFinished(ItemStack stack) {
        stack.remove(SCDataComponents.IGNITED.get());
        stack.remove(SCDataComponents.IGNITE_TIME.get());
    }

    default Long getIgniteTime(ItemStack stack) {
        return stack.get(SCDataComponents.IGNITE_TIME.get());
    }

    /**
     * IGNITED component exists and is true = currently hot/ignited.
     */
    default boolean isIgnited(ItemStack stack) {
        return stack.getOrDefault(
                SCDataComponents.IGNITED.get(),
                false
        );
    }

    /**
     * Starts the ignition process.
     */
    default void igniteItem(ItemStack stack, Entity entity) {
        stack.set(
                SCDataComponents.IGNITED.get(),
                true
        );

        stack.set(
                SCDataComponents.IGNITE_TIME.get(),
                entity.level().getGameTime()
        );
    }

    /**
     * Makes the item permanently ignited by removing
     * the burnout timer while keeping IGNITED = true.
     */
    default void unlimitedItem(ItemStack stack) {
        stack.remove(SCDataComponents.IGNITE_TIME.get());
        stack.set(SCDataComponents.IGNITED.get(), true);
    }

    /**
     * Handles the item reaching its maximum ignition time.
     */
    default boolean tickBurnout(
            ItemStack stack,
            Level level,
            Entity entity
    ) {
        // Already cooled/finished.
        if (!isIgnited(stack)) {
            return false;
        }

        Long igniteTime = getIgniteTime(stack);

        if (igniteTime == null) {
            return false;
        }

        long elapsed = level.getGameTime() - igniteTime;

        if (elapsed < getIgniteDuration()) {
            return false;
        }

        int count = stack.getCount();

        /**/

        if (finishInsideTongs(stack, entity, count)) {
            playBurnoutEffects(level, entity);
            return true;
        }

        stack.setCount(0);

        // Return the entire stack as the quench result.
        ItemStack result = new ItemStack(
                getQuenchResult(),
                count
        );

        entity.spawnAtLocation(result);

        playBurnoutEffects(level, entity);

        return true;
    }

    default void playBurnoutEffects(
            Level level,
            Entity entity
    ) {
        level.playSound(
                null,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                SoundEvents.GENERIC_EXTINGUISH_FIRE,
                entity.getSoundSource(),
                0.5f,
                1.8f + level.getRandom().nextFloat() * (3.4f - 1.8f)
        );
    }

    default boolean finishInsideTongs(
            ItemStack capturedStack,
            Entity entity,
            int count
    ) {
        /*
         * Tongs are inside the player's inventory.
         */
        if (entity instanceof Player player) {

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {

                ItemStack tongsStack = player.getInventory().getItem(i);

                if (!(tongsStack.getItem() instanceof Tongs tongs)) {
                    continue;
                }

                ItemStack tongCaptured = tongs.getCapturedItem(tongsStack);

                /*
                 * This is the exact ItemStack being ticked by
                 * Tongs.inventoryTick().
                 */
                if (tongCaptured != capturedStack) {
                    continue;
                }

                /*
                 * Remove the hot item from the Tongs first.
                 */
                tongs.removeCapturedItem(tongsStack);

                if (destroysOnQuench()) {

                    ItemStack result = new ItemStack(
                            getQuenchResult(),
                            count
                    );

                    /*
                     * Return the cooled result to the player's inventory.
                     */
                    if (!player.addItem(result)) {
                        player.drop(result, false);
                    }

                } else {

                    /*
                     * Remove ignition data.
                     */
                    setFinished(capturedStack);

                    /*
                     * Return the cooled item to the player's inventory.
                     */
                    ItemStack result = capturedStack.copy();

                    if (!player.addItem(result)) {
                        player.drop(result, false);
                    }
                }

                return true;
            }

            return false;
        }

        /*
         * Tongs are lying on the ground inside an ItemEntity.
         */
        if (entity instanceof ItemEntity tongEntity) {

            ItemStack tongsStack = tongEntity.getItem();

            if (!(tongsStack.getItem() instanceof Tongs tongs)) {
                return false;
            }

            ItemStack tongCaptured = tongs.getCapturedItem(tongsStack);

            /*
             * Make sure this is the captured ItemStack that
             * actually triggered the burnout.
             */
            if (tongCaptured != capturedStack) {
                return false;
            }

            /*
             * Remove the hot item from the Tongs.
             */
            tongs.removeCapturedItem(tongsStack);

            /*
             * Put the cooled/result item at the exact same
             * location as the Tongs.
             */
            if (destroysOnQuench()) {

                ItemStack result = new ItemStack(
                        getQuenchResult(),
                        count
                );

                tongEntity.spawnAtLocation(result);

            } else {

                /*
                 * Remove ignition data.
                 */
                setFinished(capturedStack);

                /*
                 * Spawn the cooled item next to the Tongs.
                 */
                tongEntity.spawnAtLocation(
                        capturedStack.copy()
                );
            }

            /*
             * If the Tongs became empty, it remains in the world
             * as a normal empty pair of Tongs.
             */
            return true;
        }

        return false;
    }

    /**
     * Quenches the item.
     * <p>
     * Returns false if the item is already cooled.
     */
    default boolean quench(
            ItemStack stack,
            Player player
    ) {
        // Cannot quench an already cooled item.
        if (!isIgnited(stack)) {
            return false;
        }
        int count = stack.getCount();
        if (destroysOnQuench()) {
            stack.setCount(0);
            player.addItem(new ItemStack(getQuenchResult(),
                    count));
        } else {
            setFinished(stack);
        }

        return true;
    }

    /**
     * Quenches a dropped item.
     * <p>
     * Returns false if the item is already cooled.
     */
    default boolean quenchDropped(
            ItemStack stack,
            Entity entity
    ) {
        // Cannot quench an already cooled item.
        if (!isIgnited(stack)) {
            return false;
        }

        int count = stack.getCount();

        if (destroysOnQuench()) {
            // Remove the entire hot stack.
            stack.setCount(0);

            // Return the entire stack as the quench result.
            ItemStack result = new ItemStack(
                    getQuenchResult(),
                    count
            );

            entity.spawnAtLocation(result);
        } else {
            // The entire stack becomes cooled.
            setFinished(stack);
        }

        return true;
    }

    default void playQuenchEffects(
            Level level,
            BlockPos pos,
            int particleCount
    ) {
        if (level.isClientSide()) {
            return;
        }

        level.playSound(
                null,
                pos,
                SoundEvents.GENERIC_EXTINGUISH_FIRE,
                SoundSource.PLAYERS,
                0.5f,
                1.8f + level.random.nextFloat() * (3.4f - 1.8f)
        );

        if (level instanceof ServerLevel serverLevel) {
            RandomSource random = level.random;

            for (int i = 0; i < particleCount; i++) {
                double x = pos.getX()
                        + 0.5
                        + (random.nextDouble() - 0.5) * 0.8;

                double y = pos.getY()
                        + 0.8
                        + random.nextDouble() * 0.4;

                double z = pos.getZ()
                        + 0.5
                        + (random.nextDouble() - 0.5) * 0.8;

                serverLevel.sendParticles(
                        ParticleTypes.CLOUD,
                        x,
                        y,
                        z,
                        1,
                        0,
                        0.05,
                        0,
                        0.01
                );
            }
        }
    }

    default void playQuenchSoundClient(Player player) {
        float pitch = 1.8f
                + player.getRandom().nextFloat() * (3.4f - 1.8f);

        player.playSound(
                SoundEvents.GENERIC_EXTINGUISH_FIRE,
                0.5f,
                pitch
        );
    }

    default BlockPos getLookedWater(
            Player player,
            Level level
    ) {
        double reach = 5.0;

        Vec3 start = player.getEyePosition();

        Vec3 end = start.add(
                player.getLookAngle().scale(reach)
        );

        BlockHitResult hit = level.clip(
                new ClipContext(
                        start,
                        end,
                        ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.SOURCE_ONLY,
                        player
                )
        );

        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        BlockPos pos = hit.getBlockPos();

        BlockState state = level.getBlockState(pos);

        if (state.getFluidState().isSource()
                && state.getFluidState().is(Fluids.WATER)) {
            return pos;
        }

        return null;
    }

    /**
     * Handles quenching when interacting with a water source block.
     */
    default InteractionResult handleWaterInteraction(
            Level level,
            BlockPos pos,
            Player player,
            ItemStack stack,
            InteractionHand hand
    ) {
        if (!(stack.getItem() instanceof QuenchItem quenchItem)) {
            return InteractionResult.PASS;
        }

        // Already cooled, so water does nothing.
        if (!quenchItem.isIgnited(stack)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            if (quenchItem.quench(stack, player)) {
                quenchItem.playQuenchEffects(
                        level,
                        pos,
                        8
                );
            }
        } else {
            quenchItem.playQuenchSoundClient(player);
        }

        return InteractionResult.SUCCESS;
    }

    /**
     * Handles quenching in a water cauldron.
     */
    default InteractionResult handleWaterCauldron(
            Level level,
            BlockPos pos,
            Player player,
            ItemStack stack
    ) {
        if (!(stack.getItem() instanceof QuenchItem quenchItem)) {
            return InteractionResult.PASS;
        }

        // Already cooled, so do not consume cauldron water.
        if (!quenchItem.isIgnited(stack)) {
            return InteractionResult.PASS;
        }

        int cauldronLevel = level
                .getBlockState(pos)
                .getValue(LayeredCauldronBlock.LEVEL);

        if (cauldronLevel < 1) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            if (quenchItem.quench(stack, player)) {

                LayeredCauldronBlock.lowerFillLevel(
                        level.getBlockState(pos),
                        level,
                        pos
                );

                quenchItem.playQuenchEffects(
                        level,
                        pos,
                        10
                );
            }
        } else {
            quenchItem.playQuenchSoundClient(player);
        }

        return InteractionResult.SUCCESS;
    }
}