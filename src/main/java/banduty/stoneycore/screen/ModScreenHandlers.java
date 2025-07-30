package banduty.stoneycore.screen;

import banduty.stoneycore.StoneyCore;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class ModScreenHandlers {
    public static final ScreenHandlerType<BlueprintScreenHandler> BLUEPRINT_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, new Identifier(StoneyCore.MOD_ID, "blueprint_gui"),
                    new ExtendedScreenHandlerType<>(BlueprintScreenHandler::new));

    public static void registerScreenHandlers() {
        StoneyCore.LOGGER.info("Registering Screen Handlers for " + StoneyCore.MOD_ID);
    }
}
