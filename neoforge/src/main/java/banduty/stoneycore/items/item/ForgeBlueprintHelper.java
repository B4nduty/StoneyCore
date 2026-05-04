package banduty.stoneycore.items.item;

import banduty.stoneycore.event.custom.RenderBlueprintEvent;
import banduty.stoneycore.items.custom.blueprint.IBlueprintHelper;
import banduty.stoneycore.screen.BlueprintScreen;
import banduty.stoneycore.screen.BlueprintScreenHandler;
import banduty.stoneycore.screen.SCScreenHandlers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

public class ForgeBlueprintHelper implements IBlueprintHelper {

    @Override
    public void openBlueprintScreen(ServerPlayer player, ItemStack stack, ResourceLocation structureId) {
        player.openMenu(new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player p) {
                return new BlueprintScreenHandler(syncId, playerInventory, stack, structureId);
            }

            @Override
            public Component getDisplayName() {
                return Component.literal("Blueprint");
            }
        }, (RegistryFriendlyByteBuf buf) -> {
            ItemStack.STREAM_CODEC.encode(buf, stack);
            buf.writeResourceLocation(structureId);
        });
    }

    @Override
    public MenuType<?> blueprintScreenHandler() {
        return SCScreenHandlers.BLUEPRINT_SCREEN_HANDLER;
    }

    @Override
    public void renderBlueprintEvents(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, BlueprintScreen blueprintScreen) {
        NeoForge.EVENT_BUS.post(new RenderBlueprintEvent(guiGraphics, mouseX, mouseY, delta, blueprintScreen));
    }
}