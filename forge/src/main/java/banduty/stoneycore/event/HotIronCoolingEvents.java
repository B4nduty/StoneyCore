package banduty.stoneycore.event;

import banduty.stoneycore.items.hotiron.HotIron;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class HotIronCoolingEvents {

    // industrial-grade tracking structure
    private static final Set<ItemEntity> HOT_IRON_ITEMS =
            java.util.Collections.newSetFromMap(new WeakHashMap<>());

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {

        if (!(event.getEntity() instanceof ItemEntity item)) return;

        if (item.getItem().getItem() instanceof HotIron) {
            HOT_IRON_ITEMS.add(item);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {

        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;

        Iterator<ItemEntity> it = HOT_IRON_ITEMS.iterator();

        while (it.hasNext()) {

            ItemEntity item = it.next();

            // cleanup invalid references
            if (item == null || !item.isAlive()) {
                it.remove();
                continue;
            }

            ItemStack stack = item.getItem();

            // stack changed → stop tracking
            if (!(stack.getItem() instanceof HotIron)) {
                it.remove();
                continue;
            }

            BlockPos pos = item.blockPosition();
            BlockState state = level.getBlockState(pos);

            boolean cooled = false;

            // water source / waterlogged
            if (state.getFluidState().isSource()
                    && state.getFluidState().is(Fluids.WATER)) {
                cooled = true;
            }

            // water cauldron
            if (state.is(Blocks.WATER_CAULDRON)
                    && state.getValue(LayeredCauldronBlock.LEVEL) > 0) {

                cooled = true;
                LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            }

            if (cooled) {
                cool(level, item, stack);
                it.remove(); // no longer hot iron after cooling
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