package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.hotiron.QuenchItem;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

@EventBusSubscriber(
        modid = StoneyCore.MOD_ID,
        bus = EventBusSubscriber.Bus.GAME
)
public class HotIronCoolingEvents {

    private static final Set<ItemEntity> HOT_IRON_ITEMS =
            Collections.newSetFromMap(new WeakHashMap<>());

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ItemEntity item)) {
            return;
        }

        ItemStack stack = item.getItem();

        if (stack.getItem() instanceof QuenchItem quenchItem
                && quenchItem.isIgnited(stack)) {

            HOT_IRON_ITEMS.add(item);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Iterator<ItemEntity> it = HOT_IRON_ITEMS.iterator();

        while (it.hasNext()) {
            ItemEntity item = it.next();

            if (item == null || !item.isAlive()) {
                it.remove();
                continue;
            }

            ItemStack stack = item.getItem();

            if (!(stack.getItem() instanceof QuenchItem quenchItem)) {
                it.remove();
                continue;
            }

            // No longer ignited → no longer needs tracking.
            if (!quenchItem.isIgnited(stack)) {
                it.remove();
                continue;
            }

            BlockPos pos = item.blockPosition();
            BlockState state = level.getBlockState(pos);

            boolean cooled = false;

            // Water source.
            if (state.getFluidState().isSource()
                    && state.getFluidState().is(Fluids.WATER)) {

                cooled = true;
            }

            // Water cauldron.
            if (state.is(Blocks.WATER_CAULDRON)
                    && state.getValue(LayeredCauldronBlock.LEVEL) > 0) {

                cooled = true;

                LayeredCauldronBlock.lowerFillLevel(
                        state,
                        level,
                        pos
                );
            }

            if (cooled) {
                cool(level, item, stack);
                it.remove();
            }
        }
    }

    private static void cool(
            ServerLevel level,
            ItemEntity item,
            ItemStack stack
    ) {
        QuenchItem quenchItem = (QuenchItem) stack.getItem();

        // Safety check: don't quench an already cooled item.
        if (!quenchItem.isIgnited(stack)) {
            return;
        }

        quenchItem.quenchDropped(stack, item);

        /*
         * Make sure the ItemEntity knows that its ItemStack
         * has changed so the new component state is synchronized
         * to the client.
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
    }
}