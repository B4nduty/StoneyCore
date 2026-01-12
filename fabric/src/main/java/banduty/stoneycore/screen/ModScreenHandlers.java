package banduty.stoneycore.screen;

import banduty.stoneycore.StoneyCore;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public interface ModScreenHandlers {
    MenuType<BlueprintScreenHandler> BLUEPRINT_SCREEN_HANDLER =
            registerSound("blueprint_gui", new ExtendedScreenHandlerType<>(BlueprintScreenHandler::new));

    private static <T extends AbstractContainerMenu> MenuType<T> registerSound(String name, MenuType<T> menuType) {
        return Registry.register(BuiltInRegistries.MENU, new ResourceLocation(StoneyCore.MOD_ID, name), menuType);
    }

    static void registerMenu() {
        StoneyCore.LOG.info("Registering Menu for " + StoneyCore.MOD_ID);
    }
}