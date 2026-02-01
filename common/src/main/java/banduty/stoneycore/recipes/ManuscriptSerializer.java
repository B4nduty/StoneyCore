package banduty.stoneycore.recipes;

import banduty.stoneycore.items.SmithingHammer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

import java.util.Arrays;

public class ManuscriptSerializer implements RecipeSerializer<ManuscriptCraftingRecipe> {
    public static final ManuscriptSerializer INSTANCE = new ManuscriptSerializer();

    @Override
    public ManuscriptCraftingRecipe fromJson(ResourceLocation id, JsonObject json) {
        Ingredient input = Ingredient.EMPTY;
        JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");

        for (var element : ingredients) {
            Ingredient ing = Ingredient.fromJson(element);

            boolean isConstraintItem = Arrays.stream(ing.getItems()).anyMatch(stack ->
                    stack.is(Items.PAPER) || stack.getItem() instanceof SmithingHammer
            );

            if (!isConstraintItem) {
                input = ing;
            }
        }

        CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                GsonHelper.getAsString(json, "category", null),
                CraftingBookCategory.MISC
        );

        return new ManuscriptCraftingRecipe(id, category, input);
    }

    @Override
    public ManuscriptCraftingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
        Ingredient input = Ingredient.fromNetwork(buf);
        return new ManuscriptCraftingRecipe(id, category, input);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, ManuscriptCraftingRecipe recipe) {
        buf.writeEnum(recipe.category());
        recipe.getInput().toNetwork(buf);
    }
}