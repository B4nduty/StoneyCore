package banduty.stoneycore.compat.rei;

import banduty.stoneycore.block.ModBlocks;
import banduty.stoneycore.smithing.AnvilRecipe;
import banduty.stoneycore.smithing.ModRecipes;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;

public class StoneyCoreREIClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new CraftmanAnvilCategoryREI());
        registry.addWorkstations(CraftmanAnvilCategoryREI.CRAFTMAN_ANVIL, EntryStacks.of(ModBlocks.CRAFTMAN_ANVIL));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerRecipeFiller(AnvilRecipe.class, ModRecipes.ANVIL_RECIPE_TYPE, CraftmanAnvilDisplayREI::new);
    }
}
