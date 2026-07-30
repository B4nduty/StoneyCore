package banduty.stoneycore.event;

import banduty.stoneycore.items.custom.hotiron.HotIron;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
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

        // track any item that enters the world already ignited
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ItemEntity item && isIgnited(item.getItem())) {
                HOT_IRON.add(item);
            }
        });

        // tick cooling logic
        ServerTickEvents.END_WORLD_TICK.register(HotIronCoolingHandler::tick);
    }

    private static boolean isIgnited(ItemStack stack) {
        return !stack.isEmpty() && stack.get(SCDataComponents.IGNITE_TIME.get()) != null;
    }

    private static void tick(ServerLevel world) {

        Iterator<ItemEntity> it = HOT_IRON.iterator();

        while (it.hasNext()) {

            ItemEntity item = it.next();

            if (item == null || !item.isAlive()) {
                it.remove();
                continue;
            }

            ItemStack stack = item.getItem();

            if (!isIgnited(stack)) {
                it.remove();
                continue;
            }

            BlockPos pos = item.getOnPos();
            BlockState state = world.getBlockState(pos);

            boolean cooled = false;

            // water source / waterlogged
            if (state.getFluidState().isSource()
                    && state.getFluidState().is(Fluids.WATER)) {
                cooled = true;
            }

            // water cauldron
            else if (state.is(Blocks.WATER_CAULDRON)
                    && state.getValue(LayeredCauldronBlock.LEVEL) > 0) {

                cooled = true;
                LayeredCauldronBlock.lowerFillLevel(state, world, pos);
            }

            if (cooled) {
                cool(world, item, stack);
                it.remove();
            }
        }
    }

    private static void cool(ServerLevel level, ItemEntity item, ItemStack stack) {
        ((HotIron) stack.getItem()).quenchDropped(stack, item);

        level.playSound(
                null,
                item.getX(), item.getY(), item.getZ(),
                SoundEvents.GENERIC_EXTINGUISH_FIRE,
                SoundSource.BLOCKS,
                0.6f,
                1.6f + level.random.nextFloat() * 0.8f
        );

        RandomSource r = level.random;

        for (int i = 0; i < 20; i++) {
            level.sendParticles(
                    ParticleTypes.CLOUD,
                    item.getX(),
                    item.getY() + 0.2,
                    item.getZ(),
                    1,
                    (r.nextDouble() - 0.5) * 0.3,
                    0.07,
                    (r.nextDouble() - 0.5) * 0.3,
                    0.02
            );
        }
    }
}