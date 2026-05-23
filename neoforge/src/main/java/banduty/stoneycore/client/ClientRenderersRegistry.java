package banduty.stoneycore.client;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.SCItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRenderersRegistry {
    private static final CrownClientExtensions CROWN_RENDERER = new CrownClientExtensions();

    @SubscribeEvent
    public static void registerExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(CROWN_RENDERER, SCItems.CROWN.get());
    }
}
