package banduty.stoneycore.items.blueprint;

import banduty.stoneycore.screen.BlueprintScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public interface IBlueprintHelper {
    void openBlueprintScreen(ServerPlayer player, ItemStack stack, ResourceLocation structureId);

    MenuType<?> blueprintScreenHandler();

    void renderBlueprintEvents(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, BlueprintScreen blueprintScreen);
}
