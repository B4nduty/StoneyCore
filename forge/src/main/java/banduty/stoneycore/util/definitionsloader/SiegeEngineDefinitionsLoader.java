package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
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
public class SiegeEngineDefinitionsLoader extends SimplePreparableReloadListener<Void> {
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SiegeEngineDefinitionsLoader());
    }

    @Override
    protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return null;
    }

    @Override
    protected void apply(Void prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        SiegeEngineDefinitionsStorage.clearDefinitions();

        Map<ResourceLocation, Resource> resources =
                resourceManager.listResources("definitions/siege_engines", id -> id.getPath().endsWith(".json"));

        resources.forEach((id, resource) -> {
            try (InputStream stream = resource.open()) {
                JsonElement element = JsonParser.parseReader(new InputStreamReader(stream));

                DataResult<SiegeEngineDefinitionData> result =
                        SiegeEngineDefinitionData.CODEC.parse(JsonOps.INSTANCE, element);

                result.resultOrPartial(StoneyCore.LOG::error)
                        .ifPresent(def -> {
                            ResourceLocation siegeEngineId = ResourceLocation.tryBuild(
                                    id.getNamespace(),
                                    id.getPath().substring("definitions/siege_engines/".length(),
                                            id.getPath().length() - 5)
                            );
                            SiegeEngineDefinitionsStorage.addDefinition(siegeEngineId, def);
                        });

            } catch (Exception e) {
                StoneyCore.LOG.error("Failed to load siege engine definition from {}: {}", id, e.getMessage(), e);
            }
        });

        StoneyCore.LOG.info("Loaded {} siege engine definitions", SiegeEngineDefinitionsStorage.DEFINITIONS.size());
    }
}