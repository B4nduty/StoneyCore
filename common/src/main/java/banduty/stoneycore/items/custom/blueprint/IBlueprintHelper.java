package banduty.stoneycore.items.custom.blueprint;

import banduty.stoneycore.screen.BlueprintScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public interface IBlueprintHelper {
    void openBlueprintScreen(ServerPlayer player, ItemStack stack, ResourceLocation structureId);

    MenuType<?> blueprintScreenHandler();

    void renderBlueprintEvents(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, BlueprintScreen blueprintScreen);

    record BlueprintOpeningData(ItemStack stack, ResourceLocation structureId) {
        public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintOpeningData> STREAM_CODEC =
                StreamCodec.of(BlueprintOpeningData::write, BlueprintOpeningData::read);

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
