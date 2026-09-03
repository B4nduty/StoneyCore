package banduty.stoneycore.compat.emi;

import banduty.stoneycore.recipes.CraftmanAnvilRecipe;
import banduty.stoneycore.recipes.StackIngredient;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftmanAnvilRecipeEMI implements EmiRecipe {
    private final CraftmanAnvilRecipe recipe;
    private final ResourceLocation id;
    private final List<EmiIngredient> inputs;
    private final List<EmiStack> outputs;

    public CraftmanAnvilRecipeEMI(RecipeHolder<CraftmanAnvilRecipe> holder) {
        this.recipe = holder.value();
        this.id = holder.id();
        this.inputs = buildInputs(recipe);
        this.outputs = List.of(EmiStack.of(recipe.output()));
    }

    private static List<EmiIngredient> buildInputs(CraftmanAnvilRecipe recipe) {
        List<EmiIngredient> list = new ArrayList<>();

        if (recipe.pattern().isPresent()) {
            for (Optional<StackIngredient> slot : recipe.pattern().get().slots()) {
                slot.ifPresent(ingredient -> list.add(toEmiIngredient(ingredient)));
            }
        } else {
            for (StackIngredient ingredient : recipe.ingredients()) {
                list.add(toEmiIngredient(ingredient));
            }
        }

        return list;
    }

    private static EmiIngredient toEmiIngredient(StackIngredient ingredient) {
        List<EmiStack> stacks = ingredient.asItemStacks().stream()
                .map(EmiStack::of)
                .toList();

        return EmiIngredient.of(stacks);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return CraftmanAnvilCategoryEMI.CATEGORY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 175;
    }

    @Override
    public int getDisplayHeight() {
        return 82;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(CraftmanAnvilCategoryEMI.TEXTURE, 0, 0, 175, 82, 0, 0);

        int[] inputSlotsX = {36, 54, 72, 36, 54, 72};
        int[] inputSlotsY = {11, 11, 11, 29, 29, 29};

        if (recipe.pattern().isPresent()) {
            List<Optional<StackIngredient>> slots = recipe.pattern().get().slots();
            int inputIndex = 0;

            for (int i = 0; i < slots.size(); i++) {
                if (slots.get(i).isEmpty()) continue;

                widgets.addSlot(inputs.get(inputIndex), inputSlotsX[i], inputSlotsY[i]);
                inputIndex++;
            }
        } else {
            int inputSize = Math.min(inputs.size(), 6);

            for (int i = 0; i < inputSize; i++) {
                int addX = 0;
                int addY = 0;

                if (inputSize <= 3) addY = -9;
                if (inputSize == 1 || inputSize == 2) addX = 18;
                else if (inputSize == 5 && (i == 3 || i == 4)) addX = 9;

                widgets.addSlot(inputs.get(i), inputSlotsX[i] + addX, inputSlotsY[i] + addY);
            }
        }

        widgets.addSlot(outputs.get(0), 120, 20).recipeContext(this);

        // Hits / chance overlay text, same as your JEI draw() and REI labels
        widgets.add(new Widget() {
            @Override
            public Bounds getBounds() {
                return new Bounds(0, 0, 175, 82);
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
                graphics.drawString(Minecraft.getInstance().font,
                        "Hits: " + recipe.hitTimes(), 10, 42, 0xFFFFFFFF, true);

                if (recipe.chance() < 1f) {
                    graphics.drawString(Minecraft.getInstance().font,
                            String.format("Chance: %.1f%%", recipe.chance() * 100), 90, 42, 0xFFFFFFFF, true);
                }
            }
        });
    }
}