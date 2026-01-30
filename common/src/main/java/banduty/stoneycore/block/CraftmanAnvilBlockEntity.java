package banduty.stoneycore.block;

import banduty.stoneycore.platform.Services;
import banduty.stoneycore.recipes.AnvilRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.UUID;

public class CraftmanAnvilBlockEntity extends BlockEntity implements ImplementedInventory {
    protected final NonNullList<ItemStack> items;
    private int hitCount = 0;
    private UUID lastHitter = null;
    private boolean lastRecipeValid = false;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> hitCount;
                case 1 -> lastRecipeValid ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> hitCount = value;
                case 1 -> lastRecipeValid = value == 1;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public CraftmanAnvilBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.items = NonNullList.withSize(7, ItemStack.EMPTY);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    public Optional<AnvilRecipe> getRecipe() {
        if (level == null) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(Services.PLATFORM.getCraftmanAnvilRecipe(), this, level);
    }

    public int getHitCount() {
        return hitCount;
    }

    public void checkAndSpawnRecipeParticles() {
        if (level == null || level.isClientSide()) return;

        boolean hasItems = false;
        for (int i = 0; i < 6; i++) {
            if (!items.get(i).isEmpty()) {
                hasItems = true;
                break;
            }
        }

        if (!hasItems) {
            if (lastRecipeValid) {
                lastRecipeValid = false;
            }
            return;
        }

        boolean currentRecipeValid = getRecipe().isPresent();

        if (currentRecipeValid && !lastRecipeValid) {
            spawnParticles(ParticleTypes.HAPPY_VILLAGER, 10);
            lastRecipeValid = true;
        } else if (!currentRecipeValid && lastRecipeValid) {
            spawnParticles(new DustParticleOptions(new Vector3f(1.0f, 0.0f, 0.0f), 1.0f), 10);
            lastRecipeValid = false;
        }
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

        if (getRecipe().isPresent()) {
            hitCount++;
            lastHitter = player.getUUID();

            level.playSound(null, getBlockPos(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.5f, 1.0f);

            if (hitCount >= getRecipe().get().hitTimes()) {
                completeCrafting(getRecipe().get());
            }
            setChanged();
        }
    }

    private void completeCrafting(AnvilRecipe recipe) {
        if (level == null || !(level instanceof ServerLevel serverLevel)) return;

        RandomSource random = level.random;

        ItemStack result = random.nextFloat() < recipe.chance()
                ? recipe.output().copy()
                : ItemStack.EMPTY;

        NonNullList<ItemStack> remainders = getRecipeReminder();

        for (int i = 1; i < 7; i++) {
            items.set(i, ItemStack.EMPTY);
        }

        items.set(0, result);
        lastRecipeValid = false;

        for (ItemStack remainder : remainders) {
            if (!remainder.isEmpty()) {
                boolean added = false;

                for (int j = 1; j < 7; j++) {
                    if (!items.get(j).isEmpty() && ItemStack.isSameItemSameTags(items.get(j), remainder)) {
                        int newCount = items.get(j).getCount() + remainder.getCount();
                        if (newCount <= items.get(j).getMaxStackSize()) {
                            items.get(j).setCount(newCount);
                            added = true;
                            break;
                        }
                    }
                }

                if (!added) {
                    for (int j = 1; j < 7; j++) {
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
        NonNullList<ItemStack> remainders = NonNullList.withSize(6, ItemStack.EMPTY);

        for (int i = 0; i < 6; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                ItemStack remainder = Services.PLATFORM.getCraftingRemainingItem(stack);
                if (!remainder.isEmpty()) {
                    remainders.set(i, remainder.copy());
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

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) {
            return;
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
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        ContainerHelper.saveAllItems(nbt, items, true);
        nbt.putInt("HitCount", hitCount);
        if (lastHitter != null) {
            nbt.putUUID("LastHitter", lastHitter);
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.items.clear();
        ContainerHelper.loadAllItems(nbt, items);
        if (nbt.contains("HitCount")) {
            hitCount = nbt.getInt("HitCount");
        }
        if (nbt.hasUUID("LastHitter")) {
            lastHitter = nbt.getUUID("LastHitter");
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("HitCount", hitCount);
        ContainerHelper.saveAllItems(nbt, this.items, true);
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ContainerData getContainerData() {
        return data;
    }

    public boolean addItem(ItemStack stack) {
        if (level != null && level.isClientSide()) {
            return false;
        }

        hitCount = 0;

        for (int i = 0; i < 6; i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack.split(1));
                setChanged();
                checkAndSpawnRecipeParticles();
                return true;
            }
        }
        return false;
    }

    public void removeItems(Player playerEntity) {
        if (level != null && level.isClientSide()) {
            return;
        }

        hitCount = 0;

        for (int i = 0; i < 6; i++) {
            if (!items.get(i).isEmpty()) {
                playerEntity.addItem(items.get(i));
                items.set(i, ItemStack.EMPTY);
            }
        }

        checkAndSpawnRecipeParticles();
        setChanged();
    }

    // Override WorldlyContainer methods for specific slot access if needed
    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot >= 1 && slot < 7; // Input slots 1-6
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == 0; // Output slot 0
    }
}