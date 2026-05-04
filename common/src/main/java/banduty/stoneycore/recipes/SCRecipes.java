package banduty.stoneycore.recipes;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public interface SCRecipes {
    RecipeType<CraftmanAnvilRecipe> CRAFTMAN_ANVIL_RECIPE_TYPE = registerType("craftman_anvil_crafting");

    RecipeSerializer<CraftmanAnvilRecipe> CRAFTMAN_ANVIL_RECIPE_SERIALIZER = registerSerializer("craftman_anvil_crafting", new CraftmanAnvilRecipe.Serializer());

    RecipeSerializer<ManuscriptCraftingRecipe> MANUSCRIPT_SERIALIZER = registerSerializer("manuscript_crafting", new ManuscriptCraftingRecipe.Serializer());

    RecipeSerializer<BannerPatternRecipe> BANNER_SERIALIZER = registerSerializer("banner_pattern_crafting", new BannerPatternRecipe.Serializer());

    @SuppressWarnings("unchecked")
    static <T extends Recipe<?>> RecipeType<T> registerType(String key) {
        return (RecipeType<T>) Services.PLATFORM.register(BuiltInRegistries.RECIPE_TYPE, key, () -> new RecipeType<T>() {
                    @Override
                    public String toString() {
                        return key;
                    }
                }
        ).get();
    }

    @SuppressWarnings("unchecked")
    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S registerSerializer(String key, S recipeSerializer) {
        return (S) Services.PLATFORM.register(BuiltInRegistries.RECIPE_SERIALIZER, key, () -> recipeSerializer).get();
    }
    static void register() {
        StoneyCore.LOG.info("Registering ModRecipes for " + StoneyCore.MOD_ID);
    }

}