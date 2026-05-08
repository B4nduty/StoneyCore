package banduty.stoneycore.recipes;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

public interface SCRecipes {
    Supplier<RecipeType<CraftmanAnvilRecipe>> CRAFTMAN_ANVIL_RECIPE_TYPE = registerType("craftman_anvil_crafting");

    Supplier<RecipeSerializer<CraftmanAnvilRecipe>> CRAFTMAN_ANVIL_RECIPE_SERIALIZER = registerSerializer("craftman_anvil_crafting", new CraftmanAnvilRecipe.Serializer());

    Supplier<RecipeSerializer<ManuscriptCraftingRecipe>> MANUSCRIPT_SERIALIZER = registerSerializer("manuscript_crafting", new ManuscriptCraftingRecipe.Serializer());

    Supplier<RecipeSerializer<BannerPatternRecipe>> BANNER_SERIALIZER = registerSerializer("banner_pattern_crafting", new BannerPatternRecipe.Serializer());

    @SuppressWarnings("unchecked")
    static <T extends Recipe<?>> Supplier<RecipeType<T>> registerType(String key) {
        return Services.PLATFORM.register((Registry<RecipeType<T>>) (Registry<?>) BuiltInRegistries.RECIPE_TYPE, key,
                () -> new RecipeType<>() {
                    @Override
                    public String toString() {
                        return key;
                    }
                }
        );
    }

    @SuppressWarnings("unchecked")
    static <S extends RecipeSerializer<T>, T extends Recipe<?>> Supplier<S> registerSerializer(String key, S recipeSerializer) {
        return ((Supplier<S>) Services.PLATFORM.register(BuiltInRegistries.RECIPE_SERIALIZER, key, () -> recipeSerializer));
    }

    static void register() {
        StoneyCore.LOG.info("Registering ModRecipes for " + StoneyCore.MOD_ID);
    }

}