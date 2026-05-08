package banduty.stoneycore.screen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public interface SCScreenHandlers {
    Supplier<MenuType<BlueprintScreenHandler>> BLUEPRINT_SCREEN_HANDLER = register("blueprint_gui", BlueprintScreenHandler::new);

    @SuppressWarnings("unchecked")
    private static <T extends AbstractContainerMenu> Supplier<MenuType<T>> register(String name, banduty.stoneycore.platform.services.IPlatformHelper.IFactory<T> factory) {
        return Services.PLATFORM.register(
                (Registry<MenuType<T>>) (Registry<?>) BuiltInRegistries.MENU,
                name,
                () -> Services.PLATFORM.createMenuType(factory)
        );
    }

    static void register() {
        StoneyCore.LOG.info("Registering Menu for " + StoneyCore.MOD_ID);
    }
}