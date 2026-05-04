package banduty.stoneycore.compat.jei;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.block.SCBlocks;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.custom.manuscript.Manuscript;
import banduty.stoneycore.recipes.CraftmanAnvilRecipe;
import banduty.stoneycore.recipes.SCRecipes;
import banduty.stoneycore.screen.BlueprintScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class StoneyCoreJEIPlugin implements IModPlugin {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CraftmanAnvilCategoryJEI(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, SCItems.MANUSCRIPT, (stack, context) -> {
            ItemStack target = Manuscript.getTargetStack(stack);

            if (target.isEmpty()) return "";

            return BuiltInRegistries.ITEM.getKey(target.getItem()).toString();
        });
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<RecipeHolder<CraftmanAnvilRecipe>> recipeHolders = recipeManager.getAllRecipesFor(SCRecipes.CRAFTMAN_ANVIL_RECIPE_TYPE);

        List<CraftmanAnvilRecipe> anvilRecipes = recipeHolders.stream()
                .map(RecipeHolder::value)
                .toList();

        registration.addRecipes(CraftmanAnvilCategoryJEI.CRAFTMAN_ANVIL_TYPE, anvilRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(SCBlocks.CRAFTMAN_ANVIL),
                CraftmanAnvilCategoryJEI.CRAFTMAN_ANVIL_TYPE
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(BlueprintScreen.class,
                new IGuiContainerHandler<>() {
                    @Override
                    public List<Rect2i> getGuiExtraAreas(BlueprintScreen screen) {
                        return List.of(new Rect2i(
                                screen.getX(),
                                screen.getY(),
                                screen.getWidth(),
                                screen.getHeight()
                        ));
                    }
                });
    }
}
