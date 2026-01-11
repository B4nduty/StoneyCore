package banduty.stoneycore.compat.rei;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CraftmanAnvilCategoryREI implements DisplayCategory<BasicDisplay> {
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(StoneyCore.MOD_ID, "textures/gui/craftman_anvil_gui.png");
    public static final CategoryIdentifier<CraftmanAnvilDisplayREI> CRAFTMAN_ANVIL =
            CategoryIdentifier.of(StoneyCore.MOD_ID, "craftman_anvil");

    @Override
    public CategoryIdentifier<? extends BasicDisplay> getCategoryIdentifier() {
        return CRAFTMAN_ANVIL;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Craftsman's Anvil");
    }

    @Override
    public Renderer getIcon() {
        return EntryStacks.of(Services.PLATFORM.getCraftmanAnvil().asItem().getDefaultInstance());
    }

    @Override
    public List<Widget> setupDisplay(BasicDisplay display, Rectangle bounds) {
        if (!(display instanceof CraftmanAnvilDisplayREI craftmanAnvilDisplayREI)) return new ArrayList<>();
        final Point startPoint = new Point(bounds.getCenterX() - 87, bounds.getCenterY() - 35);
        int inputSize = Math.min(display.getInputEntries().size(), 6);
        List<Widget> widgets = new LinkedList<>();
        widgets.add(Widgets.createTexturedWidget(TEXTURE, new Rectangle(startPoint.x, startPoint.y, 175, 82)));

        // Input slots 2x3 grid
        int[] inputSlotsX = {54, 36, 72, 54, 36, 72};
        int[] inputSlotsY = {29, 29, 29, 11, 11, 11};

        // Input slots
        for (int i = 0; i < inputSize; i++) {
            int addX = 0;
            int addY = 0;

            if (inputSize <= 3) addY = -9;

            if (inputSize == 1 || inputSize == 2) {
                addX = 18;
            } else if (inputSize == 5) {
                if (i == 3 || i == 4) addX = 9;
            }

            Widget slot = Widgets.createSlot(new Point(startPoint.x + inputSlotsX[i] + addX, startPoint.y + inputSlotsY[i] + addY))
                    .entries(display.getInputEntries().get(i));
            widgets.add(slot);
        }

        // Output slot
        if (!craftmanAnvilDisplayREI.getRealOutput().isEmpty() && !craftmanAnvilDisplayREI.getRealOutput().get(0).isEmpty()) {
            ItemStack target = craftmanAnvilDisplayREI.getOutputEntries().get(0).get(0).castValue();
            widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
                PoseStack poseStack = graphics.pose();
                poseStack.pushPose();
                poseStack.translate(startPoint.x + 128, startPoint.y + 28, 0);

                float scale = 0.5f;
                poseStack.scale(scale, scale, scale);

                graphics.renderItem(target, 0, 0);
                graphics.renderItemDecorations(Minecraft.getInstance().font, target, 0, 0);

                poseStack.popPose();
            }));
        }

        if (!display.getOutputEntries().isEmpty()) {
            EntryIngredient entryIngredient = display.getOutputEntries().get(0);
            if (!craftmanAnvilDisplayREI.getRealOutput().isEmpty() && !craftmanAnvilDisplayREI.getRealOutput().get(0).isEmpty())
                entryIngredient = craftmanAnvilDisplayREI.getRealOutput().get(0);
            Widget outputSlot = Widgets.createSlot(new Point(startPoint.x + 120, startPoint.y + 20))
                    .markOutput()
                    .entries(entryIngredient);
            widgets.add(outputSlot);
        }

        if (display instanceof CraftmanAnvilDisplayREI anvilDisplay) {
            // Hit times
            Widget hitLabel = Widgets.createLabel(new Point(startPoint.x + 10, startPoint.y + 42),
                            Component.literal("Hits: " + anvilDisplay.getHitTimes()))
                    .leftAligned();
            widgets.add(hitLabel);

            // Chance
            float chance = anvilDisplay.getChance();
            if (chance < 1.0f) {
                Widget chanceLabel = Widgets.createLabel(new Point(startPoint.x + 90, startPoint.y + 42),
                                Component.literal(String.format("Chance: %.1f%%", chance * 100)))
                        .leftAligned();
                widgets.add(chanceLabel);
            }
        }

        return widgets;
    }

    @Override
    public int getDisplayHeight() {
        return 90;
    }
}
