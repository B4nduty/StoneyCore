package banduty.stoneycore.items.item;

import banduty.stoneycore.lands.util.LandState;
import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public class SiegeSpawnerItem extends Item {
    public static final Map<Supplier<? extends EntityType<?>>, SiegeSpawnerItem> SIEGE_SPAWNERS = Maps.newIdentityHashMap();
    private final Supplier<? extends EntityType<?>> type;

    public SiegeSpawnerItem(Supplier<? extends EntityType<?>> type, Settings settings) {
        super(settings);
        this.type = type;
        SIEGE_SPAWNERS.put(type, this);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            BlockPos pos = context.getBlockPos().up();
            Entity entity = type.get().create(world);

            if (entity != null) {
                LandState landState = LandState.get(serverWorld);

                boolean isClaimed = landState.isClaimed(pos);
                boolean isOwnerOrAllie = context.getPlayer() != null && (
                        landState.isOwner(pos, context.getPlayer().getUuid()) ||
                                landState.isAllay(pos, context.getPlayer().getUuid())
                );

                if (!isClaimed || isOwnerOrAllie) {
                    entity.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                    world.spawnEntity(entity);
                    context.getStack().decrement(1);
                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.FAIL;
                }
            }
        }
        return ActionResult.PASS;
    }


    public static SiegeSpawnerItem forEntity(@Nullable EntityType<?> type) {
        for (Map.Entry<Supplier<? extends EntityType<?>>, SiegeSpawnerItem> entry : SIEGE_SPAWNERS.entrySet()) {
            if (entry.getKey().get() == type) {
                return entry.getValue();
            }
        }
        return null;
    }
}