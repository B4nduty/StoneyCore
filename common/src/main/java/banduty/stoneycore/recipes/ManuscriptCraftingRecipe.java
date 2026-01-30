package banduty.stoneycore.recipes;

import banduty.stoneycore.items.SmithingHammer;
import banduty.stoneycore.items.manuscript.Manuscript;
import banduty.stoneycore.platform.Services;import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ManuscriptCraftingRecipe extends CustomRecipe {
    public ManuscriptCraftingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack paper = ItemStack.EMPTY;
        ItemStack hammer = ItemStack.EMPTY;
        ItemStack itemInput = ItemStack.EMPTY;
        int count = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                count++;
                if (stack.is(Items.PAPER)) paper = stack;
                else if (stack.getItem() instanceof SmithingHammer) hammer = stack;
                else itemInput = stack;
            }
        }
        // Only return true if exactly these 3 items are present
        return count == 3 && !paper.isEmpty() && !hammer.isEmpty() && !itemInput.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registry) {
        ItemStack itemInput = ItemStack.EMPTY;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && !stack.is(Items.PAPER) && !(stack.getItem() instanceof SmithingHammer)) {
                itemInput = stack;
                break;
            }
        }
        // This creates the actual item that appears in the output slot
        return Manuscript.createForStack(itemInput);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Services.PLATFORM.getManuscriptRecipeSerializer();
    }
}