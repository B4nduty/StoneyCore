package banduty.stoneycore.recipes;

import banduty.stoneycore.mixin.ShapelessRecipeAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class BannerPatternRecipe extends ShapelessRecipe {
    public BannerPatternRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(group, category, result, ingredients);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SCRecipes.BANNER_SERIALIZER.get();
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!super.matches(input, level)) {
            return false;
        }

        boolean hasBanner = false;
        boolean hasArmor = false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof BannerItem) {
                hasBanner = true;
            } else {
                hasArmor = true;
            }
        }

        return hasBanner && hasArmor;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack banner = ItemStack.EMPTY;
        ItemStack otherInput = ItemStack.EMPTY;
        DyeColor dye = DyeColor.WHITE;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof BannerItem bannerItem) {
                dye = bannerItem.getColor();
                banner = stack;
            } else {
                otherInput = stack;
            }
        }

        if (banner.isEmpty() || otherInput.isEmpty()) return ItemStack.EMPTY;

        ItemStack result = otherInput.copyWithCount(1);

        BannerPatternLayers patterns =
                banner.getOrDefault(
                        DataComponents.BANNER_PATTERNS,
                        BannerPatternLayers.EMPTY
                );
        result.set(DataComponents.BANNER_PATTERNS, patterns);

        result.set(DataComponents.DYED_COLOR, new DyedItemColor(dye.getTextColor(), false));

        return result;
    }

    public static class Serializer implements RecipeSerializer<BannerPatternRecipe> {
        private static final MapCodec<BannerPatternRecipe> CODEC = RecordCodecBuilder.mapCodec((inst) -> inst.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> {
                    ItemStack result = ((ShapelessRecipeAccessor) recipe).stoneycore$getResult().copy();
                    ItemStack banner = ItemStack.EMPTY;
                    ItemStack otherInput = ItemStack.EMPTY;
                    DyeColor dye = DyeColor.WHITE;

                    for (Ingredient ing : recipe.getIngredients()) {
                        ItemStack[] stacks = ing.getItems();
                        for (ItemStack stack : stacks) {
                            if (stack.isEmpty()) continue;

                            if (stack.getItem() instanceof BannerItem bannerItem) {
                                dye = bannerItem.getColor();
                                banner = stack;
                            } else {
                                otherInput = stack;
                            }
                        }
                    }

                    if (banner.isEmpty() || otherInput.isEmpty())
                        result = ((ShapelessRecipeAccessor) recipe).stoneycore$getResult();
                    else {
                        BannerPatternLayers patterns =
                                banner.getOrDefault(
                                        DataComponents.BANNER_PATTERNS,
                                        BannerPatternLayers.EMPTY
                                );
                        result.set(DataComponents.BANNER_PATTERNS, patterns);

                        result.set(DataComponents.DYED_COLOR, new DyedItemColor(dye.getTextColor(), false));
                    }

                    return result;
                }),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").xmap(list -> {
                    NonNullList<Ingredient> nonnulllist = NonNullList.create();
                    nonnulllist.addAll(list);
                    return nonnulllist;
                }, list -> list).forGetter(ShapelessRecipe::getIngredients)
        ).apply(inst, BannerPatternRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, BannerPatternRecipe> STREAM_CODEC = StreamCodec.of(
                Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public MapCodec<BannerPatternRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BannerPatternRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static BannerPatternRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String s = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            int i = buffer.readVarInt();
            NonNullList<Ingredient> ingredients = NonNullList.withSize(i, Ingredient.EMPTY);
            for (int j = 0; j < i; j++) {
                ingredients.set(j, Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
            }
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);

            ItemStack banner = ItemStack.EMPTY;
            ItemStack otherInput = ItemStack.EMPTY;
            DyeColor dye = DyeColor.WHITE;

            for (Ingredient ing : ingredients) {
                for (ItemStack stack : ing.getItems()) {
                    if (stack.isEmpty()) continue;

                    if (stack.getItem() instanceof BannerItem bannerItem) {
                        dye = bannerItem.getColor();
                        banner = stack;
                    } else {
                        otherInput = stack;
                    }
                }
            }

            if (!banner.isEmpty() && !otherInput.isEmpty()) {
                BannerPatternLayers patterns =
                        banner.getOrDefault(
                                DataComponents.BANNER_PATTERNS,
                                BannerPatternLayers.EMPTY
                        );
                result.set(DataComponents.BANNER_PATTERNS, patterns);

                result.set(
                        DataComponents.DYED_COLOR,
                        new DyedItemColor(dye.getTextColor(), false)
                );
            }
            return new BannerPatternRecipe(s, category, result, ingredients);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, BannerPatternRecipe recipe) {
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