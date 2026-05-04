package banduty.stoneycore.compat.rei;

import banduty.stoneycore.block.SCBlocks;
import banduty.stoneycore.recipes.CraftmanAnvilRecipe;
import banduty.stoneycore.recipes.SCRecipes;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;

public class StoneyCoreREIClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new CraftmanAnvilCategoryREI());
        registry.addWorkstations(CraftmanAnvilCategoryREI.CRAFTMAN_ANVIL, EntryStacks.of(SCBlocks.CRAFTMAN_ANVIL));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(CraftmanAnvilRecipe.class, SCRecipes.CRAFTMAN_ANVIL_RECIPE_TYPE, CraftmanAnvilDisplayREI::new);
    }
}
