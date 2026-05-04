package banduty.stoneycore.screen;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface SCScreenHandlers {
    MenuType<BlueprintScreenHandler> BLUEPRINT_SCREEN_HANDLER = register("blueprint_gui", BlueprintScreenHandler::new);

    @SuppressWarnings("unchecked")
    private static <T extends AbstractContainerMenu> MenuType<T> register(String name, banduty.stoneycore.platform.services.IPlatformHelper.IFactory<T> factory) {
        return (MenuType<T>) Services.PLATFORM.register(
                BuiltInRegistries.MENU,
                name,
                () -> Services.PLATFORM.createMenuType(factory)
        ).get();
    }

    static void register() {
        StoneyCore.LOG.info("Registering Menu for " + StoneyCore.MOD_ID);
    }
}