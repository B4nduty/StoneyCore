package banduty.stoneycore.items.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.world.World;

import java.util.function.BiFunction;

public class SCArrow extends Item {
    private final BiFunction<PlayerEntity, World, Entity> arrowEntityFactory;

    public SCArrow(Settings settings, BiFunction<PlayerEntity, World, Entity> arrowEntityFactory) {
        super(settings);
        this.arrowEntityFactory = arrowEntityFactory;
    }

    public final Entity createArrowEntity(PlayerEntity playerEntity, World world) {
        return arrowEntityFactory.apply(playerEntity, world);
    }
}