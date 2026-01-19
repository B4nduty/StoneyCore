package banduty.stoneycore.datagen;

import banduty.stoneycore.smithing.ModRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CraftmanAnvilRecipeJsonBuilder {
    private final ItemStack result;
    private final List<Ingredient> inputs = new ArrayList<>();
    private int hitTimes = 1;
    private float chance = 1.0f;

    private CraftmanAnvilRecipeJsonBuilder(ItemStack result) {
        this.result = result;
    }

    public static CraftmanAnvilRecipeJsonBuilder create(ItemStack result) {
        return new CraftmanAnvilRecipeJsonBuilder(result);
    }

    public CraftmanAnvilRecipeJsonBuilder requires(ItemStack stack) {
        this.inputs.add(new ItemIngredient(stack));
        return this;
    }

    public CraftmanAnvilRecipeJsonBuilder requires(TagKey<Item> tag, int count) {
        this.inputs.add(new TagIngredient(tag, count));
        return this;
    }

    public CraftmanAnvilRecipeJsonBuilder requires(TagKey<Item> tag) {
        return requires(tag, 1);
    }

    public CraftmanAnvilRecipeJsonBuilder hitTimes(int times) { this.hitTimes = times; return this; }
    public CraftmanAnvilRecipeJsonBuilder chance(float c) { this.chance = c; return this; }

    public void save(Consumer<FinishedRecipe> exporter, ResourceLocation resourceLocation) {
        exporter.accept(new Provider(resourceLocation));
    }

    private interface Ingredient {
        JsonObject toJson();
    }

    private record ItemIngredient(ItemStack stack) implements Ingredient {
        private ItemIngredient(ItemStack stack) {
            this.stack = stack.copy();
        }

        @Override
        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            obj.addProperty("count", stack.getCount());
            if (stack.hasTag()) {
                obj.addProperty("nbt", stack.getTag().toString());
            }
            return obj;
        }
    }

    private record TagIngredient(TagKey<Item> tag, int count) implements Ingredient {

        @Override
        public JsonObject toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("tag", tag.location().toString());
            obj.addProperty("count", count);
            return obj;
        }
    }

    private class Provider implements FinishedRecipe {
        private final ResourceLocation resourceLocation;
        Provider(ResourceLocation id) { this.resourceLocation = id; }

        @Override
        public void serializeRecipeData(JsonObject json) {
            json.addProperty("type", "stoneycore:craftman_anvil_crafting");

            JsonArray ingredients = new JsonArray();
            for (Ingredient ingredient : inputs) {
                ingredients.add(ingredient.toJson());
            }
            json.add("ingredients", ingredients);

            JsonObject resultObj = new JsonObject();
            resultObj.addProperty("item", BuiltInRegistries.ITEM.getKey(result.getItem()).toString());
            resultObj.addProperty("count", result.getCount());
            if (result.hasTag()) {
                resultObj.addProperty("nbt", result.getTag().toString());
            }
            json.add("result", resultObj);

            json.addProperty("hit_times", hitTimes);
            json.addProperty("chance", chance);
        }

        @Override public @NotNull ResourceLocation getId() { return resourceLocation; }
        @Override public @NotNull RecipeSerializer<?> getType() { return ModRecipes.ANVIL_RECIPE_SERIALIZER.get(); }
        @Override public JsonObject serializeAdvancement() { return null; }
        @Override public ResourceLocation getAdvancementId() { return null; }
    }
}