package banduty.stoneycore.compat.jei;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.block.ModBlocks;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.recipes.AnvilRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class StoneyCoreJEIPlugin implements IModPlugin {
    public static final ResourceLocation UID = new ResourceLocation(StoneyCore.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CraftmanAnvilCategoryJEI(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        List<AnvilRecipe> anvilRecipes = recipeManager.getAllRecipesFor(Services.PLATFORM.getCraftmanAnvilRecipe());
        registration.addRecipes(CraftmanAnvilCategoryJEI.CRAFTMAN_ANVIL_TYPE, anvilRecipes);
    }
    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.CRAFTMAN_ANVIL),
                CraftmanAnvilCategoryJEI.CRAFTMAN_ANVIL_TYPE
        );
    }
}
