package banduty.stoneycore.block;

import banduty.stoneycore.items.CraftmanAnvilHelper;
import banduty.stoneycore.items.SmithingHammer;
import banduty.stoneycore.items.hotiron.HotIron;
import banduty.stoneycore.items.tongs.Tongs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

public class CraftmanAnvilBlock extends BaseEntityBlock implements Fallable {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 10, 14);

    public CraftmanAnvilBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        // This will be implemented differently per loader
        return null; // To be overridden by loader-specific implementations
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.getStateDefinition()
                .any().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.PASS;

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof CraftmanAnvilBlockEntity anvilEntity)) {
            return InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof SmithingHammer && anvilEntity.getRecipe().isPresent()) {
            anvilEntity.hitAnvil(serverPlayer);
            if (player.isCreative()) return InteractionResult.SUCCESS;
            stack.hurtAndBreak(1, serverPlayer, p -> p.broadcastBreakEvent(serverPlayer.getUsedItemHand()));
            if (stack.getDamageValue() >= stack.getMaxDamage()) stack.shrink(1);
            return InteractionResult.SUCCESS;
        }

        if (hand == InteractionHand.MAIN_HAND) {
            if (Tongs.getTargetStack(stack).isEmpty() && (stack.isEmpty() || player.getMainHandItem().getItem() instanceof Tongs || player.getOffhandItem().getItem() instanceof Tongs)) {
                NonNullList<ItemStack> itemStacks = anvilEntity.getItems();
                for (ItemStack itemStack : itemStacks) {
                    Optional<ItemStack> tongs = getTongsFromInventory(player);
                    if (tongs.isEmpty() && player.getMainHandItem().getItem() instanceof Tongs) tongs = Optional.of(player.getMainHandItem());
                    if (tongs.isEmpty() && player.getOffhandItem().getItem() instanceof Tongs) tongs = Optional.of(player.getOffhandItem());
                    if (tongs.isEmpty()) continue;
                    if (!Tongs.getTargetStack(tongs.get()).isEmpty()) continue;
                    if (itemStack.isEmpty()) continue;

                    if (!(itemStack.getItem() instanceof HotIron))
                        continue;

                    Tongs.setTargetStack(tongs.get(), itemStack);
                    itemStack.shrink(1);
                }
                anvilEntity.removeItems(serverPlayer);
                return InteractionResult.SUCCESS;
            }

            if (stack.getItem() instanceof SmithingHammer) return InteractionResult.PASS;

            ItemStack newStack = stack;

            if (stack.getItem() instanceof CraftmanAnvilHelper helper) {
                newStack = helper.acceptCraftmanAnvilItem(stack);
            }

            boolean wasAdded = anvilEntity.addItem(newStack);
            if (wasAdded) {
                level.playSound(null, pos, SoundEvents.METAL_PLACE, SoundSource.BLOCKS, 0.5f, 1.0f);
                anvilEntity.checkAndSpawnRecipeParticles();
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public static Optional<ItemStack> getTongsFromInventory(Player player) {
        return player.getInventory().items.stream()
                .filter(stack -> !stack.isEmpty() && stack.getItem() instanceof Tongs && !Tongs.hasTargetStack(stack))
                .findFirst();
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState newState, boolean moved) {
        if (!blockState.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof CraftmanAnvilBlockEntity anvilEntity) {
                Containers.dropContents(level, blockPos, anvilEntity);
            }
            super.onRemove(blockState, level, blockPos, newState, moved);
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        // This will be implemented differently per loader
        return null; // To be overridden by loader-specific implementations
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        level.scheduleTick(blockPos, this, this.getFallDelay());
    }

    @Override
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (canFallThrough(serverLevel.getBlockState(blockPos.below())) && blockPos.getY() >= serverLevel.getMinBuildHeight()) {
            BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
            CompoundTag nbt = null;

            if (blockEntity != null) {
                nbt = blockEntity.saveWithId();
            }

            serverLevel.removeBlockEntity(blockPos);
            serverLevel.removeBlock(blockPos, false);

            FallingBlockEntity falling = FallingBlockEntity.fall(serverLevel, blockPos, blockState);

            if (nbt != null) {
                falling.blockData = nbt.copy();
            }
        }
    }

    public static boolean canFallThrough(BlockState state) {
        return state.isAir() || state.is(BlockTags.FIRE) || state.liquid() || state.canBeReplaced();
    }

    protected int getFallDelay() {
        return 2;
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block sourceBlock, BlockPos fromPos, boolean notify) {
        super.neighborChanged(blockState, level, blockPos, sourceBlock, fromPos, notify);
        level.scheduleTick(blockPos, this, this.getFallDelay());
    }
}