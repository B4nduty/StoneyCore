package banduty.stoneycore.datagen;

import banduty.stoneycore.recipes.CraftmanAnvilRecipe;
import banduty.stoneycore.recipes.StackIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CraftmanAnvilRecipeJsonBuilder implements RecipeBuilder {
    private final ItemStack result;

    private final List<StackIngredient> ingredients = new ArrayList<>();

    @Nullable
    private List<String> pattern;
    private final Map<String, StackIngredient> key = new LinkedHashMap<>();

    private int hitTimes = 3;
    private float chance = 1.0f;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    private CraftmanAnvilRecipeJsonBuilder(ItemStack result) {
        this.result = result;
    }

    public static CraftmanAnvilRecipeJsonBuilder create(ItemStack result) {
        return new CraftmanAnvilRecipeJsonBuilder(result);
    }

    public CraftmanAnvilRecipeJsonBuilder requires(ItemStack stack) {
        this.ingredients.add(new StackIngredient(stack, Optional.empty()));
        return this;
    }

    public CraftmanAnvilRecipeJsonBuilder requires(TagKey<Item> tag, int count) {
        this.ingredients.add(new StackIngredient(ItemStack.EMPTY, Optional.of(tag)));
        return this;
    }

    public CraftmanAnvilRecipeJsonBuilder requiresFinished(ItemStack stack) {
        this.ingredients.add(new StackIngredient(stack, Optional.empty(), StackIngredient.FinishedRequirement.FINISHED));
        return this;
    }

    public CraftmanAnvilRecipeJsonBuilder requiresUnfinished(ItemStack stack) {
        this.ingredients.add(new StackIngredient(stack, Optional.empty(), StackIngredient.FinishedRequirement.NOT_FINISHED));
        return this;
    }

    public CraftmanAnvilRecipeJsonBuilder pattern(String... rows) {
        this.pattern = List.of(rows);
        return this;
    }

    public CraftmanAnvilRecipeJsonBuilder define(char symbol, ItemStack stack) {
        this.key.put(String.valueOf(symbol), new StackIngredient(stack, Optional.empty()));
        return this;
    }

    public CraftmanAnvilRecipeJsonBuilder defineFinished(char symbol, ItemStack stack) {
        this.key.put(String.valueOf(symbol), new StackIngredient(stack, Optional.empty(), StackIngredient.FinishedRequirement.FINISHED));
        return this;
    }

    public CraftmanAnvilRecipeJsonBuilder defineUnfinished(char symbol, ItemStack stack) {
        this.key.put(String.valueOf(symbol), new StackIngredient(stack, Optional.empty(), StackIngredient.FinishedRequirement.NOT_FINISHED));
        return this;
    }

    public CraftmanAnvilRecipeJsonBuilder define(char symbol, Item item) {
        return define(symbol, new ItemStack(item));
    }

    public CraftmanAnvilRecipeJsonBuilder define(char symbol, TagKey<Item> tag) {
        this.key.put(String.valueOf(symbol), new StackIngredient(ItemStack.EMPTY, Optional.of(tag)));
        return this;
    }

    public CraftmanAnvilRecipeJsonBuilder hitTimes(int times) {
        this.hitTimes = times;
        return this;
    }

    public CraftmanAnvilRecipeJsonBuilder chance(float c) {
        this.chance = c;
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;
        return this;
    }

    @Override
    public Item getResult() {
        return result.getItem();
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        if (this.pattern != null && !this.ingredients.isEmpty()) {
            throw new IllegalStateException("Craftman's Anvil recipe " + id + " mixes pattern(...) with requires(...) - use one or the other");
        }

        Advancement.Builder advancementBuilder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        this.criteria.forEach(advancementBuilder::addCriterion);

        CraftmanAnvilRecipe recipe;

        if (this.pattern != null) {
            CraftmanAnvilRecipe.ShapedPattern shapedPattern = CraftmanAnvilRecipe.createPattern(this.pattern, this.key);
            recipe = new CraftmanAnvilRecipe(Optional.of(shapedPattern), List.of(), this.result, this.hitTimes, this.chance);
        } else {
            recipe = new CraftmanAnvilRecipe(Optional.empty(), List.copyOf(this.ingredients), this.result, this.hitTimes, this.chance);
        }

        recipeOutput.accept(id, recipe, advancementBuilder.build(id.withPrefix("recipes/")));
    }
}