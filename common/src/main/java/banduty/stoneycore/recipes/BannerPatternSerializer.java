package banduty.stoneycore.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.crafting.*;

import java.util.Arrays;

public class BannerPatternSerializer implements RecipeSerializer<BannerPatternRecipe> {
    public static final BannerPatternSerializer INSTANCE = new BannerPatternSerializer();

    @Override
    public BannerPatternRecipe fromJson(ResourceLocation id, JsonObject json) {
        Ingredient input = Ingredient.EMPTY;
        JsonArray ingredients = GsonHelper.getAsJsonArray(json, "ingredients");

        for (var element : ingredients) {
            Ingredient ing = Ingredient.fromJson(element);

            boolean isBanner = Arrays.stream(ing.getItems()).anyMatch(s -> s.getItem() instanceof BannerItem);
            if (!isBanner) {
                input = ing;
                break;
            }
        }

        CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                GsonHelper.getAsString(json, "category", null),
                CraftingBookCategory.MISC
        );

        return new BannerPatternRecipe(id, category, input);
    }

    @Override
    public BannerPatternRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
        Ingredient input = Ingredient.fromNetwork(buf);
        return new BannerPatternRecipe(id, category, input);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, BannerPatternRecipe recipe) {
        buf.writeEnum(recipe.category());
        recipe.getInput().toNetwork(buf);
    }
}