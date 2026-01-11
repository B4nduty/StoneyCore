package banduty.stoneycore.util.definitionsloader;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.LandTypeRegistry;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class LandDefinitionsLoader implements IdentifiableResourceReloadListener {
    private static final ResourceLocation RELOAD_LISTENER_ID =
            new ResourceLocation(StoneyCore.MOD_ID, "land_definitions_loader");

    @Override
    public ResourceLocation getFabricId() {
        return RELOAD_LISTENER_ID;
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier synchronizer,
                                                   @NotNull ResourceManager resourceManager,
                                                   @NotNull ProfilerFiller prepareProfiler,
                                                   @NotNull ProfilerFiller applyProfiler,
                                                   @NotNull Executor prepareExecutor,
                                                   @NotNull Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
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
        }, prepareExecutor).thenCompose(synchronizer::wait).thenRunAsync(() -> {
            StoneyCore.LOG.info("Loaded {} land definitions", LandDefinitionsStorage.DEFINITIONS.size());
        }, applyExecutor);
    }
}