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

import java.util.Map;
import java.util.function.Supplier;

public class SiegeSpawnerItem extends Item {
    public static final Map<EntityType<?>, SiegeSpawnerItem> SIEGE_SPAWNERS = Maps.newIdentityHashMap();
    private final Supplier<? extends EntityType<?>> typeSupplier;

    public SiegeSpawnerItem(Supplier<? extends EntityType<?>> typeSupplier, Properties properties) {
        super(properties);
        this.typeSupplier = typeSupplier;
    }

    public SiegeSpawnerItem(EntityType<?> type, Properties properties) {
        this(() -> type, properties);
        SIEGE_SPAWNERS.put(type, this);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        EntityType<?> type = typeSupplier.get();
        if (level instanceof ServerLevel serverLevel && type != null) {
            BlockPos pos = context.getClickedPos().above();
            Entity entity = type.create(level);

            if (entity != null) {
                if (!SIEGE_SPAWNERS.containsKey(type)) {
                    SIEGE_SPAWNERS.put(type, this);
                }

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

    public static SiegeSpawnerItem forEntity(EntityType<?> type) {
        return SIEGE_SPAWNERS.get(type);
    }
}