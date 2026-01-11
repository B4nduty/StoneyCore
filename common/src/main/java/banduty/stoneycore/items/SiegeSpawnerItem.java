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
    public static final Map<Supplier<? extends EntityType<?>>, SiegeSpawnerItem> SIEGE_SPAWNERS = Maps.newIdentityHashMap();
    private final Supplier<? extends EntityType<?>> type;

    public SiegeSpawnerItem(Supplier<? extends EntityType<?>> type, Properties properties) {
        super(properties);
        this.type = type;
        SIEGE_SPAWNERS.put(type, this);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            BlockPos pos = context.getClickedPos().above();
            Entity entity = type.get().create(level);

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


    public static SiegeSpawnerItem forEntity(EntityType<?> type) {
        for (Map.Entry<Supplier<? extends EntityType<?>>, SiegeSpawnerItem> entry : SIEGE_SPAWNERS.entrySet()) {
            if (entry.getKey().get() == type) {
                return entry.getValue();
            }
        }
        return null;
    }
}