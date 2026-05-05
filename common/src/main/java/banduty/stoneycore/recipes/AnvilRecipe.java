package banduty.stoneycore.recipes;

import banduty.stoneycore.block.CraftmanAnvilBlockEntity;
import banduty.stoneycore.items.hotiron.HotIron;
import banduty.stoneycore.platform.Services;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record AnvilRecipe(ResourceLocation id, List<StackIngredient> ingredients, ItemStack output, int hitTimes,
                          float chance) implements Recipe<CraftmanAnvilBlockEntity> {

    @Override
    public boolean matches(CraftmanAnvilBlockEntity inventory, Level level) {
        for (int i = 0; i < 6; i++) {
            ItemStack stack = inventory.getItem(i);

            if (!stack.isEmpty() && stack.getItem() instanceof HotIron) {
                ItemStack target = HotIron.getTargetStack(stack);

                if (!target.isEmpty()) {
                    return false;
                }
            }
        }

        List<ItemStack> inputItems = new ArrayList<>();
        for (int i = 0; i < 6; i++) inputItems.add(inventory.getItem(i));

        List<ItemStack> remainingInputs = new ArrayList<>();
        for (ItemStack stack : inputItems) {
            if (!stack.isEmpty()) {
                remainingInputs.add(stack.copy());
            }
        }

        for (StackIngredient ingredient : ingredients) {
            int neededCount = ingredient.stack().getCount();

            for (int i = 0; i < remainingInputs.size() && neededCount > 0; i++) {
                ItemStack inputStack = remainingInputs.get(i);

                if (ingredient.test(inputStack)) {
                    int available = inputStack.getCount();
                    int toTake = Math.min(available, neededCount);

                    inputStack.shrink(toTake);
                    neededCount -= toTake;

                    if (inputStack.getCount() == 0) {
                        remainingInputs.remove(i);
                        i--;
                    }
                }
            }

            if (neededCount > 0) {
                return false;
            }
        }

        for (ItemStack remaining : remainingInputs) {
            if (!remaining.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack assemble(CraftmanAnvilBlockEntity inventory, RegistryAccess registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return output;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Services.PLATFORM.getCraftmanAnvilRecipeSerializer();
    }

    @Override
    public RecipeType<?> getType() {
        return Services.PLATFORM.getCraftmanAnvilRecipe();
    }

    public static class Serializer implements RecipeSerializer<AnvilRecipe> {
        public static final Serializer INSTANCE = new Serializer();

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
}