package banduty.stoneycore.recipes;

import banduty.stoneycore.items.custom.hotiron.HotIron;
import banduty.stoneycore.items.custom.manuscript.Manuscript;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public record CraftmanAnvilRecipe(List<StackIngredient> ingredients, ItemStack output, int hitTimes,
                                  float chance) implements Recipe<AnvilInput> {

    @Override
    public boolean matches(AnvilInput input, Level level) {
        List<ItemStack> remaining = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (stack.getItem() instanceof HotIron) {
                ItemStack target = HotIron.getTargetStack(stack);

                if (!target.isEmpty()) {
                    return false;
                }
            }

            if (!stack.isEmpty()) {
                remaining.add(stack.copy());
            }
        }

        if (remaining.size() != ingredients.size()) {
            return false;
        }

        for (StackIngredient ingredient : ingredients) {
            boolean found = false;

            for (int i = 0; i < remaining.size(); i++) {
                ItemStack stack = remaining.get(i);

                if (ingredient.test(stack)) {
                    remaining.remove(i);
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(AnvilInput input) {
        NonNullList<ItemStack> remainingItems = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        // Vanilla remainders
        NonNullList<ItemStack> vanillaRemaining = Recipe.super.getRemainingItems(input);

        for (int i = 0; i < input.size(); i++) {
            ItemStack remainder = vanillaRemaining.get(i);

            if (!remainder.isEmpty()) {
                remainingItems.set(i, remainder.copy());
            }

            // Restore manuscript from input slot
            ItemStack stack = input.getItem(i);

            if (!stack.isEmpty() && stack.getItem() instanceof Manuscript) {
                remainingItems.set(i, stack.copy());
            }
        }

        return remainingItems;
    }

    @Override
    public ItemStack assemble(AnvilInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SCRecipes.CRAFTMAN_ANVIL_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return SCRecipes.CRAFTMAN_ANVIL_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<CraftmanAnvilRecipe> {
        private static final MapCodec<CraftmanAnvilRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                StackIngredient.CODEC.codec().listOf().fieldOf("ingredients").forGetter(CraftmanAnvilRecipe::ingredients),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CraftmanAnvilRecipe::output),
                Codec.INT.optionalFieldOf("hit_times", 3).forGetter(CraftmanAnvilRecipe::hitTimes),
                Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(CraftmanAnvilRecipe::chance)
        ).apply(inst, CraftmanAnvilRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, CraftmanAnvilRecipe> STREAM_CODEC = StreamCodec.composite(
                StackIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), CraftmanAnvilRecipe::ingredients,
                ItemStack.STREAM_CODEC, CraftmanAnvilRecipe::output,
                ByteBufCodecs.VAR_INT, CraftmanAnvilRecipe::hitTimes,
                ByteBufCodecs.FLOAT, CraftmanAnvilRecipe::chance,
                CraftmanAnvilRecipe::new
        );

        @Override
        public MapCodec<CraftmanAnvilRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CraftmanAnvilRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}