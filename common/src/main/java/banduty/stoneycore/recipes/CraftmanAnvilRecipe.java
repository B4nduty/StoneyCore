package banduty.stoneycore.recipes;

import banduty.stoneycore.items.custom.manuscript.Manuscript;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
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

import java.util.*;
import java.util.stream.Stream;

public record CraftmanAnvilRecipe(Optional<ShapedPattern> pattern, List<StackIngredient> ingredients,
                                  ItemStack output, int hitTimes, float chance) implements Recipe<AnvilInput> {

    public static final int GRID_WIDTH = 3;
    public static final int GRID_HEIGHT = 2;
    public static final int GRID_SIZE = GRID_WIDTH * GRID_HEIGHT;

    @Override
    public boolean matches(AnvilInput input, Level level) {
        return pattern.map(shapedPattern -> matchesShaped(input, shapedPattern)).orElseGet(() -> matchesShapeless(input));
    }

    private boolean matchesShaped(AnvilInput input, ShapedPattern shapedPattern) {
        List<Optional<StackIngredient>> slots = shapedPattern.slots();

        for (int i = 0; i < GRID_SIZE; i++) {
            ItemStack stack = input.getItem(i);
            Optional<StackIngredient> expected = slots.get(i);

            if (expected.isEmpty()) {
                if (!stack.isEmpty()) {
                    return false;
                }
            } else if (!expected.get().test(stack)) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesShapeless(AnvilInput input) {
        List<ItemStack> remaining = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

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

        NonNullList<ItemStack> vanillaRemaining = Recipe.super.getRemainingItems(input);

        for (int i = 0; i < input.size(); i++) {
            ItemStack remainder = vanillaRemaining.get(i);

            if (!remainder.isEmpty()) {
                remainingItems.set(i, remainder.copy());
            }

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

    public record ShapedPattern(List<Optional<StackIngredient>> slots) {
        public ShapedPattern {
            if (slots.size() != GRID_SIZE) {
                throw new IllegalArgumentException("ShapedPattern must have exactly " + GRID_SIZE + " slots");
            }
        }
    }

    private record PatternRaw(List<String> pattern, Map<String, StackIngredient> key) {
    }

    public static ShapedPattern createPattern(List<String> patternRows, Map<String, StackIngredient> key) {
        DataResult<ShapedPattern> result = fromRaw(new PatternRaw(List.copyOf(patternRows), Map.copyOf(key)));

        return result.result().orElseThrow(() -> new IllegalArgumentException(
                result.error().map(DataResult.Error::message).orElse("Invalid Craftman's Anvil pattern")));
    }

    private static DataResult<ShapedPattern> fromRaw(PatternRaw raw) {
        List<String> rows = raw.pattern();

        if (rows.isEmpty() || rows.size() > GRID_HEIGHT) {
            return DataResult.error(() -> "Pattern must have between 1 and " + GRID_HEIGHT + " rows, found " + rows.size());
        }

        List<Optional<StackIngredient>> slots = new ArrayList<>(Collections.nCopies(GRID_SIZE, Optional.empty()));

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            String row = rows.get(rowIndex);

            if (row.length() > GRID_WIDTH) {
                int finalRowIndex = rowIndex;
                return DataResult.error(() -> "Pattern row \"" + row + "\" at index " + finalRowIndex + " must be at most " + GRID_WIDTH + " characters wide");
            }

            for (int col = 0; col < row.length(); col++) {
                char symbol = row.charAt(col);

                if (symbol == ' ') {
                    continue;
                }

                StackIngredient ingredient = raw.key().get(String.valueOf(symbol));

                if (ingredient == null) {
                    return DataResult.error(() -> "Pattern references unknown key '" + symbol + "'");
                }

                slots.set(rowIndex * GRID_WIDTH + col, Optional.of(ingredient));
            }
        }

        return DataResult.success(new ShapedPattern(slots));
    }

    private static PatternRaw toRaw(ShapedPattern shaped) {
        Map<String, StackIngredient> key = new LinkedHashMap<>();
        Map<StackIngredient, Character> assigned = new HashMap<>();
        List<String> pattern = new ArrayList<>();
        char nextSymbol = 'A';

        StringBuilder currentRow = new StringBuilder();
        List<Optional<StackIngredient>> slots = shaped.slots();

        for (int i = 0; i < slots.size(); i++) {
            Optional<StackIngredient> slot = slots.get(i);

            if (slot.isEmpty()) {
                currentRow.append(' ');
            } else {
                StackIngredient ingredient = slot.get();
                Character symbol = assigned.get(ingredient);

                if (symbol == null) {
                    symbol = nextSymbol++;
                    assigned.put(ingredient, symbol);
                    key.put(String.valueOf(symbol), ingredient);
                }

                currentRow.append(symbol.charValue());
            }

            if ((i + 1) % GRID_WIDTH == 0) {
                pattern.add(currentRow.toString());
                currentRow = new StringBuilder();
            }
        }

        return new PatternRaw(pattern, key);
    }

    private static final MapCodec<PatternRaw> PATTERN_RAW_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.listOf().fieldOf("pattern").forGetter(PatternRaw::pattern),
            Codec.unboundedMap(Codec.STRING, StackIngredient.CODEC.codec()).fieldOf("key").forGetter(PatternRaw::key)
    ).apply(inst, PatternRaw::new));

    private static final MapCodec<ShapedPattern> SHAPED_PATTERN_CODEC = PATTERN_RAW_CODEC.flatXmap(
            CraftmanAnvilRecipe::fromRaw,
            shaped -> DataResult.success(CraftmanAnvilRecipe.toRaw(shaped))
    );

    private static final MapCodec<CraftmanAnvilRecipe> SHAPED_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SHAPED_PATTERN_CODEC.forGetter(recipe -> recipe.pattern().orElseThrow()),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CraftmanAnvilRecipe::output),
            Codec.INT.optionalFieldOf("hit_times", 3).forGetter(CraftmanAnvilRecipe::hitTimes),
            Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(CraftmanAnvilRecipe::chance)
    ).apply(inst, (shapedPattern, output, hitTimes, chance) ->
            new CraftmanAnvilRecipe(Optional.of(shapedPattern), List.of(), output, hitTimes, chance)));

    private static final MapCodec<CraftmanAnvilRecipe> SHAPELESS_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            StackIngredient.CODEC.codec().listOf().fieldOf("ingredients").forGetter(CraftmanAnvilRecipe::ingredients),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CraftmanAnvilRecipe::output),
            Codec.INT.optionalFieldOf("hit_times", 3).forGetter(CraftmanAnvilRecipe::hitTimes),
            Codec.FLOAT.optionalFieldOf("chance", 1.0f).forGetter(CraftmanAnvilRecipe::chance)
    ).apply(inst, (ingredients, output, hitTimes, chance) ->
            new CraftmanAnvilRecipe(Optional.empty(), ingredients, output, hitTimes, chance)));

    public static class Serializer implements RecipeSerializer<CraftmanAnvilRecipe> {
        private static final MapCodec<CraftmanAnvilRecipe> CODEC = new MapCodec<>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of("pattern", "key", "ingredients", "result", "hit_times", "chance")
                        .map(ops::createString);
            }

            @Override
            public <T> DataResult<CraftmanAnvilRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
                if (input.get("pattern") != null) {
                    return SHAPED_CODEC.decode(ops, input);
                }
                return SHAPELESS_CODEC.decode(ops, input);
            }

            @Override
            public <T> RecordBuilder<T> encode(CraftmanAnvilRecipe input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                if (input.pattern().isPresent()) {
                    return SHAPED_CODEC.encode(input, ops, prefix);
                }
                return SHAPELESS_CODEC.encode(input, ops, prefix);
            }
        };

        private static final StreamCodec<RegistryFriendlyByteBuf, CraftmanAnvilRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public CraftmanAnvilRecipe decode(RegistryFriendlyByteBuf buf) {
                boolean shaped = buf.readBoolean();

                Optional<ShapedPattern> pattern = Optional.empty();
                List<StackIngredient> ingredients = List.of();

                if (shaped) {
                    List<Optional<StackIngredient>> slots = new ArrayList<>(GRID_SIZE);
                    for (int i = 0; i < GRID_SIZE; i++) {
                        slots.add(ByteBufCodecs.optional(StackIngredient.STREAM_CODEC).decode(buf));
                    }
                    pattern = Optional.of(new ShapedPattern(slots));
                } else {
                    ingredients = StackIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                }

                ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
                int hitTimes = ByteBufCodecs.VAR_INT.decode(buf);
                float chance = ByteBufCodecs.FLOAT.decode(buf);

                return new CraftmanAnvilRecipe(pattern, ingredients, output, hitTimes, chance);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, CraftmanAnvilRecipe value) {
                buf.writeBoolean(value.pattern().isPresent());

                if (value.pattern().isPresent()) {
                    for (Optional<StackIngredient> slot : value.pattern().get().slots()) {
                        ByteBufCodecs.optional(StackIngredient.STREAM_CODEC).encode(buf, slot);
                    }
                } else {
                    StackIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, value.ingredients());
                }

                ItemStack.STREAM_CODEC.encode(buf, value.output());
                ByteBufCodecs.VAR_INT.encode(buf, value.hitTimes());
                ByteBufCodecs.FLOAT.encode(buf, value.chance());
            }
        };

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