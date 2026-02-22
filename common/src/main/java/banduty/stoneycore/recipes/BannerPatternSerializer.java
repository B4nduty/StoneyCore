package banduty.stoneycore.recipes;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class BannerPatternSerializer implements RecipeSerializer<BannerPatternRecipe> {
    public static final BannerPatternSerializer INSTANCE = new BannerPatternSerializer();

    @Override
    public BannerPatternRecipe fromJson(ResourceLocation id, JsonObject json) {

        // Let vanilla parse shapeless JSON
        ShapelessRecipe vanilla = RecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json);

        return new BannerPatternRecipe(
                id,
                vanilla.getGroup(),
                vanilla.category(),
                vanilla.getResultItem(null),
                vanilla.getIngredients()
        );
    }

    @Override
    public BannerPatternRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {

        ShapelessRecipe vanilla = RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buf);

        return new BannerPatternRecipe(
                id,
                vanilla.getGroup(),
                vanilla.category(),
                vanilla.getResultItem(null),
                vanilla.getIngredients()
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, BannerPatternRecipe recipe) {

        // Just delegate to shapeless serializer
        RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buf, recipe);
    }
}