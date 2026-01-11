package banduty.stoneycore.items.hotiron;

import banduty.stoneycore.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

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
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(target.getItem());
            return Component.translatable("item." + id.getNamespace() + ".hot_iron_finished_" + id.getPath());
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
        super.useOn(context);

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        if (state.is(Blocks.WATER_CAULDRON) && state.hasProperty(LayeredCauldronBlock.LEVEL)) {
            int cauldronLevel = state.getValue(LayeredCauldronBlock.LEVEL);
            if (cauldronLevel >= 1) {
                if (!level.isClientSide()) {
                    LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                    ItemStack target = getTargetStack(stack);
                    if (!target.isEmpty()) player.addItem(target.copy());
                    stack.shrink(1);
                } else {
                    float pitch = 1.8f + player.getRandom().nextFloat() * (3.4f - 1.8f);
                    player.playSound(SoundEvents.GENERIC_EXTINGUISH_FIRE, 0.5f, pitch);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
