package banduty.stoneycore.entity.siege;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * One step of a loading sequence (or one ammo type + its cost).
 * The cost handling lives here so no engine has to re-implement it.
 */
public record LoadingStage(Item item, int amount, boolean consumesItem, boolean damagesItem, boolean ignites) {

    public static LoadingStage of(Item item) {
        return of(item, 1);
    }

    public static LoadingStage of(Item item, int amount) {
        return new LoadingStage(item, amount, true, false, false);
    }

    public static LoadingStage ofDamaging(Item item) {
        return ofDamaging(item, 1);
    }

    public static LoadingStage ofDamaging(Item item, int amount) {
        return new LoadingStage(item, amount, false, true, false);
    }

    /** Final stage: damages the tool, then the engine fires. */
    public static LoadingStage ofIgnition(Item item) {
        return new LoadingStage(item, 1, false, true, true);
    }

    public boolean matches(Item other) {
        return this.item == other;
    }

    public boolean hasEnough(ItemStack stack) {
        return !consumesItem || stack.getCount() >= amount;
    }

    /** Pays this stage's cost. Creative players pay nothing. Call {@link #hasEnough} first. */
    public void apply(Player player, ItemStack stack) {
        if (player.isCreative()) return;
        if (consumesItem) stack.shrink(amount);
        if (damagesItem) stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
    }
}
