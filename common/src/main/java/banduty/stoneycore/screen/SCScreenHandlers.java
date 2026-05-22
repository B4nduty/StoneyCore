package banduty.stoneycore.screen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.blueprint.IBlueprintHelper;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.platform.services.IPlatformHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public interface SCScreenHandlers {
    Supplier<MenuType<BlueprintScreenHandler>> BLUEPRINT_SCREEN_HANDLER = registerExtended("blueprint_gui", BlueprintScreenHandler::new, IBlueprintHelper.BlueprintOpeningData.STREAM_CODEC);

    @SuppressWarnings("unchecked")
    private static <T extends AbstractContainerMenu, D> Supplier<MenuType<T>> registerExtended(
            String name, IPlatformHelper.IExtendedFactory<T, D> factory, StreamCodec<RegistryFriendlyByteBuf, D> codec) {
        return Services.PLATFORM.register(
                (Registry<MenuType<T>>) (Registry<?>) BuiltInRegistries.MENU,
                name,
                () -> Services.PLATFORM.createMenuType(factory, codec)
        );
    }

    static void register() {
        StoneyCore.LOG.info("Registering Menu for " + StoneyCore.MOD_ID);
    }
}