package banduty.stoneycore.recipes;

import banduty.stoneycore.items.SmithingHammer;
import banduty.stoneycore.items.manuscript.Manuscript;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class ManuscriptCraftingRecipe extends ShapelessRecipe {

    public ManuscriptCraftingRecipe(ResourceLocation id,
                                    CraftingBookCategory category,
                                    ItemStack result,
                                    NonNullList<Ingredient> ingredients) {
        super(id, "", category, result, ingredients);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registry) {
        ItemStack itemInput = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (!stack.isEmpty()
                    && !(stack.getItem() instanceof SmithingHammer)
                    && !stack.is(net.minecraft.world.item.Items.PAPER)) {
                itemInput = stack;
                break;
            }
        }

        return Manuscript.createForStack(itemInput);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer container) {
        NonNullList<ItemStack> remaining =
                NonNullList.withSize(container.getContainerSize(), ItemStack.EMPTY);

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (stack.getItem() instanceof SmithingHammer) {
                ItemStack hammerCopy = stack.copy();

                int dmg = hammerCopy.getDamageValue() + 1;
                if (dmg < hammerCopy.getMaxDamage()) {
                    hammerCopy.setDamageValue(dmg);
                    remaining.set(i, hammerCopy);
                }
            }
        }

        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ManuscriptSerializer.INSTANCE;
    }
}