package banduty.stoneycore.items.item;

import banduty.stoneycore.event.custom.RenderBlueprintEvents;
import banduty.stoneycore.items.custom.blueprint.IBlueprintHelper;
import banduty.stoneycore.screen.BlueprintScreen;
import banduty.stoneycore.screen.BlueprintScreenHandler;
import banduty.stoneycore.screen.SCScreenHandlers;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class FabricBlueprintHelper implements IBlueprintHelper {

    @Override
    public void openBlueprintScreen(ServerPlayer player, ItemStack stack, ResourceLocation structureId) {
        player.openMenu(new ExtendedScreenHandlerFactory<BlueprintOpeningData>() {
            @Override
            public BlueprintOpeningData getScreenOpeningData(ServerPlayer player) {
                return new BlueprintOpeningData(stack, structureId);
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                return new BlueprintScreenHandler(syncId, playerInventory, stack, structureId);
            }

            @Override
            public Component getDisplayName() {
                return Component.literal("Blueprint");
            }
        });
    }

    @Override
    public MenuType<?> blueprintScreenHandler() {
        return SCScreenHandlers.BLUEPRINT_SCREEN_HANDLER;
    }

    @Override
    public void renderBlueprintEvents(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, BlueprintScreen blueprintScreen) {
        RenderBlueprintEvents.EVENT.invoker().render(guiGraphics, mouseX, mouseY, delta, blueprintScreen);
    }

    public record BlueprintOpeningData(ItemStack stack, ResourceLocation structureId) {
        public static void write(RegistryFriendlyByteBuf buf, BlueprintOpeningData data) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, data.stack());
            ResourceLocation.STREAM_CODEC.encode(buf, data.structureId());
        }

        public static BlueprintOpeningData read(RegistryFriendlyByteBuf buf) {
            return new BlueprintOpeningData(
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    ResourceLocation.STREAM_CODEC.decode(buf)
            );
        }
    }
}