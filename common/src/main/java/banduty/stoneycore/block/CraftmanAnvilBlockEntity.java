package banduty.stoneycore.block;

import banduty.stoneycore.items.custom.manuscript.Manuscript;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.recipes.AnvilInput;
import banduty.stoneycore.recipes.CraftmanAnvilRecipe;
import banduty.stoneycore.recipes.SCRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.UUID;

public class CraftmanAnvilBlockEntity extends BlockEntity implements ImplementedInventory {

    private static final int OUTPUT_SLOT = 0;
    private static final int FIRST_INGREDIENT_SLOT = 1;

    protected final NonNullList<ItemStack> items;
    private int hitCount = 0;
    private UUID lastHitter = null;
    private boolean lastRecipeValid = false;

    public CraftmanAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(SCBlocks.CRAFTMAN_ANVIL_BLOCK_ENTITY.get(), pos, state);
        this.items = NonNullList.withSize(FIRST_INGREDIENT_SLOT + CraftmanAnvilRecipe.GRID_SIZE, ItemStack.EMPTY);
    }

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> hitCount;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) hitCount = value;
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public Optional<RecipeHolder<CraftmanAnvilRecipe>> getRecipe() {
        if (level == null) return Optional.empty();

        AnvilInput input = new AnvilInput(
                items.get(1), items.get(2), items.get(3),
                items.get(4), items.get(5), items.get(6)
        );

        return level.getRecipeManager()
                .getRecipeFor(SCRecipes.CRAFTMAN_ANVIL_RECIPE_TYPE.get(), input, level);
    }

    public int getHitCount() {
        return hitCount;
    }

    public void checkAndSpawnRecipeParticles() {
        if (level == null || level.isClientSide()) return;

        boolean hasItems = false;
        for (int i = FIRST_INGREDIENT_SLOT; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                hasItems = true;
                break;
            }
        }

        boolean currentRecipeValid = hasItems && getRecipe().isPresent();

        if (currentRecipeValid && !lastRecipeValid) {
            spawnParticles(ParticleTypes.HAPPY_VILLAGER, 10);
        }

        if (!currentRecipeValid && lastRecipeValid) {
            spawnParticles(new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f), 10);
        }

        lastRecipeValid = currentRecipeValid;
    }

    private void spawnParticles(ParticleOptions particleType, int count) {
        if (level == null || level.isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) level;
        RandomSource random = level.random;
        BlockPos pos = getBlockPos();

        for (int i = 0; i < count; i++) {
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
            double y = pos.getY() + 1.0 + random.nextDouble() * 0.3;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;

            serverLevel.sendParticles(particleType, x, y, z, 1, 0, 0, 0, 0);
        }
    }

    public void hitAnvil(Player player) {
        if (level == null) return;

        Optional<RecipeHolder<CraftmanAnvilRecipe>> recipeOpt = getRecipe();

        if (recipeOpt.isEmpty()) {
            hitCount = 0;
            setChanged();
            return;
        }

        RecipeHolder<CraftmanAnvilRecipe> recipeHolder = recipeOpt.get();
        CraftmanAnvilRecipe recipe = recipeHolder.value();

        hitCount++;
        lastHitter = player.getUUID();

        level.playSound(null, getBlockPos(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.5f, 1.0f);

        if (hitCount >= recipe.hitTimes()) {
            completeCrafting(recipe);
        }

        setChanged();
    }

    private void completeCrafting(CraftmanAnvilRecipe recipe) {
        if (level == null || !(level instanceof ServerLevel serverLevel)) return;

        RandomSource random = level.random;

        ItemStack result = random.nextFloat() < recipe.chance()
                ? recipe.output().copy()
                : ItemStack.EMPTY;

        NonNullList<ItemStack> remainders = getRecipeReminder();

        for (int i = FIRST_INGREDIENT_SLOT; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }

        items.set(OUTPUT_SLOT, result);
        lastRecipeValid = false;

        for (ItemStack remainder : remainders) {
            if (!remainder.isEmpty()) {
                boolean added = false;

                for (int j = FIRST_INGREDIENT_SLOT; j < items.size(); j++) {
                    if (!items.get(j).isEmpty() && ItemStack.isSameItemSameComponents(items.get(j), remainder)) {
                        int newCount = items.get(j).getCount() + remainder.getCount();
                        if (newCount <= items.get(j).getMaxStackSize()) {
                            items.get(j).setCount(newCount);
                            added = true;
                            break;
                        }
                    }
                }

                if (!added) {
                    for (int j = FIRST_INGREDIENT_SLOT; j < items.size(); j++) {
                        if (items.get(j).isEmpty()) {
                            items.set(j, remainder);
                            added = true;
                            break;
                        }
                    }
                }

                if (!added && !level.isClientSide()) {
                    dropStack(level, getBlockPos(), remainder);
                }
            }
        }

        if (result.isEmpty()) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, getBlockPos().getX() + 0.5, getBlockPos().getY() + 1, getBlockPos().getZ() + 0.5,
                    1, 0f, 0f, 0f, 0.005f);
        }

        hitCount = 0;
        setChanged();
    }

    public NonNullList<ItemStack> getRecipeReminder() {
        NonNullList<ItemStack> remainders = NonNullList.withSize(CraftmanAnvilRecipe.GRID_SIZE, ItemStack.EMPTY);

        for (int i = 0; i < CraftmanAnvilRecipe.GRID_SIZE; i++) {
            ItemStack stack = items.get(i + FIRST_INGREDIENT_SLOT);
            if (!stack.isEmpty()) {
                ItemStack remainder = Services.PLATFORM.getCraftingRemainingItem(stack);
                if (!remainder.isEmpty()) {
                    remainders.set(i, remainder.copy());
                }
                if (!stack.isEmpty() && stack.getItem() instanceof Manuscript) {
                    remainders.set(i, stack.copy());
                }
            }
        }

        return remainders;
    }

    private void dropStack(Level level, BlockPos pos, ItemStack stack) {
        if (!stack.isEmpty() && !level.isClientSide()) {
            ItemEntity itemEntity = new ItemEntity(
                    level,
                    pos.getX() + 0.5,
                    pos.getY() + 1.0,
                    pos.getZ() + 0.5,
                    stack
            );
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

    public void tick(Level level) {
        if (level.isClientSide()) {
            return;
        }
        if (getRecipe().isEmpty() && hitCount > 0) {
            hitCount = 0;
        }

        inventoryTick();

        if (level.getGameTime() % 20 == 0) {
            checkAndSpawnRecipeParticles();
        }
    }

    protected void inventoryTick() {
        if (level == null) return;

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                stack.inventoryTick(level, createTempEntity(level), i, false);
                setChanged();
            }
        }
    }

    protected Pig createTempEntity(Level level) {
        Pig tempEntity = new Pig(EntityType.PIG, level);
        tempEntity.setPos(getBlockPos().getX() + 0.5, getBlockPos().getY() + 1, getBlockPos().getZ() + 0.5);
        tempEntity.setInvulnerable(true);
        tempEntity.setInvisible(true);
        tempEntity.setNoGravity(true);
        return tempEntity;
    }

    @Override
    public void setChanged() {
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
        super.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        ContainerHelper.saveAllItems(nbt, items, registries);
        nbt.putInt("HitCount", hitCount);
        if (lastHitter != null) {
            nbt.putUUID("LastHitter", lastHitter);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        this.items.clear();
        ContainerHelper.loadAllItems(nbt, items, registries);
        this.hitCount = nbt.getInt("HitCount");
        if (nbt.hasUUID("LastHitter")) {
            this.lastHitter = nbt.getUUID("LastHitter");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        nbt.putInt("HitCount", hitCount);
        ContainerHelper.saveAllItems(nbt, this.items, registries);
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ContainerData getContainerData() {
        return data;
    }

    public boolean hasOutput() {
        return !items.get(OUTPUT_SLOT).isEmpty();
    }

    public void takeAll(Player player) {
        if (level != null && level.isClientSide()) {
            return;
        }

        ItemStack output = items.get(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return;
        }

        ItemStack copy = output.copy();
        if (!player.getInventory().add(copy)) {
            player.drop(copy, false);
        }

        items.set(OUTPUT_SLOT, ItemStack.EMPTY);

        for (int i = FIRST_INGREDIENT_SLOT; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                copy = items.get(i).copy();
                player.drop(copy, false);
                items.set(i, ItemStack.EMPTY);
            }
        }
        setChanged();
        checkAndSpawnRecipeParticles();
    }

    public boolean addItem(ItemStack stack) {
        return addItem(stack, null);
    }

    public boolean addItem(ItemStack stack, Player player) {
        if (level != null && level.isClientSide()) {
            return false;
        }

        for (int i = FIRST_INGREDIENT_SLOT; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                return addItem(stack, player, i);
            }
        }

        return false;
    }

    public boolean addItem(ItemStack stack, Player player, int targetSlot) {
        if (level != null && level.isClientSide()) {
            return false;
        }

        if (targetSlot < FIRST_INGREDIENT_SLOT || targetSlot >= items.size()) {
            return false;
        }

        hitCount = 0;

        boolean isIncomingManuscript = stack.getItem() instanceof Manuscript;

        if (isIncomingManuscript) {
            for (int i = FIRST_INGREDIENT_SLOT; i < items.size(); i++) {
                ItemStack existing = items.get(i);

                if (!existing.isEmpty() && existing.getItem() instanceof Manuscript) {
                    ItemStack old = existing.copy();
                    items.set(i, stack.split(1));

                    if (player != null) {
                        if (!player.getInventory().add(old)) {
                            player.drop(old, false);
                        }
                    } else {
                        dropStack(level, getBlockPos(), old);
                    }

                    setChanged();
                    checkAndSpawnRecipeParticles();
                    return true;
                }
            }
        }

        if (items.get(targetSlot).isEmpty()) {
            items.set(targetSlot, stack.split(1));
            setChanged();
            checkAndSpawnRecipeParticles();
            return true;
        }

        return false;
    }

    public void removeItems(Player playerEntity) {
        if (level != null && level.isClientSide()) return;

        hitCount = 0;
        boolean itemsRemoved = false;

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty() && stack.getCount() > 0) {
                ItemStack copy = stack.copy();
                if (!playerEntity.getInventory().add(copy)) {
                    playerEntity.drop(copy, false);
                }
                items.set(i, ItemStack.EMPTY);
                itemsRemoved = true;
            } else {
                items.set(i, ItemStack.EMPTY);
            }
        }

        if (itemsRemoved) {
            checkAndSpawnRecipeParticles();
            setChanged();
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot >= FIRST_INGREDIENT_SLOT && slot < items.size();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == OUTPUT_SLOT;
    }
}