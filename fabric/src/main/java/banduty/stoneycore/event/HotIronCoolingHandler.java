package banduty.stoneycore.event;

import banduty.stoneycore.items.hotiron.HotIron;
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
import net.minecraft.world.item.Items;
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

        // track when item enters world
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof ItemEntity item) {
                if (item.getItem().getItem() instanceof HotIron) {
                    HOT_IRON.add(item);
                }
            }
        });

        // tick cooling logic
        ServerTickEvents.END_WORLD_TICK.register(world -> tick(world));
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

            if (!(stack.getItem() instanceof HotIron)) {
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

        int count = stack.getCount();

        ItemStack target = HotIron.getTargetStack(stack);

        ItemStack result;

        if (!target.isEmpty()) {
            result = target.copy();
        } else {
            result = new ItemStack(Items.IRON_INGOT);
        }

        result.setCount(count);

        // remove the hot iron entity completely
        item.discard();

        // spawn cooled stack
        ItemEntity newEntity = new ItemEntity(
                level,
                item.getX(),
                item.getY(),
                item.getZ(),
                result
        );

        level.addFreshEntity(newEntity);

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