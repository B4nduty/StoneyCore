package banduty.stoneycore.recipes;

import banduty.stoneycore.StoneyCore;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

public interface ModRecipes {
    static void registerRecipes() {
        StoneyCore.LOG.info("Registering ModRecipes for " + StoneyCore.MOD_ID);
    }

    RecipeType<AnvilRecipe> ANVIL_RECIPE_TYPE = Registry.register(
            BuiltInRegistries.RECIPE_TYPE,
            new ResourceLocation(StoneyCore.MOD_ID, "craftman_anvil_crafting"),
            new RecipeType<AnvilRecipe>() {
                public String toString() {
                    return "craftman_anvil_crafting";
                }
            }
    );

    RecipeSerializer<AnvilRecipe> ANVIL_RECIPE_SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            new ResourceLocation(StoneyCore.MOD_ID, "craftman_anvil_crafting"),
            AnvilRecipeSerializer.INSTANCE
    );

    RecipeSerializer<ManuscriptCraftingRecipe> MANUSCRIPT_SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            new ResourceLocation(StoneyCore.MOD_ID, "manuscript_crafting"),
            new SimpleCraftingRecipeSerializer<>(ManuscriptCraftingRecipe::new));

    RecipeSerializer<BannerPatternRecipe> BANNER_SERIALIZER = Registry.register(
            BuiltInRegistries.RECIPE_SERIALIZER,
            new ResourceLocation(StoneyCore.MOD_ID, "banner_pattern_crafting"),
            new SimpleCraftingRecipeSerializer<>(BannerPatternRecipe::new));
}
