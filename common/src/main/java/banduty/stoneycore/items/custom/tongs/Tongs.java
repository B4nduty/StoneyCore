package banduty.stoneycore.items.custom.tongs;

import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.custom.CraftmanAnvilHelper;
import banduty.stoneycore.items.custom.hotiron.HotIron;
import banduty.stoneycore.util.data.itemdata.ItemStackHolder;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Tongs extends Item implements CraftmanAnvilHelper {
    private static final String STACK_KEY = "TargetStack";

    public Tongs(Properties properties) {
        super(properties);
    }

    public static boolean hasTargetStack(ItemStack stack) {
        return !getTargetStack(stack).isEmpty();
    }

    public static ItemStack getTongsFor(ItemStack targetStack) {
        return Tongs.createForStack(targetStack);
    }

    public static ItemStack createForStack(ItemStack targetStack) {
        ItemStack manuscript = new ItemStack(SCItems.TONGS.get());
        setTargetStack(manuscript, targetStack);
        return manuscript;
    }

    public static void removeTargetStack(ItemStack stack) {
        stack.remove(SCDataComponents.TARGET_STACK.get());
    }

    public static void setTargetStack(ItemStack stack, ItemStack targetStack) {
        stack.set(SCDataComponents.TARGET_STACK.get(), new ItemStackHolder(targetStack));
    }

    public static ItemStack getTargetStack(ItemStack stack) {
        if (stack.has(SCDataComponents.TARGET_STACK.get())) {
            return stack.get(SCDataComponents.TARGET_STACK.get()).stack();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Component getName(ItemStack stack) {
        ItemStack target = getTargetStack(stack);
        if (!target.isEmpty())
            return Component.translatable("item.stoneycore.tongs_with_item", target.getHoverName());
        return super.getName(stack);
    }

    @Override
    public ItemStack acceptCraftmanAnvilItem(ItemStack itemStack) {
        if (hasTargetStack(itemStack) && getTargetStack(itemStack).getItem() instanceof HotIron) {
            ItemStack targetStack = getTargetStack(itemStack);
            ItemStack finalItemStack = targetStack.copy();
            removeTargetStack(itemStack);
            return finalItemStack;
        }
        return itemStack;
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

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide()) return InteractionResultHolder.pass(stack);

        ItemStack target = getTargetStack(stack);

        // Check for water in the player's line of sight
        BlockPos waterPos = getLookedWater(player, level);

        if (waterPos != null && !target.isEmpty()) {
            // Check if the target is HotIron
            if (target.getItem() instanceof HotIron) {
                // Get the inner target of the hot iron (what it was made from)
                ItemStack hotIronTarget = HotIron.getTargetStack(target);

                // The cooled item is either the hot iron's target or an iron ingot
                ItemStack cooledItem;
                if (!hotIronTarget.isEmpty()) {
                    cooledItem = hotIronTarget.copy();
                } else {
                    cooledItem = new ItemStack(Items.IRON_INGOT);
                }

                // Remove the hot iron target from tongs (keep the tongs!)
                removeTargetStack(stack);
                // Give the cooled item to the player
                player.addItem(cooledItem);
            } else {
                // For non-hot-iron items, just drop them as-is
                ItemStack drop = target.copy();
                removeTargetStack(stack);
                player.addItem(drop);
            }

            level.playSound(null, waterPos, SoundEvents.GENERIC_EXTINGUISH_FIRE,
                    SoundSource.PLAYERS, 0.5f,
                    1.8f + level.random.nextFloat() * (3.4f - 1.8f));

            if (level instanceof ServerLevel serverLevel) {
                RandomSource random = level.random;
                for (int i = 0; i < 8; i++) {
                    double x = waterPos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
                    double y = waterPos.getY() + 0.8 + random.nextDouble() * 0.4;
                    double z = waterPos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
                    serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 0, 0.05, 0, 0.01);
                }
            }

            return InteractionResultHolder.success(stack);
        }

        // Check for cauldron
        BlockHitResult hit = (BlockHitResult) player.pick(5.0D, 0.0F, false);

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            BlockState state = level.getBlockState(pos);

            boolean isCauldron = state.is(Blocks.WATER_CAULDRON)
                    && state.hasProperty(LayeredCauldronBlock.LEVEL);

            if (isCauldron && !target.isEmpty()) {
                int cauldronLevel = state.getValue(LayeredCauldronBlock.LEVEL);
                if (cauldronLevel >= 1) {
                    // Check if the target is HotIron
                    if (target.getItem() instanceof HotIron) {
                        // Get the inner target of the hot iron
                        ItemStack hotIronTarget = HotIron.getTargetStack(target);

                        // The cooled item is either the hot iron's target or an iron ingot
                        ItemStack cooledItem;
                        if (!hotIronTarget.isEmpty()) {
                            cooledItem = hotIronTarget.copy();
                        } else {
                            cooledItem = new ItemStack(Items.IRON_INGOT);
                        }

                        // Remove the hot iron target from tongs (keep the tongs!)
                        removeTargetStack(stack);
                        // Give the cooled item to the player
                        player.addItem(cooledItem);
                    } else {
                        // For non-hot-iron items, just drop them as-is
                        ItemStack drop = target.copy();
                        removeTargetStack(stack);
                        player.addItem(drop);
                    }

                    // Lower the cauldron water level
                    LayeredCauldronBlock.lowerFillLevel(state, level, pos);

                    level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE,
                            SoundSource.PLAYERS, 0.5f,
                            1.8f + level.random.nextFloat() * (3.4f - 1.8f));

                    if (level instanceof ServerLevel serverLevel) {
                        RandomSource random = level.random;
                        for (int i = 0; i < 8; i++) {
                            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
                            double y = pos.getY() + 0.8 + random.nextDouble() * 0.4;
                            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
                            serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 0, 0.05, 0, 0.01);
                        }
                    }

                    return InteractionResultHolder.success(stack);
                }
            }
        }

        // Rest of the method remains the same...
        InteractionHand secondHand = (hand == InteractionHand.MAIN_HAND)
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;

        ItemStack secondStack = player.getItemInHand(secondHand);

        if (!target.isEmpty()) {
            player.addItem(target.copy());
            removeTargetStack(stack);
            return InteractionResultHolder.success(stack);
        }

        if (secondStack.getItem() instanceof HotIron) {
            setTargetStack(stack, secondStack.copyWithCount(1));
            secondStack.shrink(1);
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(itemStack, level, entity, slot, selected);

        if (level.isClientSide()) return;

        ItemStack target = getTargetStack(itemStack);

        if (!target.isEmpty()) {

            target.getItem().inventoryTick(target, level, entity, slot, selected);

            if (target.isEmpty()) {
                removeTargetStack(itemStack);
            } else {
                setTargetStack(itemStack, target);
            }
        }
    }

    public static boolean isHolding(ItemStack tongsStack) {
        return hasTargetStack(tongsStack);
    }
}
