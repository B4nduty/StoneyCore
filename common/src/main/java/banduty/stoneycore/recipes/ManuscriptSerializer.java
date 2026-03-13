package banduty.stoneycore.recipes;

import banduty.stoneycore.items.SmithingHammer;
import banduty.stoneycore.items.manuscript.Manuscript;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.Arrays;

public class ManuscriptSerializer implements RecipeSerializer<ManuscriptCraftingRecipe> {

    public static final ManuscriptSerializer INSTANCE = new ManuscriptSerializer();

    @Override
    public ManuscriptCraftingRecipe fromJson(ResourceLocation id, JsonObject json) {

        JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");

        NonNullList<Ingredient> ingredients =
                NonNullList.withSize(array.size(), Ingredient.EMPTY);

        boolean foundPaper = false;
        boolean foundHammer = false;
        int inputCount = 0;
        Ingredient inputIngredient = Ingredient.EMPTY;

        for (int i = 0; i < array.size(); i++) {
            Ingredient ing = Ingredient.fromJson(array.get(i));
            ingredients.set(i, ing);

            boolean isPaper = Arrays.stream(ing.getItems())
                    .anyMatch(s -> s.is(Items.PAPER));

            boolean isHammer = Arrays.stream(ing.getItems())
                    .anyMatch(s -> s.getItem() instanceof SmithingHammer);

            if (isPaper) foundPaper = true;
            else if (isHammer) foundHammer = true;
            else {
                inputIngredient = ing;
                inputCount++;
            }
        }

        if (!foundPaper || !foundHammer || inputCount != 1) {
            throw new JsonParseException(
                    "Manuscript recipe must contain paper + hammer + exactly 1 input");
        }

        CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                GsonHelper.getAsString(json, "category", "misc"),
                CraftingBookCategory.MISC
        );

        ItemStack result = Manuscript.createForStack(inputIngredient.getItems()[0]);

        return new ManuscriptCraftingRecipe(id, category, result, ingredients);
    }

    @Override
    public ManuscriptCraftingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {

        CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
        int size = buf.readVarInt();

        NonNullList<Ingredient> ingredients =
                NonNullList.withSize(size, Ingredient.EMPTY);

        for (int i = 0; i < size; i++) {
            ingredients.set(i, Ingredient.fromNetwork(buf));
        }

        ItemStack result = buf.readItem();

        return new ManuscriptCraftingRecipe(id, category, result, ingredients);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, ManuscriptCraftingRecipe recipe) {

        buf.writeEnum(recipe.category());
        buf.writeVarInt(recipe.getIngredients().size());

        for (Ingredient ing : recipe.getIngredients()) {
            ing.toNetwork(buf);
        }

        buf.writeItem(recipe.getResultItem(null));
    }
}