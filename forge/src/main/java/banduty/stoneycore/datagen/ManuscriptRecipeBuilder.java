package banduty.stoneycore.datagen;

import banduty.stoneycore.recipes.ModRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public record ManuscriptRecipeBuilder(ResourceLocation id, Item result, NonNullList<Ingredient> ingredients) implements FinishedRecipe {
    @Override
    public void serializeRecipeData(JsonObject json) {
        JsonArray jsonArray = new JsonArray();
        for (Ingredient ingredient : ingredients) {
            jsonArray.add(ingredient.toJson());
        }
        json.add("ingredients", jsonArray);

        JsonObject resultJson = new JsonObject();
        resultJson.addProperty("item", BuiltInRegistries.ITEM.getKey(result).toString());
        json.add("result", resultJson);
    }

    @Override
    public ResourceLocation getId() { return id; }

    @Override
    public RecipeSerializer<?> getType() {
        return ModRecipes.MANUSCRIPT_SERIALIZER.get();
    }

    @Override
    public JsonObject serializeRecipe() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "stoneycore:manuscript_crafting");
        serializeRecipeData(json);
        return json;
    }

    @Override public JsonObject serializeAdvancement() { return null; }
    @Override public ResourceLocation getAdvancementId() { return null; }
}