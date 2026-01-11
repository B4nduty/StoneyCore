package banduty.stoneycore.screen;

import banduty.stoneycore.StoneyCore;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModScreenHandlers {
    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, StoneyCore.MOD_ID);

    public static final RegistryObject<MenuType<BlueprintScreenHandler>> BLUEPRINT_SCREEN_HANDLER =
            MENU_TYPES.register("blueprint_gui", () ->
                    IForgeMenuType.create((windowId, inv, data) ->
                            new BlueprintScreenHandler(windowId, inv, data.readItem(), data.readResourceLocation())
                    )
            );

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
        StoneyCore.LOG.info("Registering Screen Handlers for " + StoneyCore.MOD_ID);
    }
}