package banduty.stoneycore.compat.emi;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.block.SCBlocks;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class CraftmanAnvilCategoryEMI {
    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "craftman_anvil");
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "textures/gui/craftman_anvil_gui.png");

    public static final EmiStack WORKSTATION =
            EmiStack.of(new ItemStack(SCBlocks.CRAFTMAN_ANVIL.get().asItem()));

    public static final EmiRecipeCategory CATEGORY = new EmiRecipeCategory(
            UID,
            WORKSTATION,
            new EmiTexture(TEXTURE, 0, 0, 16, 16)
    );

    private CraftmanAnvilCategoryEMI() {
    }
}