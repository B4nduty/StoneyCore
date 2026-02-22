package banduty.stoneycore.datagen;

import banduty.stoneycore.recipes.ModRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

public record BannerRecipeBuilder(
        ResourceLocation id,
        Item result,
        NonNullList<Ingredient> ingredients,
        CraftingBookCategory category
) implements FinishedRecipe {

    @Override
    public void serializeRecipeData(JsonObject json) {

        // category (required for shapeless)
        json.addProperty("category", category.getSerializedName());

        // ingredients
        JsonArray jsonArray = new JsonArray();
        for (Ingredient ingredient : ingredients) {
            jsonArray.add(ingredient.toJson());
        }
        json.add("ingredients", jsonArray);

        // result
        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("item",
                BuiltInRegistries.ITEM.getKey(result).toString());
        resultJson.addProperty("count", 1);
        json.add("result", resultJson);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getType() {
        return ModRecipes.BANNER_SERIALIZER.get();
    }

    // DO NOT override serializeRecipe()

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return null;
    }
}