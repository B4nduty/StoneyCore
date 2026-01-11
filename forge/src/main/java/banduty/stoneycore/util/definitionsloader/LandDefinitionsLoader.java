package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.LandTypeRegistry;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LandDefinitionsLoader extends SimplePreparableReloadListener<Void> {
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new LandDefinitionsLoader());
    }

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        LandDefinitionsStorage.clearDefinitions();

        Map<ResourceLocation, Resource> resources =
                resourceManager.listResources("definitions/lands", id -> id.getPath().endsWith(".json"));

        resources.forEach((id, resource) -> {
            try (InputStream stream = resource.open()) {
                JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                DataResult<LandValues> result =
                        LandValues.CODEC.parse(JsonOps.INSTANCE, element);

                result.resultOrPartial(StoneyCore.LOG::error)
                        .ifPresent(def -> {
                            ResourceLocation landId = new ResourceLocation(
                                    id.getNamespace(),
                                    id.getPath().substring("definitions/lands/".length(),
                                            id.getPath().length() - 5)
                            );
                            LandDefinitionsStorage.addDefinition(landId, def);
                            // Also update the LandTypeRegistry if needed
                            LandTypeRegistry.applyOverride(landId, def);
                        });

            } catch (Exception e) {
                StoneyCore.LOG.error("Failed to load land definition from {}: {}", id, e.getMessage(), e);
            }
        });

        StoneyCore.LOG.info("Loaded {} land definitions", LandDefinitionsStorage.DEFINITIONS.size());
    }
}