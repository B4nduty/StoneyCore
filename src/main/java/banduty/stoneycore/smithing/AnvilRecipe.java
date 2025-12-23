package banduty.stoneycore.smithing;

import banduty.stoneycore.block.CraftmanAnvilBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public record AnvilRecipe(ResourceLocation id, List<StackIngredient> ingredients, ItemStack output, int hitTimes,
                          float chance) implements Recipe<CraftmanAnvilBlockEntity> {

    @Override
    public boolean matches(CraftmanAnvilBlockEntity inventory, Level level) {
        List<ItemStack> inputItems = new ArrayList<>();
        for (int i = 0; i < 6; i++) inputItems.add(inventory.getItem(i));

        List<ItemStack> remainingInputs = new ArrayList<>();
        for (ItemStack stack : inputItems) {
            if (!stack.isEmpty()) {
                remainingInputs.add(stack.copy());
            }
        }

        for (StackIngredient ingredient : ingredients) {
            int neededCount = ingredient.stack().getCount();

            for (int i = 0; i < remainingInputs.size() && neededCount > 0; i++) {
                ItemStack inputStack = remainingInputs.get(i);

                if (ingredient.test(inputStack)) {
                    int available = inputStack.getCount();
                    int toTake = Math.min(available, neededCount);

                    inputStack.shrink(toTake);
                    neededCount -= toTake;

                    if (inputStack.getCount() == 0) {
                        remainingInputs.remove(i);
                        i--;
                    }
                }
            }

            if (neededCount > 0) {
                return false;
            }
        }

        for (ItemStack remaining : remainingInputs) {
            if (!remaining.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftmanAnvilBlockEntity inventory, @NotNull RegistryAccess registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public @NotNull ItemStack getResultItem(RegistryAccess registries) {
        return output;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipes.ANVIL_RECIPE_SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipes.ANVIL_RECIPE_TYPE;
    }
}