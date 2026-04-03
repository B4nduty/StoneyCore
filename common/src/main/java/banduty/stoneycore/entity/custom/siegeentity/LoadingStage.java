package banduty.stoneycore.entity.custom.siegeentity;

import net.minecraft.world.item.Item;

public record LoadingStage(Item item, int amount, boolean consumesItem, boolean damagesItem) {

    public static LoadingStage of(Item item) {
        return new LoadingStage(item, 1, true, false);
    }

    public static LoadingStage of(Item item, int amount) {
        return new LoadingStage(item, amount, true, false);
    }

    public static LoadingStage ofDamaging(Item item) {
        return new LoadingStage(item, 1, false, true);
    }

    public static LoadingStage ofDamaging(Item item, int amount) {
        return new LoadingStage(item, amount, false, true);
    }

    public boolean matches(Item other) {
        return this.item == other;
    }
}