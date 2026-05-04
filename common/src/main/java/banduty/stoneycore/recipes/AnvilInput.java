package banduty.stoneycore.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public record AnvilInput(ItemStack slot0, ItemStack slot1, ItemStack slot2,
        ItemStack slot3, ItemStack slot4, ItemStack slot5) implements RecipeInput {

    @Override
    public @NotNull ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> slot0;
            case 1 -> slot1;
            case 2 -> slot2;
            case 3 -> slot3;
            case 4 -> slot4;
            case 5 -> slot5;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 6;
    }
}