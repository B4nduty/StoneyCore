package banduty.stoneycore.definitions;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.mobgear.MobGearDataReader;
import banduty.stoneycore.mobgear.SCMobGearRegistry;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class MobGearLoader extends SimplePreparableReloadListener<MobGearDataReader.Result> {

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MobGearLoader());
    }

    @Override
    protected MobGearDataReader.Result prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return MobGearDataReader.readAll(resourceManager);
    }

    @Override
    protected void apply(MobGearDataReader.Result data, ResourceManager resourceManager, ProfilerFiller profiler) {
        SCMobGearRegistry.applyDatapackData(data.weapons(), data.armor(), data.attachments());
        StoneyCore.LOG.debug("Loaded mob gear: {} weapons, {} armor pieces, {} attachments",
                data.weapons().size(), data.armor().size(), data.attachments().size());
    }
}