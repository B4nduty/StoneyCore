package banduty.stoneycore.block;

import banduty.stoneycore.items.custom.CraftmanAnvilHelper;
import banduty.stoneycore.items.custom.SmithingHammer;
import banduty.stoneycore.items.custom.hotiron.HotIron;
import banduty.stoneycore.items.custom.tongs.Tongs;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
    public static final MapCodec<CraftmanAnvilBlock> CODEC = simpleCodec(CraftmanAnvilBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 10, 14);

    public CraftmanAnvilBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CraftmanAnvilBlockEntity(blockPos, blockState);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
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
                .any().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (level.isClientSide()) {
            return ItemInteractionResult.sidedSuccess(true);
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof CraftmanAnvilBlockEntity anvilEntity)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.getItem() instanceof SmithingHammer) {
            if (anvilEntity.getRecipe().isEmpty()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }

            anvilEntity.hitAnvil(serverPlayer);

            if (!player.isCreative()) {
                stack.hurtAndBreak(
                        1,
                        serverPlayer,
                        LivingEntity.getSlotForHand(hand)
                );

                if (stack.getDamageValue() >= stack.getMaxDamage()) {
                    stack.shrink(1);
                }
            }

            return ItemInteractionResult.SUCCESS;
        }

        ItemStack tongsStack = ItemStack.EMPTY;

        if (player.getMainHandItem().getItem() instanceof Tongs) {
            tongsStack = player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof Tongs) {
            tongsStack = player.getOffhandItem();
        }

        boolean usingTongs = !tongsStack.isEmpty();


        if (usingTongs || stack.isEmpty()) {
            if (Tongs.getTargetStack(tongsStack).isEmpty()) {
                NonNullList<ItemStack> items = anvilEntity.getItems();

                for (int i = 0; i < items.size(); i++) {

                    ItemStack item = items.get(i);

                    if (item.isEmpty()) continue;
                    if (!(item.getItem() instanceof HotIron)) continue;

                    if (usingTongs) {
                        Tongs.setTargetStack(tongsStack, item.copy());
                        item.shrink(1);
                    }

                    if (item.isEmpty()) {
                        items.set(i, ItemStack.EMPTY);
                    }


                    anvilEntity.removeItems(serverPlayer);

                    return ItemInteractionResult.SUCCESS;
                }
            } else
                // nothing to pick up
                if (stack.isEmpty()) {
                    if (!usingTongs)
                        anvilEntity.removeItems(serverPlayer);
                    //return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
        }

        ItemStack newStack;

        ItemStack insertStack = stack;

        // If main hand is empty, try offhand
        if (insertStack.isEmpty() && !player.getOffhandItem().isEmpty()) {
            insertStack = player.getOffhandItem();
        }

        if (insertStack.getItem() instanceof CraftmanAnvilHelper helper) {
            newStack = helper.acceptCraftmanAnvilItem(insertStack);
        } else {
            newStack = insertStack;
        }

        if (insertStack.isEmpty()) {
            anvilEntity.removeItems(serverPlayer);
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean added = anvilEntity.addItem(newStack, player);

        if (added) {
            level.playSound(
                    null,
                    pos,
                    SoundEvents.METAL_PLACE,
                    SoundSource.BLOCKS,
                    0.5f,
                    1.0f
            );

            anvilEntity.checkAndSpawnRecipeParticles();
        }


        return ItemInteractionResult.SUCCESS;
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
        return createTickerHelper(blockEntityType, SCBlocks.CRAFTMAN_ANVIL_BLOCK_ENTITY.get(),
                (world1, pos, state1, blockEntity) -> blockEntity.tick(world1));
    }

    @Override
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        level.scheduleTick(blockPos, this, this.getFallDelay());
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (canFallThrough(level.getBlockState(pos.below()))
                && pos.getY() >= level.getMinBuildHeight()) {

            BlockEntity blockEntity = level.getBlockEntity(pos);

            // Drop inventory BEFORE removing block
            if (blockEntity instanceof CraftmanAnvilBlockEntity anvil) {
                Containers.dropContents(level, pos, anvil);
            }

            // Remove BE safely
            level.removeBlockEntity(pos);
            level.removeBlock(pos, false);

            // Spawn falling block WITHOUT block entity data
            FallingBlockEntity.fall(level, pos, state);
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

    @Override
    public void onLand(Level pLevel, BlockPos pPos, BlockState pState, BlockState pReplaceableState, FallingBlockEntity pFallingBlock) {
        if (!pFallingBlock.isSilent()) {
            pLevel.levelEvent(1031, pPos, 0);
        }
    }
}