package banduty.stoneycore.compat.rei;

import banduty.stoneycore.items.custom.manuscript.ManuscriptType;
import banduty.stoneycore.recipes.CraftmanAnvilRecipe;
import banduty.stoneycore.recipes.StackIngredient;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CraftmanAnvilDisplayREI extends BasicDisplay {
    private final int hitTimes;
    private final float chance;
    protected List<EntryIngredient> realOutput;

    public CraftmanAnvilDisplayREI(List<EntryIngredient> inputs, List<EntryIngredient> outputs, int hitTimes, float chance) {
        super(inputs, outputs);
        this.hitTimes = hitTimes;
        this.chance = chance;
    }

    public CraftmanAnvilDisplayREI(RecipeHolder<CraftmanAnvilRecipe> recipeHolder) {
        this(recipeHolder.value());
    }

    private CraftmanAnvilDisplayREI(CraftmanAnvilRecipe recipe) {
        super(getInputList(recipe), List.of(EntryIngredient.of(EntryStacks.of(recipe.output()))));

        this.hitTimes = recipe.hitTimes();
        this.chance = recipe.chance();

        EntryStack<?> entry = this.outputs.getFirst().getFirst();
        ItemStack stack = entry.castValue();

        ItemStack output = recipe.output();
        ManuscriptType type = ManuscriptType.getManuscriptType(output);

        if (type != null && type.getHotIronItem() != null && type.getHotIronItem() != Items.AIR) {
            ItemStack target = new ItemStack(type.getHotIronItem());

            if (!target.isEmpty()) {
                this.outputs = List.of(
                        EntryIngredient.of(EntryStacks.of(target.copy()))
                );
                this.realOutput = List.of(
                        EntryIngredient.of(EntryStacks.of(stack.copy()))
                );
            }
        }
    }

    public List<EntryIngredient> getRealOutput() {
        return realOutput;
    }

    private static List<EntryIngredient> getInputList(CraftmanAnvilRecipe recipe) {
        if (recipe == null) return Collections.emptyList();
        List<EntryIngredient> list = new ArrayList<>();

        List<StackIngredient> ingredients = recipe.pattern()
                .map(p -> p.slots().stream()
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList())
                .orElse(recipe.ingredients());

        for (StackIngredient ingredient : ingredients) {
            if (ingredient.tag().isPresent()) {
                list.add(EntryIngredients.ofItemTag(ingredient.tag().get()));
            } else {
                list.add(EntryIngredients.of(ingredient.stack()));
            }
        }
        return list;
    }

    public int getHitTimes() {
        return hitTimes;
    }

    public float getChance() {
        return chance;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return CraftmanAnvilCategoryREI.CRAFTMAN_ANVIL;
    }
}
