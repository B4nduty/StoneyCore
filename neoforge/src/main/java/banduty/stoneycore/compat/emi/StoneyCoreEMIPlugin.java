package banduty.stoneycore.compat.emi;

import banduty.stoneycore.block.SCBlocks;
import banduty.stoneycore.recipes.CraftmanAnvilRecipe;
import banduty.stoneycore.recipes.SCRecipes;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@EmiEntrypoint
public class StoneyCoreEMIPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(CraftmanAnvilCategoryEMI.CATEGORY);
        registry.addWorkstation(CraftmanAnvilCategoryEMI.CATEGORY,
                EmiStack.of(new ItemStack(SCBlocks.CRAFTMAN_ANVIL.get())));

        RecipeManager recipeManager = registry.getRecipeManager();

        List<RecipeHolder<CraftmanAnvilRecipe>> recipeHolders =
                recipeManager.getAllRecipesFor(SCRecipes.CRAFTMAN_ANVIL_RECIPE_TYPE.get());

        for (RecipeHolder<CraftmanAnvilRecipe> holder : recipeHolders) {
            registry.addRecipe(new CraftmanAnvilRecipeEMI(holder));
        }
    }
}