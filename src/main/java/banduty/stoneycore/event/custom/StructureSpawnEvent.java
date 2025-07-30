package banduty.stoneycore.event.custom;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public interface StructureSpawnEvent {
    Event<StructureSpawnEvent> EVENT = EventFactory.createArrayBacked(StructureSpawnEvent.class,
            listeners -> new StructureSpawnEvent() {
                @Override
                public String[][] getBaseAisles() {
                    return null;
                }

                @Override
                public void setKeyMatchers(BlockPatternBuilder builder, Direction dir) {
                    for (StructureSpawnEvent listener : listeners) {
                        listener.setKeyMatchers(builder, dir);
                    }
                }

                @Override
                public @NotNull Entity getEntity(World world) {
                    Entity spawned = null;
                    for (StructureSpawnEvent listener : listeners) {
                        Entity entity = listener.getEntity(world);
                        if (entity != null) {
                            spawned = entity;
                        }
                    }
                    if (spawned == null) {
                        throw new IllegalStateException("No entity returned from any StructureSpawnEvent listener");
                    }
                    return spawned;
                }
            });

    String[][] getBaseAisles();

    void setKeyMatchers(BlockPatternBuilder builder, Direction dir);

    Entity getEntity(World world);
}
