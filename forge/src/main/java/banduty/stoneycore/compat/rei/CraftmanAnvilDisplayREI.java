package banduty.stoneycore.compat.rei;

import banduty.stoneycore.items.hotiron.HotIron;
import banduty.stoneycore.items.manuscript.Manuscript;
import banduty.stoneycore.smithing.AnvilRecipe;
import banduty.stoneycore.smithing.StackIngredient;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CraftmanAnvilDisplayREI extends BasicDisplay {
    private final int hitTimes;
    private final float chance;
    protected List<EntryIngredient> realOutput;

    public CraftmanAnvilDisplayREI(List<EntryIngredient> inputs, List<EntryIngredient> outputs, int hitTimes, float chance) {
        super(inputs, outputs);
        this.hitTimes = hitTimes;
        this.chance = chance;
    }

    public CraftmanAnvilDisplayREI(AnvilRecipe recipe) {
        super(getInputList(recipe), List.of(EntryIngredient.of(EntryStacks.of(recipe.output()))));
        this.hitTimes = recipe.hitTimes();
        this.chance = recipe.chance();
        EntryStack<?> entry = this.outputs.get(0).get(0);

        ItemStack stack = entry.castValue();

        ItemStack target = HotIron.getTargetStack(stack);
        if (target.isEmpty()) target = Manuscript.getTargetStack(stack);

        if (!target.isEmpty()) {
            this.outputs = List.of(
                    EntryIngredient.of(EntryStacks.of(target.copy()))
            );
            this.realOutput = List.of(
                    EntryIngredient.of(EntryStacks.of(stack.copy()))
            );
        }
    }

    public List<EntryIngredient> getRealOutput() {
        return realOutput;
    }

    private static List<EntryIngredient> getInputList(AnvilRecipe recipe) {
        if (recipe == null) return Collections.emptyList();

        List<EntryIngredient> list = new ArrayList<>();
        for (StackIngredient ingredient : recipe.ingredients()) {
            if (ingredient.tag() != null) {
                list.add(EntryIngredients.ofItemTag(ingredient.tag()));
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
