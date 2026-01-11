package banduty.stoneycore.screen;

import banduty.stoneycore.StoneyCore;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;

public class ModScreenHandlers {
    public static final MenuType<BlueprintScreenHandler> BLUEPRINT_SCREEN_HANDLER =
            Registry.register(BuiltInRegistries.MENU,
                    new ResourceLocation(StoneyCore.MOD_ID, "blueprint_gui"),
                    new ExtendedScreenHandlerType<>(BlueprintScreenHandler::new));

    public static void registerScreenHandlers() {
        StoneyCore.LOG.info("Registering Screen Handlers for " + StoneyCore.MOD_ID);
    }
}