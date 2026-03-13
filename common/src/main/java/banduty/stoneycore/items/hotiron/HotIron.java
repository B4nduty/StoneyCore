package banduty.stoneycore.items.hotiron;

import banduty.stoneycore.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HotIron extends Item {
    private static final int IGNITE_DURATION_TICKS = 20 * 30;
    private static final int IGNITE_DURATION_TICKS_AFTER_FINISH = 20 * 30;
    private static final String STACK_KEY = "TargetStack";
    private static final String HELD_BY_TONGS_KEY = "HeldByTongs";

    public HotIron(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (level.isClientSide()) return;

        ItemStack target = getTargetStack(stack);
        if (!target.isEmpty()) {
            target.getItem().inventoryTick(target, level, entity, slot, selected);
            if (target.isEmpty()) {
                removeTargetStack(stack);
            }
        }

        if (entity instanceof Player player && player.isCreative()) {
            unlimitedHotIron(stack);
            return;
        }

        if (!(stack.getTag() != null && stack.getTag().contains("igniteTime")))
            igniteHotIron(stack, entity);

        if (!isHeldByTongs(stack)) {
            entity.setSharedFlagOnFire(true);
            entity.setRemainingFireTicks(20);
        }

        long igniteTime = stack.getTag().getLong("igniteTime");
        long currentTime = level.getGameTime();
        if (isFinished(stack)) currentTime -= IGNITE_DURATION_TICKS_AFTER_FINISH;

        if (currentTime - igniteTime >= IGNITE_DURATION_TICKS) {
            stack.shrink(1);
            if (!target.isEmpty()) {
                entity.spawnAtLocation(target.copy());
            } else {
                entity.spawnAtLocation(Items.IRON_INGOT);
            }

            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.GENERIC_EXTINGUISH_FIRE, entity.getSoundSource(), 0.5f,
                    1.8f + level.getRandom().nextFloat() * (3.4f - 1.8f)
            );
        }
    }

    public static void setHeldByTongs(ItemStack stack, boolean held) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putBoolean(HELD_BY_TONGS_KEY, held);
    }

    public static boolean isHeldByTongs(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(HELD_BY_TONGS_KEY);
    }

    public static void removeTargetStack(ItemStack stack) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.remove(STACK_KEY);
        if (nbt.isEmpty()) {
            stack.setTag(null);
        }
    }

    public static void igniteHotIron(ItemStack stack, Entity entity) {
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putLong("igniteTime", entity.level().getGameTime());
    }

    public static void unlimitedHotIron(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null) nbt.remove("igniteTime");
        if (nbt != null && nbt.isEmpty()) {
            stack.setTag(null);
        }
    }

    public static boolean hasTargetStack(ItemStack stack) {
        return !(getTargetStack(stack).isEmpty());
    }

    public static ItemStack getHotIronFor(ItemStack targetStack) {
        return HotIron.createForStack(targetStack);
    }

    public static ItemStack createForStack(ItemStack targetStack) {
        ItemStack stack = Services.HOT_IRON.getHotIron(targetStack);
        setTargetStack(stack, targetStack);
        return stack;
    }

    public static void setTargetStack(ItemStack stack, ItemStack targetStack) {
        if (targetStack == null || targetStack.isEmpty()) return;
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag stackTag = new CompoundTag();
        targetStack.save(stackTag);
        tag.put(STACK_KEY, stackTag);
    }

    public static ItemStack getTargetStack(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains(STACK_KEY)) {
            CompoundTag stackTag = stack.getTag().getCompound(STACK_KEY);
            return ItemStack.of(stackTag);
        }
        return ItemStack.EMPTY;
    }

    public boolean isFinished(ItemStack stack) {
        return hasTargetStack(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemStack target = getTargetStack(stack);
        if (!target.isEmpty()) {
            // Get the original item's display name
            Component originalName = target.getHoverName();
            // Return a translatable component with the original name as an argument
            return Component.translatable("item.stoneycore.hot_iron.with_item", originalName);
        }
        return super.getName(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag tooltipFlag) {
        if (level != null && stack.getTag() != null && stack.getTag().contains("igniteTime")) {
            long igniteTime = stack.getTag().getLong("igniteTime");
            long currentTime = level.getGameTime();
            if (isFinished(stack)) currentTime -= IGNITE_DURATION_TICKS_AFTER_FINISH;
            long remainingTicks = IGNITE_DURATION_TICKS - (currentTime - igniteTime);
            if (remainingTicks < 0) remainingTicks = 0;

            long seconds = remainingTicks / 20;
            long hours = seconds / 3600;
            seconds %= 3600;
            long minutes = seconds / 60;
            seconds %= 60;

            tooltip.add(Component.literal(String.format("Ignited - Time left: %02d:%02d:%02d", hours, minutes, seconds)));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        // Check for water sources
        BlockPos waterPos = getLookedWater(player, level);

        if (waterPos != null) {
            return handleWaterInteraction(level, waterPos, player, stack, context.getHand());
        }

        // Check for water cauldron
        if (state.is(Blocks.WATER_CAULDRON) && state.hasProperty(LayeredCauldronBlock.LEVEL)) {
            return handleWaterCauldron(level, pos, player, stack);
        }

        return InteractionResult.PASS;
    }

    private BlockPos getLookedWater(Player player, Level level) {

        double reach = 5.0; // same as bucket reach

        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(reach));

        BlockHitResult hit = level.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.SOURCE_ONLY,
                player
        ));

        if (hit.getType() != HitResult.Type.BLOCK) return null;

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (state.getFluidState().isSource() &&
                state.getFluidState().is(Fluids.WATER)) {
            return pos;
        }

        return null;
    }

    private InteractionResult handleWaterInteraction(Level level, BlockPos pos, Player player, ItemStack stack, InteractionHand hand) {
        if (!level.isClientSide()) {
            // Get the target before extinguishing
            ItemStack target = getTargetStack(stack);

            // Extinguish the hot iron
            stack.shrink(1);

            // Drop the target or an iron ingot
            if (!target.isEmpty()) {
                player.addItem(target.copy());
            } else {
                player.addItem(new ItemStack(Items.IRON_INGOT));
            }

            // Play extinguish sound
            level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE,
                    SoundSource.PLAYERS, 0.5f, 1.8f + level.random.nextFloat() * (3.4f - 1.8f));

            // Spawn steam particles
            if (level instanceof ServerLevel serverLevel) {
                RandomSource random = level.random;
                for (int i = 0; i < 8; i++) {
                    double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
                    double y = pos.getY() + 0.8 + random.nextDouble() * 0.4;
                    double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
                    serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 0, 0.05, 0, 0.01);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleWaterCauldron(Level level, BlockPos pos, Player player, ItemStack stack) {
        int cauldronLevel = level.getBlockState(pos).getValue(LayeredCauldronBlock.LEVEL);
        if (cauldronLevel >= 1) {
            if (!level.isClientSide()) {
                // Reduce cauldron water level
                LayeredCauldronBlock.lowerFillLevel(level.getBlockState(pos), level, pos);

                // Get the target before extinguishing
                ItemStack target = getTargetStack(stack);

                // Extinguish the hot iron
                stack.shrink(1);

                // Drop the target or an iron ingot
                if (!target.isEmpty()) {
                    player.addItem(target.copy());
                } else {
                    player.addItem(new ItemStack(Items.IRON_INGOT));
                }

                // Spawn steam particles
                if (level instanceof ServerLevel serverLevel) {
                    RandomSource random = level.random;
                    for (int i = 0; i < 10; i++) {
                        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
                        double y = pos.getY() + 0.8 + random.nextDouble() * 0.4;
                        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
                        serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 0, 0.05, 0, 0.01);
                    }
                }
            } else {
                float pitch = 1.8f + player.getRandom().nextFloat() * (3.4f - 1.8f);
                player.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.5f, pitch);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
