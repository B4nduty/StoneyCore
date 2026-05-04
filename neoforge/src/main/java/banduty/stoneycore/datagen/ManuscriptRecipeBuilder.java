package banduty.stoneycore.datagen;

import banduty.stoneycore.recipes.ManuscriptCraftingRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class ManuscriptRecipeBuilder implements RecipeBuilder {
    private final Item result;
    private final NonNullList<Ingredient> ingredients;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private String group = "";
    private CraftingBookCategory category = CraftingBookCategory.MISC;

    public ManuscriptRecipeBuilder(Item result, NonNullList<Ingredient> ingredients) {
        this.result = result;
        this.ingredients = ingredients;
    }

    public static ManuscriptRecipeBuilder create(Item result, NonNullList<Ingredient> ingredients) {
        return new ManuscriptRecipeBuilder(result, ingredients);
    }

    @Override
    public ManuscriptRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public ManuscriptRecipeBuilder group(@Nullable String groupName) {
        this.group = groupName == null ? "" : groupName;
        return this;
    }

    public ManuscriptRecipeBuilder category(CraftingBookCategory category) {
        this.category = category;
        return this;
    }

    @Override
    public Item getResult() {
        return result;
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        Advancement.Builder advancementBuilder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        this.criteria.forEach(advancementBuilder::addCriterion);

        ManuscriptCraftingRecipe recipe = new ManuscriptCraftingRecipe(
                this.group,
                this.category,
                new ItemStack(this.result),
                this.ingredients
        );

        recipeOutput.accept(id, recipe, advancementBuilder.build(id.withPrefix("recipes/")));
    }
}