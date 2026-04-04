package banduty.stoneycore.items;

import banduty.stoneycore.lands.util.LandState;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public class SiegeSpawnerItem extends Item {
    private static final Map<EntityType<?>, Item> BY_ID = Maps.newIdentityHashMap();

    private final Supplier<? extends EntityType<?>> typeSupplier;
    private final EntityType<?> defaultType;

    public SiegeSpawnerItem(EntityType<?> defaultType, Properties properties) {
        super(properties);
        this.defaultType = defaultType;
        this.typeSupplier = () -> defaultType;
        BY_ID.put(defaultType, this);
    }

    public SiegeSpawnerItem(Supplier<? extends EntityType<?>> typeSupplier, Properties properties) {
        super(properties);
        this.typeSupplier = typeSupplier;
        this.defaultType = null;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        EntityType<?> type = getType();

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        if (type != null) {
            BlockPos pos = context.getClickedPos().above();
            Entity entity = type.create(level);

            if (entity != null) {
                LandState landState = LandState.get(serverLevel);

                boolean isClaimed = landState.isClaimed(pos);
                boolean isOwnerOrAllie = context.getPlayer() != null && (
                        landState.isOwner(pos, context.getPlayer().getUUID()) ||
                                landState.isAllay(pos, context.getPlayer().getUUID())
                );

                if (!isClaimed || isOwnerOrAllie) {
                    entity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    level.addFreshEntity(entity);
                    context.getItemInHand().shrink(1);
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.FAIL;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    public static Item byId(@Nullable EntityType<?> type) {
        return BY_ID.get(type);
    }

    public EntityType<?> getType() {
        if (defaultType != null) {
            return defaultType;
        }
        return typeSupplier != null ? typeSupplier.get() : null;
    }

    public static void register(EntityType<?> type, Item item) {
        BY_ID.put(type, item);
    }
}