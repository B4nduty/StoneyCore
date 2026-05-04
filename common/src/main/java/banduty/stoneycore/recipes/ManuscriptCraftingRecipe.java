package banduty.stoneycore.recipes;

import banduty.stoneycore.items.custom.SmithingHammer;
import banduty.stoneycore.items.custom.manuscript.Manuscript;
import banduty.stoneycore.mixin.ShapelessRecipeAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

public class ManuscriptCraftingRecipe extends ShapelessRecipe {

    public ManuscriptCraftingRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SCRecipes.MANUSCRIPT_SERIALIZER;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack itemInput = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (!stack.isEmpty()
                    && !(stack.getItem() instanceof SmithingHammer)
                    && !stack.is(net.minecraft.world.item.Items.PAPER)) {
                itemInput = stack;
                break;
            }
        }

        return Manuscript.createForStack(itemInput);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (stack.getItem() instanceof SmithingHammer) {
                ItemStack hammerCopy = stack.copy();
                int dmg = hammerCopy.getDamageValue() + 1;
                if (dmg < hammerCopy.getMaxDamage()) {
                    hammerCopy.setDamageValue(dmg);
                    remaining.set(i, hammerCopy);
                }
            }
        }

        return remaining;
    }

    public static class Serializer implements RecipeSerializer<ManuscriptCraftingRecipe> {
        public static final MapCodec<ManuscriptCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> ((ShapelessRecipeAccessor) recipe).stoneycore$getResult()),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").xmap(list -> {
                    NonNullList<Ingredient> nonnulllist = NonNullList.create();
                    nonnulllist.addAll(list);
                    return nonnulllist;
                }, list -> list).forGetter(ShapelessRecipe::getIngredients)
        ).apply(inst, ManuscriptCraftingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ManuscriptCraftingRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public MapCodec<ManuscriptCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ManuscriptCraftingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ManuscriptCraftingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            int ingredientCount = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientCount, Ingredient.EMPTY);
            for (int j = 0; j < ingredientCount; j++) {
                ingredients.set(j, Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            return new ManuscriptCraftingRecipe(group, category, result, ingredients);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ManuscriptCraftingRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            buffer.writeVarInt(recipe.getIngredients().size());
            for (Ingredient ingredient : recipe.getIngredients()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(buffer, ((ShapelessRecipeAccessor) recipe).stoneycore$getResult());
        }
    }
}