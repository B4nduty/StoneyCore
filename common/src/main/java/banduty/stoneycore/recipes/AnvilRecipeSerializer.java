package banduty.stoneycore.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AnvilRecipeSerializer implements RecipeSerializer<AnvilRecipe> {
    public static final AnvilRecipeSerializer INSTANCE = new AnvilRecipeSerializer();

    @Override
    public @NotNull AnvilRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
        JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
        List<StackIngredient> ingredients = new ArrayList<>();

        for (JsonElement el : array) {
            ingredients.add(StackIngredient.fromJson(el.getAsJsonObject()));
        }

        JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result");
        ItemStack output = readOutputStack(resultJson);

        int hitTimes = GsonHelper.getAsInt(json, "hit_times", 3);
        float chance = GsonHelper.getAsFloat(json, "chance", 1.0f);

        return new AnvilRecipe(id, ingredients, output, hitTimes, chance);
    }

    private ItemStack readOutputStack(JsonObject json) {
        String itemId = GsonHelper.getAsString(json, "item");
        int count = GsonHelper.getAsInt(json, "count", 1);

        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(itemId)), count);

        if (json.has("nbt")) {
            try {
                CompoundTag tag = TagParser.parseTag(GsonHelper.getAsString(json, "nbt"));
                stack.setTag(tag);
            } catch (Exception e) {
                throw new RuntimeException("Invalid NBT data for output: " + json, e);
            }
        }

        return stack;
    }

    @Override
    public @NotNull AnvilRecipe fromNetwork(@NotNull ResourceLocation id, FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<StackIngredient> ingredients = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            ingredients.add(StackIngredient.read(buf));
        }

        ItemStack output = buf.readItem();
        int hitTimes = buf.readVarInt();
        float chance = buf.readFloat();

        return new AnvilRecipe(id, ingredients, output, hitTimes, chance);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, AnvilRecipe recipe) {
        buf.writeVarInt(recipe.ingredients().size());
        for (StackIngredient ing : recipe.ingredients()) ing.write(buf);

        buf.writeItem(recipe.output());
        buf.writeVarInt(recipe.hitTimes());
        buf.writeFloat(recipe.chance());
    }
}
