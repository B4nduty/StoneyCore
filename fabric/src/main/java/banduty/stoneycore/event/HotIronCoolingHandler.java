package banduty.stoneycore.event;

import banduty.stoneycore.items.custom.hotiron.QuenchItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

public class HotIronCoolingHandler {

    private static final Set<ItemEntity> HOT_IRON =
            Collections.newSetFromMap(new WeakHashMap<>());

    public static void init() {

        // Track only items that are actually ignited.
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof ItemEntity item)) {
                return;
            }

            ItemStack stack = item.getItem();

            if (stack.getItem() instanceof QuenchItem quenchItem
                    && quenchItem.isIgnited(stack)) {

                HOT_IRON.add(item);
            }
        });

        // Tick cooling logic.
        ServerTickEvents.END_WORLD_TICK.register(
                HotIronCoolingHandler::tick
        );
    }

    private static void tick(ServerLevel world) {

        Iterator<ItemEntity> it = HOT_IRON.iterator();

        while (it.hasNext()) {

            ItemEntity item = it.next();

            // Cleanup invalid references.
            if (item == null || !item.isAlive()) {
                it.remove();
                continue;
            }

            ItemStack stack = item.getItem();

            if (!(stack.getItem() instanceof QuenchItem quenchItem)) {
                it.remove();
                continue;
            }

            // Already cooled/finished.
            if (!quenchItem.isIgnited(stack)) {
                it.remove();
                continue;
            }

            BlockPos pos = item.getOnPos();
            BlockState state = world.getBlockState(pos);

            boolean cooled = false;

            // Water source / waterlogged block.
            if (state.getFluidState().isSource()
                    && state.getFluidState().is(Fluids.WATER)) {

                cooled = true;
            }

            // Water cauldron.
            else if (state.is(Blocks.WATER_CAULDRON)
                    && state.getValue(LayeredCauldronBlock.LEVEL) > 0) {

                cooled = true;

                LayeredCauldronBlock.lowerFillLevel(
                        state,
                        world,
                        pos
                );
            }

            if (cooled) {
                if (cool(world, item, stack)) {
                    it.remove();
                }
            }
        }
    }

    private static boolean cool(
            ServerLevel level,
            ItemEntity item,
            ItemStack stack
    ) {
        QuenchItem quenchItem =
                (QuenchItem) stack.getItem();

        // Safety check: don't quench an already cooled item.
        if (!quenchItem.isIgnited(stack)) {
            return false;
        }

        boolean quenched =
                quenchItem.quenchDropped(stack, item);

        if (!quenched) {
            return false;
        }

        /*
         * Explicitly update the ItemEntity's stack.
         *
         * This is important for the client to receive the
         * updated component state and refresh the item model.
         */
        item.setItem(stack);

        level.playSound(
                null,
                item.getX(),
                item.getY(),
                item.getZ(),
                SoundEvents.GENERIC_EXTINGUISH_FIRE,
                SoundSource.BLOCKS,
                0.6f,
                1.6f + level.random.nextFloat() * 0.8f
        );

        RandomSource random = level.random;

        for (int i = 0; i < 20; i++) {
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    item.getX(),
                    item.getY() + 0.2,
                    item.getZ(),
                    1,
                    (random.nextDouble() - 0.5) * 0.3,
                    0.07,
                    (random.nextDouble() - 0.5) * 0.3,
                    0.02
            );
        }

        return true;
    }
}